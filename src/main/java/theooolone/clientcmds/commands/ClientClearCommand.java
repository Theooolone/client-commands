package theooolone.clientcmds.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import theooolone.clientcmds.ModifyCreativeInventory;

import java.util.List;
import java.util.function.Predicate;

public class ClientClearCommand {
	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		return ClientCommandManager.literal("clear")
				.executes(context -> execute(context.getSource(), stack -> true))
				.then(ClientCommandManager.argument("item", ItemPredicateArgumentType.itemPredicate(registryAccess))
						.executes(context -> execute(
								context.getSource(), context.getArgument("item", ItemPredicateArgumentType.ItemStackPredicateArgument.class)
						))
						.then(
								ClientCommandManager.argument("maxCount", IntegerArgumentType.integer(0))
										.executes(
												context -> execute(
														context.getSource(),
														context.getArgument("item", ItemPredicateArgumentType.ItemStackPredicateArgument.class),
														IntegerArgumentType.getInteger(context, "maxCount")
												)
										)
						)
				);
	}

	private static int execute(FabricClientCommandSource source, Predicate<ItemStack> item) throws CommandSyntaxException {
		return execute(source, item, -1);
	}

	private static int execute(FabricClientCommandSource source, Predicate<ItemStack> item, int maxCount) throws CommandSyntaxException {
		if (!source.getPlayer().isInCreativeMode()) {
			source.sendError(Text.translatable("commands.failed.wronggamemode"));
			return 0;
		}
		ClientPlayerEntity player = source.getPlayer();
		int i = 0;

		List<ItemStack> oldInventory = ModifyCreativeInventory.GetInventoryAsList(player);
		i += player.getInventory().remove(item, maxCount, player.playerScreenHandler.getCraftingInput());
		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.getInventory());
		ModifyCreativeInventory.UpdateInventoryOnServer(source.getClient(), oldInventory);

		if (i == 0) {
			source.sendError(Text.translatable("clientclear.failed"));
			return 0;
		} else {
			int j = i;
			if (maxCount == 0) {
				source.sendFeedback(Text.translatable("commands.clientclear.test", j, true));
			} else {
				source.sendFeedback(Text.translatable("commands.clientclear.success", j, true));
			}
			return i;
		}
	}
}
