package theooolone.clientcmds.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import theooolone.clientcmds.client.ClientPosArgument;

public class ClientPlaysoundCommand {
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.playsound.failed"));

	public static final SuggestionProvider<FabricClientCommandSource> AVAILABLE_SOUNDS = SuggestionProviders.register(
			Identifier.of("clientside_commands","available_sounds"), (context, builder) -> CommandSource.suggestIdentifiers(context.getSource().getSoundIds(), builder)
	);

	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		RequiredArgumentBuilder<FabricClientCommandSource, Identifier> requiredArgumentBuilder = ClientCommandManager.argument("sound", IdentifierArgumentType.identifier())
				.suggests(AVAILABLE_SOUNDS)
				.executes(
						context -> execute(
								context.getSource(),
								context.getArgument("sound", Identifier.class),
								SoundCategory.MASTER,
								context.getSource().getPosition(),
								1.0F,
								1.0F,
								0.0F
						)
				);

		for (SoundCategory soundCategory : SoundCategory.values()) {
			requiredArgumentBuilder.then(makeArgumentsForCategory(soundCategory));
		}


		return ClientCommandManager.literal("playsound").then(requiredArgumentBuilder);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> makeArgumentsForCategory(SoundCategory category) {
		return ClientCommandManager.literal(category.getName())
				.executes(
						context -> execute(
								context.getSource(),
								context.getArgument("sound", Identifier.class),
								category,
								context.getSource().getPosition(),
								1.0F,
								1.0F,
								0.0F
						)
				)
				.then(
						ClientCommandManager.argument("pos", Vec3ArgumentType.vec3())
								.executes(
										context -> execute(
												context.getSource(),
												context.getArgument("sound", Identifier.class),
												category,
												((ClientPosArgument) context.getArgument("pos", PosArgument.class)).getPos(context.getSource()),
												1.0F,
												1.0F,
												0.0F
										)
								)
								.then(
										ClientCommandManager.argument("volume", FloatArgumentType.floatArg(0.0F))
												.executes(
														context -> execute(
																context.getSource(),
																context.getArgument("sound", Identifier.class),
																category,
																((ClientPosArgument) context.getArgument("pos", PosArgument.class)).getPos(context.getSource()),
																context.<Float>getArgument("volume", Float.class),
																1.0F,
																0.0F
														)
												)
												.then(
														ClientCommandManager.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
																.executes(
																		context -> execute(
																				context.getSource(),
																				context.getArgument("sound", Identifier.class),
																				category,
																				((ClientPosArgument) context.getArgument("pos", PosArgument.class)).getPos(context.getSource()),
																				context.<Float>getArgument("volume", Float.class),
																				context.<Float>getArgument("pitch", Float.class),
																				0.0F
																		)
																)
																.then(
																		ClientCommandManager.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F))
																				.executes(
																						context -> execute(
																								context.getSource(),
																								context.getArgument("sound", Identifier.class),
																								category,
																								((ClientPosArgument) context.getArgument("pos", PosArgument.class)).getPos(context.getSource()),
																								context.<Float>getArgument("volume", Float.class),
																								context.<Float>getArgument("pitch", Float.class),
																								context.<Float>getArgument("minVolume", Float.class)
																						)
																				)
																)
												)
								)
				);
	}

	private static int execute(
			FabricClientCommandSource source,
			Identifier sound,
			SoundCategory category,
			Vec3d pos,
			float volume,
			float pitch,
			float minVolume
	) throws CommandSyntaxException {
		RegistryEntry<SoundEvent> registryEntry = RegistryEntry.of(SoundEvent.of(sound));
		double soundRadius = MathHelper.square(registryEntry.value().getDistanceToTravel(volume));
		long random = source.getWorld().getRandom().nextLong();

		PlayerEntity player = source.getPlayer();
		double e = pos.x - player.getX();
		double f = pos.y - player.getY();
		double g = pos.z - player.getZ();
		double soundDistance = e * e + f * f + g * g;
		Vec3d vec3d = pos;
		float vol = volume;
		if (soundDistance > soundRadius) {
			if (minVolume <= 0.0F) {
				throw FAILED_EXCEPTION.create();
			}

			double k = Math.sqrt(soundDistance);
			vec3d = new Vec3d(player.getX() + e / k * 2.0, player.getY() + f / k * 2.0, player.getZ() + g / k * 2.0);
			vol = minVolume;
		}

		source.getClient().world.playSound(
				source.getPlayer(),
				vec3d.getX(),
				vec3d.getY(),
				vec3d.getZ(),
				registryEntry,
				category,
				vol, pitch, random);

		source.sendFeedback(Text.translatable("commands.playsound.success", Text.of(sound)));

		return 1;
	}
}
