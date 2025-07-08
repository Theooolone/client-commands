package theooolone.clientcmds.client;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface ClientPosArgument extends PosArgument {
	Vec3d toAbsolutePos(FabricClientCommandSource source);

	default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
		return BlockPos.ofFloored(this.toAbsolutePos(source));
	}
	default Vec3d getPos(FabricClientCommandSource source) {
		return this.toAbsolutePos(source);
	}
}
