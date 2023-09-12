package dev.ftb.packcompanion.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

public class PlaceJigsawCommand implements CommandEntry {
    private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.place.jigsaw.failed"));
    private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType(arg -> new TranslatableComponent("commands.place.jigsaw.invalid", arg));

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("place_jigsaw")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.argument("pool", ResourceKeyArgument.key(Registry.TEMPLATE_POOL_REGISTRY))
                        .then(Commands.argument("target", ResourceLocationArgument.id())
                                .then(Commands.argument("max_depth", IntegerArgumentType.integer(1, 7))
                                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                                                .executes(ctx -> placeJigsaw(ctx.getSource(), getStructureTemplatePool(ctx, "pool"), ResourceLocationArgument.getId(ctx, "target"), IntegerArgumentType.getInteger(ctx, "max_depth"), BlockPosArgument.getLoadedBlockPos(ctx, "position")))
                                        )
                                )
                        )
                )
                .then(Commands.argument("pool", ResourceKeyArgument.key(Registry.TEMPLATE_POOL_REGISTRY))
                        .then(Commands.argument("target", ResourceLocationArgument.id())
                                .then(Commands.argument("max_depth", IntegerArgumentType.integer(1, 7))
                                        .executes(ctx -> placeJigsaw(ctx.getSource(), getStructureTemplatePool(ctx, "pool"), ResourceLocationArgument.getId(ctx, "target"), IntegerArgumentType.getInteger(ctx, "max_depth"), new BlockPos(ctx.getSource().getPosition())))
                                )
                        )
                );
    }

    public static int placeJigsaw(CommandSourceStack source, Holder<StructureTemplatePool> pool, ResourceLocation target, int maxDepth, BlockPos position) throws CommandSyntaxException {
        return placeJigsaw(source, pool.unwrapKey().orElseThrow().location(), target, maxDepth, position);
    }

    public static int placeJigsaw(CommandSourceStack source, ResourceLocation pool, ResourceLocation target, int maxDepth, BlockPos position) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();

        BlockState prevState = level.getBlockState(position);
        level.setBlock(position, Blocks.JIGSAW.defaultBlockState(), 0);
        if (level.getBlockEntity(position) instanceof JigsawBlockEntity jbe) {
            jbe.setPool(pool);
            jbe.setTarget(target);
            jbe.generate(level, maxDepth, false);
            
            if (level.getBlockState(position).getBlock() != Blocks.JIGSAW) {
                source.sendSuccess(new TranslatableComponent("commands.place.jigsaw.success", position.getX(), position.getY(), position.getZ()), false);
                return 1;
            }
        }

        level.setBlock(position, prevState, 0);
        throw ERROR_JIGSAW_FAILED.create();
    }

    private static <T> ResourceKey<T> getRegistryType(CommandContext<CommandSourceStack> ctx, String name, ResourceKey<Registry<T>> key, DynamicCommandExceptionType exceptionType) throws CommandSyntaxException {
        ResourceKey<?> resourcekey = ctx.getArgument(name, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourcekey.cast(key);
        return optional.orElseThrow(() -> exceptionType.create(resourcekey));
    }

    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> ctx, ResourceKey<? extends Registry<T>> key) {
        return ctx.getSource().getServer().registryAccess().registryOrThrow(key);
    }

    private static <T> Holder<T> getRegistryKeyType(CommandContext<CommandSourceStack> ctx, String name, ResourceKey<Registry<T>> key, DynamicCommandExceptionType exceptionType) throws CommandSyntaxException {
        ResourceKey<T> resourcekey = getRegistryType(ctx, name, key, exceptionType);
        return getRegistry(ctx, key).getHolder(resourcekey).orElseThrow(() -> exceptionType.create(resourcekey.location()));
    }

    public static Holder<StructureTemplatePool> getStructureTemplatePool(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        return getRegistryKeyType(ctx, name, Registry.TEMPLATE_POOL_REGISTRY, ERROR_INVALID_TEMPLATE_POOL);
    }
}
