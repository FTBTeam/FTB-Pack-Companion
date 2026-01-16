package dev.ftb.packcompanion.features.structureplacer.network;

import dev.ftb.packcompanion.PackCompanion;
import dev.ftb.packcompanion.features.structureplacer.PlacerItem;
import dev.ftb.packcompanion.features.structureplacer.ProcessedStructureTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

// From server to client, handled on the client
public record ProvideStructurePacket(
        Optional<CompoundTag> structure
) implements CustomPacketPayload {
    public static final Type<ProvideStructurePacket> TYPE = new Type<>(PackCompanion.id("provide_structure"));

    public static final StreamCodec<FriendlyByteBuf, ProvideStructurePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG), ProvideStructurePacket::structure,
        ProvideStructurePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ProvideStructurePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.structure().isEmpty()) {
                System.out.println("Received empty structure in ProvideStructurePacket");
                return;
            }

            var player = context.player();
            var itemInSlot = player.getMainHandItem();
            if (!(itemInSlot.getItem() instanceof PlacerItem placerItem)) {
                return; // ffs. stop changing slots you bit**
            }

            var parsedStructure = new StructureTemplate();
            parsedStructure.load(BuiltInRegistries.BLOCK.asLookup(), packet.structure().get());

            placerItem.setStructure(new ProcessedStructureTemplate(parsedStructure));
        });
    }
}
