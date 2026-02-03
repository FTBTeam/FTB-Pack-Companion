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
    private int nonAirBlockCount = 0;

    public static SchematicData load(HolderLookup.RegistryLookup<Block> lookup, CompoundTag tag) throws IOException {
        if (tag.contains("Schematic") && tag.getCompound("Schematic").getInt("Version") == 3) {
            return new SchematicData(lookup, tag.getCompound("Schematic"), 3);
        } else if (tag.getInt("Version") == 2) {
            return new SchematicData(lookup, tag, 2);
        } else {
            throw new IOException("Unknown schematic format");
        }
    }

    private SchematicData(HolderLookup.RegistryLookup<Block> lookup, CompoundTag tag, int version) throws IOException {
        height = tag.getInt("Height");
        length = tag.getInt("Length");
        width = tag.getInt("Width");
        buffer = new BlockState[width][height][length];

        if (version == 2) {
            BlockState[] statePalette = loadPalette(lookup, tag.getInt("PaletteMax"), tag.getCompound("Palette"));
            loadBlockData(tag.getByteArray("BlockData"), statePalette);
            loadBlockEntityData(tag.getList("BlockEntities", Tag.TAG_COMPOUND), "Extra");
        } else if (version == 3) {
            CompoundTag blockTag = tag.getCompound("Blocks");
            CompoundTag palette = blockTag.getCompound("Palette");
            BlockState[] statePalette = loadPalette(lookup, palette.size(), palette);
            loadBlockData(blockTag.getByteArray("Data"), statePalette);
            loadBlockEntityData(blockTag.getList("BlockEntities", Tag.TAG_COMPOUND), "Data");
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

    public int getNonAirBlockCount() {
        return nonAirBlockCount;
    }

    private void loadBlockEntityData(ListTag tag, String dataKey) {
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag el = tag.getCompound(i);
            var posTag = el.getIntArray("Pos");
            if (posTag.length == 3) {
                blockEntityData.put(new BlockPos(posTag[0], posTag[1], posTag[2]), el.getCompound(dataKey));
            }
        }
    }

    private BlockState[] loadPalette(HolderLookup.RegistryLookup<Block> lookup, int paletteMax, CompoundTag palTag) {
        BlockState[] statePalette = new BlockState[paletteMax];
        for (String key : palTag.getAllKeys()) {
            try {
                var state = BlockStateParser.parseForBlock(lookup, key, false).blockState();
                statePalette[palTag.getInt(key)] = state;
            } catch (CommandSyntaxException e) {
                SchematicPasteManager.LOGGER.error("invalid blockstate {}", key);
                statePalette[palTag.getInt(key)] = Blocks.AIR.defaultBlockState();
            } catch (IllegalArgumentException e) {
                SchematicPasteManager.LOGGER.error("invalid palette index {}", palTag.getInt(key));
                statePalette[palTag.getInt(key)] = Blocks.AIR.defaultBlockState();
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
            if (!state.isAir()) {
                nonAirBlockCount++;
            }

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
