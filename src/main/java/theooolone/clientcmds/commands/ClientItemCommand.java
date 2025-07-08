package theooolone.clientcmds.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import theooolone.clientcmds.ModifyCreativeInventory;

import java.util.List;


public class ClientItemCommand {
	private static final DynamicCommandExceptionType NO_SUCH_SLOT_SOURCE_EXCEPTION = new DynamicCommandExceptionType(
			slot -> Text.stringifiedTranslatable("commands.clientitem.no_such_slot", slot)
	);
	private static final Dynamic2CommandExceptionType KNOWN_ITEM_EXCEPTION = new Dynamic2CommandExceptionType(
			(itemName, slot) -> Text.stringifiedTranslatable("commands.clientitem.no_changed.known_item", itemName, slot)
	);

	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		return ClientCommandManager.literal("item")
				.then(ClientCommandManager.argument("slot", ItemSlotArgumentType.itemSlot())
						.then(ClientCommandManager.literal("with")
								.then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
										.executes(context -> executeReplace(
												context.getSource(),
												context.<Integer>getArgument("slot", Integer.class),
												ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false)
										))
										.then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 99))
												.executes(context -> executeReplace(
														context.getSource(),
														context.<Integer>getArgument("slot", Integer.class),
														ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), true)
												))
										)
								)
						)
						.then(ClientCommandManager.literal("from")
								.then(ClientCommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot())
										.executes(context -> executeCopy(
												context.getSource(),
												context.<Integer>getArgument("sourceSlot", Integer.class),
												context.<Integer>getArgument("slot", Integer.class)
										))
								)
						)
				);
	}

	private static int executeReplace(FabricClientCommandSource source, int slot, ItemStack stack) throws CommandSyntaxException {
		if (!source.getPlayer().isInCreativeMode()) {
			source.sendError(Text.translatable("commands.failed.wronggamemode"));
			return 0;
		}
		List<ItemStack> oldInventory = ModifyCreativeInventory.GetInventoryAsList(source.getPlayer());
		if (!source.getPlayer().getStackReference(slot).set(stack.copy())) {
			throw KNOWN_ITEM_EXCEPTION.create(stack.toHoverableText(), slot);
		}
		ModifyCreativeInventory.UpdateInventoryOnServer(source.getClient(), oldInventory);
		source.sendFeedback(Text.translatable("commands.clientitem.set.success", stack.toHoverableText()));
		return 1;
	}

	private static int executeCopy(FabricClientCommandSource source, int sourceSlot, int slot) throws CommandSyntaxException {
		return executeReplace(source, slot, getStackInSlot(source.getPlayer(), sourceSlot));
	}

	private static ItemStack getStackInSlot(ClientPlayerEntity player, int slotId) throws CommandSyntaxException {
		StackReference stackReference = player.getStackReference(slotId);
		if (stackReference == StackReference.EMPTY) {
			throw NO_SUCH_SLOT_SOURCE_EXCEPTION.create(slotId);
		} else {
			return stackReference.get().copy();
		}
	}
}
