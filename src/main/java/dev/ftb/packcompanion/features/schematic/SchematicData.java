package dev.ftb.packcompanion.features.schematic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class SchematicData {
    private final BlockState[][][] buffer;
    private final int height, length, width;
    private final Map<BlockPos, CompoundTag> blockEntityData = new HashMap<>();

    public static SchematicData load(HolderLookup.RegistryLookup<Block> lookup, CompoundTag tag) throws IOException {
        Optional<CompoundTag> schematic = tag.getCompound("Schematic");
        if (schematic.isPresent() && schematic.get().getIntOr("Version", -1) == 3) {
            return new SchematicData(lookup, schematic.get(), 3);
        } else if (tag.getIntOr("Version", -1) == 2) {
            return new SchematicData(lookup, tag, 2);
        } else {
            throw new IOException("Unknown schematic format");
        }
    }

    private SchematicData(HolderLookup.RegistryLookup<Block> lookup, CompoundTag tag, int version) throws IOException {
        height = tag.getInt("Height").orElseThrow();
        length = tag.getInt("Length").orElseThrow();
        width = tag.getInt("Width").orElseThrow();
        buffer = new BlockState[width][height][length];

        if (version == 2) {
            BlockState[] statePalette = loadPalette(lookup, tag.getInt("PaletteMax").orElseThrow(), tag.getCompound("Palette").orElseThrow());
            loadBlockData(tag.getByteArray("BlockData").orElseThrow(), statePalette);
            loadBlockEntityData(tag.getListOrEmpty("BlockEntities"), "Extra");
        } else if (version == 3) {
            CompoundTag blockTag = tag.getCompound("Blocks").orElseThrow();
            CompoundTag palette = blockTag.getCompound("Palette").orElseThrow();
            BlockState[] statePalette = loadPalette(lookup, palette.size(), palette);
            loadBlockData(blockTag.getByteArray("Data").orElseThrow(), statePalette);
            loadBlockEntityData(blockTag.getListOrEmpty("BlockEntities"), "Data");
        } else {
            throw new IOException("Unsupported schematic version " + version);
        }
    }

    BlockState getBlockAt(BlockPos pos, BlockState def) {
        return Objects.requireNonNullElse(buffer[pos.getX()][pos.getY()][pos.getZ()], def);
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    private void loadBlockEntityData(ListTag tag, String dataKey) {
        for (int i = 0; i < tag.size(); i++) {
            Optional<CompoundTag> el = tag.getCompound(i);
            if (el.isEmpty()) {
                continue;
            }

            var posTag = el.get().getIntArray("Pos");
            if (posTag.isPresent() && posTag.get().length == 3) {
                var pos = posTag.get();
                blockEntityData.put(new BlockPos(pos[0], pos[1], pos[2]), el.get().getCompoundOrEmpty(dataKey));
            }
        }
    }

    private BlockState[] loadPalette(HolderLookup.RegistryLookup<Block> lookup, int paletteMax, CompoundTag palTag) {
        BlockState[] statePalette = new BlockState[paletteMax];
        for (Map.Entry<String, Tag> entry : palTag.entrySet()) {
            var key = entry.getKey();
            try {
                var state = BlockStateParser.parseForBlock(lookup, key, false).blockState();
                statePalette[palTag.getIntOr(key, 0)] = state;
            } catch (CommandSyntaxException e) {
                SchematicPasteManager.LOGGER.error("invalid blockstate {}", key);
                statePalette[palTag.getIntOr(key, 0)] = Blocks.AIR.defaultBlockState();
            } catch (IllegalArgumentException e) {
                SchematicPasteManager.LOGGER.error("invalid palette index {}", palTag.getIntOr(key, 0));
                statePalette[palTag.getIntOr(key, 0)] = Blocks.AIR.defaultBlockState();
            }
        }
        return statePalette;
    }

    private void loadBlockData(byte[] blockdata, BlockState[] palette) throws IOException {
        int index = 0;
        int i = 0;
        int value;
        int varint_length;
        int xyArea = width * length;
        while (i < blockdata.length) {
            value = 0;
            varint_length = 0;

            while (true) {
                value |= (blockdata[i] & 127) << (varint_length++ * 7);
                if (varint_length > 5) {
                    throw new IOException("VarInt too big (probably corrupted data)");
                }
                if ((blockdata[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length + z) * width + x
            int y = index / xyArea;
            int z = (index % xyArea) / width;
            int x = (index % xyArea) % width;
            BlockState state = palette[value];
            buffer[x][y][z] = state;

            index++;
        }
        if (index != width * height * length) {
            throw new IOException(String.format("invalid blockdata length! %d != %d * %d * %d", index, width, length, height));
        }
    }

    public Optional<CompoundTag> getBlockEntityDataAt(BlockPos pos) {
        return Optional.ofNullable(blockEntityData.get(pos));
    }
}
