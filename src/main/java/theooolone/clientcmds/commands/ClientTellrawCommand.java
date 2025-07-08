package theooolone.clientcmds.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.*;

public class ClientTellrawCommand {
	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		return ClientCommandManager.literal("tellraw")
				.then(ClientCommandManager.argument("message", TextArgumentType.text(registryAccess)).executes(context -> {
					// That was oddly easy, makes me concerned as to why TextArgumentType.parseTextArgument is so complex, am I missing anything?
					// TextArgumentType.parseTextArgument is the method used in vanilla's TellRawCommand
					context.getSource().getClient().inGameHud.getChatHud().addMessage(context.getArgument("message", Text.class));
					return 1;
				}));
	}
}
