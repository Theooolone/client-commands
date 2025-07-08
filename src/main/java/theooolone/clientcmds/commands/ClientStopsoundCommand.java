package theooolone.clientcmds.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ClientStopsoundCommand {
	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		LiteralArgumentBuilder<FabricClientCommandSource> requiredArgumentBuilder = ClientCommandManager.literal("*")
				.then(
						ClientCommandManager.argument("sound", IdentifierArgumentType.identifier())
								.suggests(ClientPlaysoundCommand.AVAILABLE_SOUNDS)
								.executes(
										context -> execute(
												context.getSource(), null, context.getArgument("sound", Identifier.class)
										)
								)
				);

		for (SoundCategory soundCategory : SoundCategory.values()) {
			requiredArgumentBuilder.then(
					ClientCommandManager.literal(soundCategory.getName())
							.executes(context -> execute(context.getSource(), soundCategory, null))
							.then(
									ClientCommandManager.argument("sound", IdentifierArgumentType.identifier())
											.suggests(ClientPlaysoundCommand.AVAILABLE_SOUNDS)
											.executes(
													context -> execute(
															context.getSource(), soundCategory, context.getArgument("sound", Identifier.class)
													)
											)
							)
			);
		}

		return ClientCommandManager.literal("stopsound")
				.executes(context -> execute(context.getSource(), null, null))
				.then(requiredArgumentBuilder);
	}

	private static int execute(FabricClientCommandSource source, @Nullable SoundCategory category, @Nullable Identifier sound) {
		source.getClient().getSoundManager().stopSounds(sound, category);

		if (category != null) {
			if (sound != null) {
				source.sendFeedback(Text.translatable("commands.stopsound.success.source.sound", Text.of(sound), category.getName()));
			} else {
				source.sendFeedback(Text.translatable("commands.stopsound.success.source.any", category.getName()));
			}
		} else if (sound != null) {
			source.sendFeedback(Text.translatable("commands.stopsound.success.sourceless.sound", Text.of(sound)));
		} else {
			source.sendFeedback(Text.translatable("commands.stopsound.success.sourceless.any"));
		}

		return 1;
	}
}