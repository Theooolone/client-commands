package theooolone.clientcmds.client;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.Entity;

import java.util.List;

public interface ClientEntitySelector {
	Entity getEntity(FabricClientCommandSource source) throws CommandSyntaxException;
	List<? extends Entity> getEntities(FabricClientCommandSource source);

}
