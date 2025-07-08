package theooolone.clientcmds.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.text.Text;

public class ClientTitleCommand {

	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		return ClientCommandManager.literal("title")
				.then(ClientCommandManager.literal("clear").executes(context -> executeClear(context.getSource())))
				.then(ClientCommandManager.literal("reset").executes(context -> executeReset(context.getSource())))
				.then(
						ClientCommandManager.literal("title")
								.then(
										ClientCommandManager.argument("title", TextArgumentType.text(registryAccess))
												.executes(
														context -> executeTitle(
																context.getSource(),
																context.getArgument("title", Text.class)
														)
												)
								)
				)
				.then(
						ClientCommandManager.literal("subtitle")
								.then(
										ClientCommandManager.argument("title", TextArgumentType.text(registryAccess))
												.executes(
														context -> executeSubtitle(
																context.getSource(),
																context.getArgument("title", Text.class)
														)
												)
								)
				)
				.then(
						ClientCommandManager.literal("actionbar")
								.then(
										ClientCommandManager.argument("title", TextArgumentType.text(registryAccess))
												.executes(
														context -> executeActionbar(
																context.getSource(),
																context.getArgument("title", Text.class)
														)
												)
								)
				)
				.then(
						ClientCommandManager.literal("times")
								.then(
										ClientCommandManager.argument("fadeIn", TimeArgumentType.time())
												.then(
														ClientCommandManager.argument("stay", TimeArgumentType.time())
																.then(
																		ClientCommandManager.argument("fadeOut", TimeArgumentType.time())
																				.executes(
																						context -> executeTimes(
																								context.getSource(),
																								IntegerArgumentType.getInteger(context, "fadeIn"),
																								IntegerArgumentType.getInteger(context, "stay"),
																								IntegerArgumentType.getInteger(context, "fadeOut")
																						)
																				)
																)
												)
								)
				);
	}

	private static int executeClear(FabricClientCommandSource source) {
		source.getClient().inGameHud.clearTitle();
		source.sendFeedback(Text.translatable("commands.clienttitle.clear"));
		return 1;
	}

	private static int executeReset(FabricClientCommandSource source) {
		source.getClient().inGameHud.clearTitle();
		source.getClient().inGameHud.setDefaultTitleFade();
		source.sendFeedback(Text.translatable("commands.clienttitle.reset"));
		return 1;
	}

	private static int executeTitle(FabricClientCommandSource source, Text title) {
		source.getClient().inGameHud.setTitle(title);
		source.sendFeedback(Text.translatable("commands.clienttitle.show.title"));
		return 1;
	}

	private static int executeSubtitle(FabricClientCommandSource source, Text title) {
		source.getClient().inGameHud.setSubtitle(title);
		source.sendFeedback(Text.translatable("commands.clienttitle.show.subtitle"));
		return 1;
	}

	private static int executeActionbar(FabricClientCommandSource source, Text title) {
		source.getClient().inGameHud.setOverlayMessage(title,false);
		source.sendFeedback(Text.translatable("commands.clienttitle.show.actionbar"));
		return 1;
	}

	private static int executeTimes(FabricClientCommandSource source, int fadeIn, int stay, int fadeOut) {
		source.getClient().inGameHud.setTitleTicks(fadeIn, stay, fadeOut);
		source.sendFeedback(Text.translatable("commands.clienttitle.times"));
		return 1;
	}
}
