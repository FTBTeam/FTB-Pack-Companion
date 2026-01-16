package dev.ftb.packcompanion.features.schematic;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SchematicCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("schematic")
                .requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("paste")
                        .then(Commands.argument("schematic", ResourceLocationArgument.id())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("blocks_per_tick", IntegerArgumentType.integer(1))
                                                .executes(ctx -> doPaste(ctx,
                                                        ResourceLocationArgument.getId(ctx, "schematic"),
                                                        BlockPosArgument.getBlockPos(ctx, "pos"),
                                                        IntegerArgumentType.getInteger(ctx, "blocks_per_tick"))
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("list")
                        .executes(SchematicCommand::listPastes)
                )
                .then(Commands.literal("cancel")
                        .then(Commands.argument("dimId", ResourceLocationArgument.id())
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .executes(ctx -> cancelPaste(ctx, ResourceLocationArgument.getId(ctx, "dimId"), ResourceLocationArgument.getId(ctx, "id")))
                                )
                        )
                );
    }

    private static int doPaste(CommandContext<CommandSourceStack> ctx, ResourceLocation schematic, BlockPos pos, int blocksPerTick) {
        SchematicPasteManager.getInstance(ctx.getSource().getServer())
                .startPaste(ctx.getSource(), schematic, pos, blocksPerTick);

        ctx.getSource().sendSuccess(() -> Component.literal("Schematic paste started for " + schematic + " @ " + pos), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Paste speed: " + blocksPerTick + " blocks/tick"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int listPastes(CommandContext<CommandSourceStack> ctx) {
        List<Component> output = new ArrayList<>();
        SchematicPasteManager.getInstance(ctx.getSource().getServer()).list().forEach(pair -> {
            ResourceLocation id = pair.getFirst();
            SchematicPasteWorker worker = pair.getSecond();
            MutableComponent c = Component.empty().append(Component.literal(id.toString()).append(": ")
                    .append(Component.literal(worker.getState().toString()).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.YELLOW));
            if (worker.getState() == SchematicPasteWorker.State.PASTING) {
                c.append(" ").append(String.valueOf(worker.getProgress())).append("%");
            }
            c.append(" ").append(makeCommandClicky("gui.cancel", ChatFormatting.GOLD,
                    "/ftbpc schematic cancel " + worker.getDimensionId() + " " + id));
            output.add(c);
        });
        if (output.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No schematics in progress"), false);
        } else {
            output.forEach(c -> ctx.getSource().sendSuccess(() -> c, false));
        }
        return output.size();
    }

    private static int cancelPaste(CommandContext<CommandSourceStack> ctx, ResourceLocation dimId, ResourceLocation id) {
        if (SchematicPasteManager.getInstance(ctx.getSource().getServer()).cancelPaste(dimId, id)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Cancelled paste job: " + dimId + " / " + id), false);
            return Command.SINGLE_SUCCESS;
        } else {
            ctx.getSource().sendFailure(Component.literal("Could not cancel paste job: " + dimId + " / " + id));
            return 0;
        }
    }

    public static Component makeCommandClicky(String translationKey, ChatFormatting color, String command) {
        return makeCommandClicky(translationKey, color, command, false);
    }

    public static Component makeCommandClicky(String translationKey, ChatFormatting color, String command, boolean suggestOnly) {
        ClickEvent.Action action = suggestOnly ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND;
        return Component.literal("[")
                .append(Component.translatable(translationKey)
                        .withStyle(Style.EMPTY.withColor(color)
                                .withClickEvent(new ClickEvent(action, command))))
                .append("]");
    }
}
