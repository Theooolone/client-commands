package theooolone.clientcmds.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.*;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.nbt.*;
import net.minecraft.server.command.DataCommand;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import theooolone.clientcmds.client.ClientEntitySelector;
import theooolone.clientcmds.client.ClientPosArgument;

import java.util.List;
import java.util.function.Function;

public class ClientDataCommand {
	static final SimpleCommandExceptionType INVALID_BLOCK_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.data.block.invalid"));

	public static final Function<String, ClientDataCommand.ObjectType> ENTITY_TYPE_FACTORY = argumentName -> new ClientDataCommand.ObjectType() {
		@Override
		public DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
			return new EntityDataObject(((ClientEntitySelector) context.getArgument(argumentName, EntitySelector.class)).getEntity(context.getSource()));
		}

		@Override
		public ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				Function<ArgumentBuilder<FabricClientCommandSource, ?>, ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		) {
			return ClientCommandManager.literal("entity")
					.then(argumentAdder.apply(
							ClientCommandManager.argument(argumentName, EntityArgumentType.entity()))
					);
		}
	};

	public static final Function<String, ClientDataCommand.ObjectType> BLOCK_TYPE_FACTORY = argumentName -> new ClientDataCommand.ObjectType() {
		@Override
		public DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {

			BlockPos blockPos = ((ClientPosArgument) context.getArgument(argumentName + "Pos", PosArgument.class)).toAbsoluteBlockPos(context.getSource());
			BlockEntity blockEntity = context.getSource().getWorld().getBlockEntity(blockPos);
			if (blockEntity == null) {
				throw INVALID_BLOCK_EXCEPTION.create();
			} else {
				return new BlockDataObject(blockEntity, blockPos);
			}
		}

		@Override
		public ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				Function<ArgumentBuilder<FabricClientCommandSource, ?>, ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		) {
			return ClientCommandManager.literal("block")
					.then(argumentAdder.apply(
							ClientCommandManager.argument(argumentName + "Pos", BlockPosArgumentType.blockPos())
					)
			);
		}
	};

	private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType(
			path -> Text.stringifiedTranslatable("commands.data.get.unknown", path)
	);
	private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType(
			path -> Text.stringifiedTranslatable("commands.data.get.invalid", path)
	);

	public static final List<Function<String, ClientDataCommand.ObjectType>> OBJECT_TYPE_FACTORIES = ImmutableList.of(
			ENTITY_TYPE_FACTORY, BLOCK_TYPE_FACTORY
	);
	public static final List<ClientDataCommand.ObjectType> TARGET_OBJECT_TYPES = OBJECT_TYPE_FACTORIES.stream()
			.map(factory -> factory.apply("target"))
			.collect(ImmutableList.toImmutableList());

	public static LiteralArgumentBuilder<FabricClientCommandSource> builder(CommandRegistryAccess registryAccess) {
		LiteralArgumentBuilder<FabricClientCommandSource> literalArgumentBuilder = ClientCommandManager.literal("data");
		for (ClientDataCommand.ObjectType objectType : TARGET_OBJECT_TYPES) {
			literalArgumentBuilder.then(
					objectType.addArgumentsToBuilder(
							builder -> builder.executes(context -> executeGet(context.getSource(), objectType.getObject(context)))
									.then(
											ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath())
													.executes(context -> executeGet(context.getSource(), objectType.getObject(context), context.getArgument("path", NbtPathArgumentType.NbtPath.class)))
													.then(
															ClientCommandManager.argument("scale", DoubleArgumentType.doubleArg())
																	.executes(
																			context -> executeGet(
																					context.getSource(),
																					objectType.getObject(context),
																					context.getArgument("path", NbtPathArgumentType.NbtPath.class),
																					DoubleArgumentType.getDouble(context, "scale")
																			)
																	)
													)
									)
					)
			);

		}
		return literalArgumentBuilder;
	}

	private static int executeGet(FabricClientCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
		NbtElement nbtElement = DataCommand.getNbt(path, object);
		if (!(nbtElement instanceof AbstractNbtNumber)) {
			throw GET_INVALID_EXCEPTION.create(path.toString());
		} else {
			int i = MathHelper.floor(((AbstractNbtNumber)nbtElement).doubleValue() * scale);
			source.sendFeedback(object.feedbackGet(path, scale, i));
			return i;
		}
	}

	private static int executeGet(FabricClientCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
		NbtElement nbtElement = DataCommand.getNbt(path, object);

		int i = switch (nbtElement) {
			case AbstractNbtNumber abstractNbtNumber -> MathHelper.floor(abstractNbtNumber.doubleValue());
			case AbstractNbtList abstractNbtList -> abstractNbtList.size();
			case NbtCompound nbtCompound -> nbtCompound.getSize();
			case NbtString(String var14) -> var14.length();
			case NbtEnd nbtEnd -> throw GET_UNKNOWN_EXCEPTION.create(path.toString());
		};
		source.sendFeedback(object.feedbackQuery(nbtElement));
		return i;
	}

	private static int executeGet(FabricClientCommandSource source, DataCommandObject object) throws CommandSyntaxException {
		source.sendFeedback(object.feedbackQuery(object.getNbt()));
		return 1;
	}

	public interface ObjectType {
		DataCommandObject getObject(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException;

		ArgumentBuilder<FabricClientCommandSource, ?> addArgumentsToBuilder(
				Function<ArgumentBuilder<FabricClientCommandSource, ?>, ArgumentBuilder<FabricClientCommandSource, ?>> argumentAdder
		);
	}
}
