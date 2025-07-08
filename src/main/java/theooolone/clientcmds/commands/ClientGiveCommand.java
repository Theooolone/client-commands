package theooolone.clientcmds.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import theooolone.clientcmds.ModifyCreativeInventory;

import java.util.List;

public class ClientGiveCommand {
	public static final int MAX_STACKS = 100;

	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		return ClientCommandManager.literal("give")
				.then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
						.executes(context -> execute(
								context.getSource(),
								ItemStackArgumentType.getItemStackArgument(context, "item"),
								1
						))
						.then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1))
								.executes(context -> execute(
										context.getSource(),
										ItemStackArgumentType.getItemStackArgument(context, "item"),
										IntegerArgumentType.getInteger(context, "count")
								))
						)
				);
	}

	private static int execute(FabricClientCommandSource source, ItemStackArgument item, int count) throws CommandSyntaxException {
		if (!source.getPlayer().isInCreativeMode()) {
			source.sendError(Text.translatable("commands.failed.wronggamemode"));
			return 0;
		}
		ItemStack itemStack = item.createStack(1, false);
		int stackMaxCount = itemStack.getMaxCount();
		int giveLimit = stackMaxCount * MAX_STACKS;

		if (count > giveLimit) {
			source.sendError(Text.translatable("commands.give.failed.toomanyitems", giveLimit, itemStack.toHoverableText()));
			return 0;
		}

		ClientPlayerEntity player = source.getPlayer();

		List<ItemStack> oldInventory = ModifyCreativeInventory.GetInventoryAsList(player);

		int k = count;

		while (k > 0) {
			int l = Math.min(stackMaxCount, k);
			k -= l;
			ItemStack itemStack2 = item.createStack(l, false);
			boolean bl = player.getInventory().insertStack(itemStack2);

			if (bl && itemStack2.isEmpty()) {

				player.playSoundToPlayer(
						SoundEvents.ENTITY_ITEM_PICKUP,
						SoundCategory.PLAYERS,
						0.2F,
						((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
				);
			}
		}

		ModifyCreativeInventory.UpdateInventoryOnServer(source.getClient(), oldInventory);

		source.sendFeedback(Text.translatable("commands.clientgive.success", count, itemStack.toHoverableText()));
		return 1;
	}
}