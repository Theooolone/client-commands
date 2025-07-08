package theooolone.clientcmds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModifyCreativeInventory {
	public static List<ItemStack> GetInventoryAsList(ClientPlayerEntity player) {
		List<ItemStack> before = new ArrayList<>();
		for (int i = 0; i < player.playerScreenHandler.slots.size(); i++) {
			before.add(player.playerScreenHandler.slots.get(i).getStack().copy());
		}
		return before;
	}

	public static void UpdateInventoryOnServer(MinecraftClient client, List<ItemStack> oldInventory) {
		if (client.player == null) {
			ClientCommands.LOGGER.error("Can't update inventory, no player detected");
			return;
		}
		if (client.interactionManager == null) {
			ClientCommands.LOGGER.error("Can't update inventory, no interactionManager detected");
			return;
		}
		List<Integer> modifiedSlots = new ArrayList<>();
		for (int i = 0; i < client.player.playerScreenHandler.slots.size(); i++) {
			ItemStack beforeStack = oldInventory.get(i);
			ItemStack afterStack = client.player.playerScreenHandler.slots.get(i).getStack();
			if (!ItemStack.areEqual(beforeStack, afterStack)) {
				modifiedSlots.add(i);
			}
		}

		for (Integer modifiedSlot : modifiedSlots) {
			client.interactionManager.clickCreativeStack(
					client.player.playerScreenHandler.slots.get(modifiedSlot).getStack(),
					modifiedSlot
			);
		}
	}
}
