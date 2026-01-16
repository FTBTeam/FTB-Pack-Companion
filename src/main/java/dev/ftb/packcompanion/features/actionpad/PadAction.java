package dev.ftb.packcompanion.features.actionpad;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.packcompanion.core.utils.ExtraCodecs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
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

    public static PadAction decode(FriendlyByteBuf buffer) {
        String name = buffer.readUtf();
        Icon icon = Icon.getIcon(buffer.readUtf());
        boolean autoclose = buffer.readBoolean();

        // Read the flag to see which optional fields are present
        var readerFlag = buffer.readByte();

        Optional<String> unlockedAt = (readerFlag & 1) != 0 ? Optional.of(buffer.readUtf()) : Optional.empty();
        Optional<CommandAction> commandAction = (readerFlag & 2) != 0 ? Optional.of(CommandAction.decode(buffer)) : Optional.empty();
        Optional<TeleportAction> teleportAction = (readerFlag & 4) != 0 ? Optional.of(TeleportAction.decode(buffer)) : Optional.empty();

        return new PadAction(name, icon, unlockedAt, commandAction, teleportAction, autoclose);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(name);
        buffer.writeUtf(icon.toString());
        buffer.writeBoolean(autoclose);

        // Bump the flag so we know which optional fields are present
        var readerFlag = 0;
        if (unlockedAt.isPresent()) readerFlag |= 1;
        if (commandAction.isPresent()) readerFlag |= 2;
        if (teleportAction.isPresent()) readerFlag |= 4;
        buffer.writeByte(readerFlag);

        unlockedAt.ifPresent(buffer::writeUtf);
        commandAction.ifPresent(action -> action.encode(buffer));
        teleportAction.ifPresent(action -> action.encode(buffer));
    }

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

        public static CommandAction decode(FriendlyByteBuf buffer) {
            String command = buffer.readUtf();
            int executionLevel = buffer.readInt();
            boolean executeAsServer = buffer.readBoolean();

            return new CommandAction(command, executionLevel, executeAsServer);
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUtf(command);
            buffer.writeInt(executionLevel);
            buffer.writeBoolean(executeAsServer);
        }

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

        public static TeleportAction decode(FriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            ResourceKey<Level> dimension = buffer.readResourceKey(Registries.DIMENSION);
            Optional<Vector2f> rotation;
            if (buffer.readBoolean()) {
                float yaw = buffer.readFloat();
                float pitch = buffer.readFloat();
                rotation = Optional.of(new Vector2f(yaw, pitch));
            } else {
                rotation = Optional.empty();
            }
            return new TeleportAction(pos, dimension, rotation);
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(BlockPos.ZERO);
            buffer.writeResourceKey(dimension);
            if (rotation.isPresent()) {
                buffer.writeBoolean(true);
                buffer.writeFloat(rotation.get().x);
                buffer.writeFloat(rotation.get().y);
            } else {
                buffer.writeBoolean(false);
            }
        }

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
