package theooolone.clientcmds;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theooolone.clientcmds.commands.*;
import theooolone.clientcmds.commands.ClientDataCommand;

public class ClientCommands implements ClientModInitializer {
	public static final String MOD_ID = "clientside-commands";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, r) -> {
			dispatcher.register(
					ClientCommandManager.literal("c")
							.then(ClientGiveCommand.builder(r))
							.then(ClientItemCommand.builder(r))
							.then(ClientClearCommand.builder(r))
							.then(ClientTellrawCommand.builder(r))
							.then(ClientTitleCommand.builder(r))
							.then(ClientDataCommand.builder(r))
							.then(ClientTimeCommand.builder(r))
							.then(ClientPlaysoundCommand.builder(r))
							.then(ClientStopsoundCommand.builder(r))
			);
		}));
	}
}