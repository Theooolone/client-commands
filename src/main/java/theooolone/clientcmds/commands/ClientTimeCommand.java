package theooolone.clientcmds.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class ClientTimeCommand {
	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		return ClientCommandManager.literal("time")
						.then(ClientCommandManager.literal("daytime").executes(
								context -> executeQuery(context.getSource(), (int)(context.getSource().getWorld().getTimeOfDay() % 24000L))
						))
						.then(ClientCommandManager.literal("gametime").executes(
								context -> executeQuery(context.getSource(), (int)(context.getSource().getWorld().getTime() % 2147483647L))
						))
						.then(ClientCommandManager.literal("day").executes(
								context -> executeQuery(context.getSource(), (int)(context.getSource().getWorld().getTimeOfDay() / 24000L % 2147483647L))
						));
	}

	private static int executeQuery(FabricClientCommandSource source, int time) {
		source.sendFeedback(Text.translatable("commands.time.query", time));
		return time;
	}
}

