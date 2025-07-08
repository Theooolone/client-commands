package theooolone.clientcmds.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import theooolone.clientcmds.client.ClientEntitySelector;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin implements ClientEntitySelector {

	@Shadow @Final private boolean includesNonPlayers;
	@Shadow @Final private Function<Vec3d, Vec3d> positionOffset;
	@Shadow @Final private boolean senderOnly;
	@Shadow @Final @Nullable private String playerName;
	@Shadow @Final @Nullable private UUID uuid;
	@Shadow @Final private TypeFilter<Entity, ?> entityFilter;
	@Shadow private Predicate<Entity> getPositionPredicate(Vec3d pos, @Nullable Box box, @Nullable FeatureSet enabledFeatures) {return null;}
	@Shadow @Nullable private Box getOffsetBox(Vec3d offset) {return null;}
	@Shadow private int getAppendLimit() {return 0;}
	@Shadow private <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities) {return null;}


	public Entity getEntity(FabricClientCommandSource source) throws CommandSyntaxException {
		List<? extends Entity> list = this.getEntities(source);
		if (list.isEmpty()) {
			throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
		} else if (list.size() > 1) {
			throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
		} else {
			return list.getFirst();
		}
	}

	public List<? extends Entity> getEntities(FabricClientCommandSource source) {
		if (!this.includesNonPlayers) {
			return this.getPlayers(source);
		} else if (this.playerName != null) {
			PlayerEntity playerEntity = clientGetPlayerEntity(this.playerName, source.getWorld());
			return playerEntity == null ? List.of() : List.of(playerEntity);
		} else if (this.uuid != null) {
			Entity entity = source.getWorld().getEntity(this.uuid);
			if (entity != null) {
				if (entity.getType().isEnabled(source.getEnabledFeatures())) {
					return List.of(entity);
				}
			}
			return List.of();
		} else {
			Vec3d vec3d = this.positionOffset.apply(source.getPosition());
			Box box = this.getOffsetBox(vec3d);
			if (this.senderOnly) {
				Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, null);
				return source.getEntity() != null && predicate.test(source.getEntity()) ? List.of(source.getEntity()) : List.of();
			} else {
				Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, source.getEnabledFeatures());
				List<Entity> list = new ObjectArrayList<>();
				this.appendEntitiesFromWorld(list, source.getWorld(), box, predicate);
				return this.getEntities(vec3d, list);
			}
		}
	}

	@Unique
	private void appendEntitiesFromWorld(List<Entity> entities, World world, @Nullable Box box, Predicate<Entity> predicate) {
		int i = this.getAppendLimit();
		if (entities.size() < i) {
			if (box != null) {
				world.collectEntitiesByType(this.entityFilter, box, predicate, entities, i);
			} else {
				((WorldMixin)world).invokeGetEntityLookup().forEach(this.entityFilter, (entity) -> {
					if (predicate.test(entity)) {
						entities.add(entity);
						if (entities.size() >= i) {
							return LazyIterationConsumer.NextIteration.ABORT;
						}
					}

					return LazyIterationConsumer.NextIteration.CONTINUE;
				});
			}
		}
	}

	@Unique
	static private PlayerEntity clientGetPlayerEntity(String name, World world) {
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	@Unique
	static private PlayerEntity clientGetPlayerEntity(UUID uuid, World world) {
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getGameProfile().getId().equals(uuid)) {
				return player;
			}
		}
		return null;
	}

	@Unique
	static private List<PlayerEntity> clientGetPlayers(World world, Predicate<? super PlayerEntity> predicate, int limit) {
		List<PlayerEntity> list = Lists.newArrayList();
		for (PlayerEntity playerEntity : world.getPlayers()) {
			if (predicate.test(playerEntity)) {
				list.add(playerEntity);
				if (list.size() >= limit) {
					return list;
				}
			}
		}
		return list;
	}


	@Unique
	public List<PlayerEntity> getPlayers(FabricClientCommandSource source) {
		if (this.playerName != null) {
			PlayerEntity PlayerEntity = clientGetPlayerEntity(this.playerName, source.getWorld());
			return PlayerEntity == null ? List.of() : List.of(PlayerEntity);
		} else if (this.uuid != null) {
			PlayerEntity PlayerEntity = clientGetPlayerEntity(this.uuid, source.getWorld());
			return PlayerEntity == null ? List.of() : List.of(PlayerEntity);
		} else {
			Vec3d vec3d = this.positionOffset.apply(source.getPosition());
			Box box = this.getOffsetBox(vec3d);
			Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, null);
			if (this.senderOnly) {
				return source.getEntity() instanceof PlayerEntity playerEntity2 && predicate.test(playerEntity2)
						? List.of(playerEntity2)
						: List.of();
			} else {
				int i = this.getAppendLimit();
				List<PlayerEntity> list;
				list = clientGetPlayers(source.getWorld(), predicate, i);
				return this.getEntities(vec3d, list);
			}
		}
	}

}
