package dev.ftb.packcompanion.features.actionpad;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.packcompanion.core.utils.ExtraCodecs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public record PadAction(
        String name,
        Icon icon,
        Optional<String> unlockedAt,
        Optional<CommandAction> commandAction,
        Optional<TeleportAction> teleportAction,
        boolean autoclose
) {
    public static final Codec<PadAction> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(PadAction::name),
            Codec.STRING.fieldOf("icon").xmap(Icon::getIcon, Icon::toString).orElse(Icon.empty()).forGetter(PadAction::icon),
            Codec.STRING.optionalFieldOf("unlocked_at").forGetter(PadAction::unlockedAt),
            CommandAction.CODEC.optionalFieldOf("command_action").forGetter(PadAction::commandAction),
            TeleportAction.CODEC.optionalFieldOf("teleport_action").forGetter(PadAction::teleportAction),
            Codec.BOOL.optionalFieldOf("autoclose", true).forGetter(PadAction::autoclose)
    ).apply(builder, PadAction::new));

//    public static final StreamCodec<FriendlyByteBuf, PadAction> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.STRING_UTF8, PadAction::name,
//            Icon.STREAM_CODEC, PadAction::icon,
//            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), PadAction::unlockedAt,
//            ByteBufCodecs.optional(CommandAction.STREAM_CODEC), PadAction::commandAction,
//            ByteBufCodecs.optional(TeleportAction.STREAM_CODEC), PadAction::teleportAction,
//            ByteBufCodecs.BOOL, PadAction::autoclose,
//            PadAction::new
//    );

    public record CommandAction(String command, int executionLevel, boolean executeAsServer) implements ActionRunner {
        public static final Codec<CommandAction> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("command").forGetter(CommandAction::command),
                Codec.INT.optionalFieldOf("execution_level", Commands.LEVEL_GAMEMASTERS).forGetter(CommandAction::executionLevel),
                Codec.BOOL.optionalFieldOf("execute_as_server", false).forGetter(CommandAction::executeAsServer)
        ).apply(builder, CommandAction::new));

//        public static final StreamCodec<FriendlyByteBuf, CommandAction> STREAM_CODEC = StreamCodec.composite(
//                ByteBufCodecs.STRING_UTF8, CommandAction::command,
//                ByteBufCodecs.optional(ByteBufCodecs.INT).map(opt -> opt.orElse(Commands.LEVEL_GAMEMASTERS), Optional::of), CommandAction::executionLevel,
//                ByteBufCodecs.optional(ByteBufCodecs.BOOL).map(opt -> opt.orElse(false), Optional::of), CommandAction::executeAsServer,
//                CommandAction::new
//        );

        @Override
        public void run(ServerPlayer player) {
            var level = player.level();

            CommandSourceStack sourceStack;
            if (executeAsServer) {
                sourceStack = Objects.requireNonNull(level.getServer()).createCommandSourceStack().withPermission(executionLevel);
            } else {
                sourceStack = player.createCommandSourceStack().withPermission(executionLevel);
            }

            Objects.requireNonNull(level.getServer()).getCommands().performPrefixedCommand(
                    sourceStack,
                    command
            );
        }
    }

    public record TeleportAction(
            BlockPos position,
            ResourceKey<Level> dimension,
            Optional<Vector2f> rotation
    ) implements ActionRunner {
        private final static Logger LOGGER = LoggerFactory.getLogger(PadAction.class);

        public static final Codec<TeleportAction> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                BlockPos.CODEC.fieldOf("position").forGetter(TeleportAction::position),
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(TeleportAction::dimension),
                ExtraCodecs.VECTOR2F_CODEC.optionalFieldOf("rotation").forGetter(TeleportAction::rotation)
        ).apply(builder, TeleportAction::new));

//        public static final StreamCodec<FriendlyByteBuf, TeleportAction> STREAM_CODEC = StreamCodec.composite(
//                net.minecraft.core.BlockPos.STREAM_CODEC, TeleportAction::position,
//                ResourceKey.streamCodec(Registries.DIMENSION), TeleportAction::dimension,
//                ByteBufCodecs.optional(ExtraCodecs.VECTOR2F_STREAM_CODEC), TeleportAction::rotation,
//                TeleportAction::new
//        );

        @Override
        public void run(ServerPlayer player) {
            ServerLevel level = player.getServer().getLevel(dimension());
            if (level == null) {
                LOGGER.warn("Failed to teleport player {}: dimension {} not found", player.getName().getString(), dimension().location());
                return;
            }

            float yaw = rotation().map(e -> e.x).orElse(player.getYRot());
            float pitch = rotation().map(e -> e.y).orElse(player.getXRot());

            var pos = position();

            player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, RelativeMovement.ALL, yaw, pitch);
        }
    }

    @FunctionalInterface
    public interface ActionRunner {
        default ActionRunner asActionRunner() { return this; }
        void run(ServerPlayer player);
    }
}
