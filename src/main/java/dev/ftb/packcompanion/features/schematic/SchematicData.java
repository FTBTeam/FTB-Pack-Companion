package dev.ftb.packcompanion.features.schematic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class SchematicData {
    private final BlockState[][][] buffer;
    @Nullable
    private final Holder<Biome>[][][] biomeBuffer;
    private final int height, length, width;
    private final int biomeWidth, biomeHeight, biomeLength;
    private final Map<BlockPos, CompoundTag> blockEntityData = new HashMap<>();
    private int nonAirBlockCount = 0;

    public static SchematicData load(HolderLookup.Provider registries, CompoundTag tag) throws IOException {
        if (tag.contains("Schematic") && tag.getCompound("Schematic").getInt("Version") == 3) {
            return new SchematicData(registries, tag.getCompound("Schematic"), 3);
        } else if (tag.getInt("Version") == 2) {
            return new SchematicData(registries, tag, 2);
        } else {
            throw new IOException("Unknown schematic format");
        }
    }

    @SuppressWarnings("unchecked")
    private SchematicData(HolderLookup.Provider registries, CompoundTag tag, int version) throws IOException {
        HolderLookup.RegistryLookup<Block> blockLookup = registries.lookupOrThrow(Registries.BLOCK);
        HolderLookup.RegistryLookup<Biome> biomeLookup = registries.lookupOrThrow(Registries.BIOME);

        height = tag.getInt("Height");
        length = tag.getInt("Length");
        width = tag.getInt("Width");
        buffer = new BlockState[width][height][length];

        biomeWidth = (width + 3) >> 2;
        biomeHeight = (height + 3) >> 2;
        biomeLength = (length + 3) >> 2;

        if (version == 2) {
            BlockState[] statePalette = loadPalette(blockLookup, tag.getInt("PaletteMax"), tag.getCompound("Palette"));
            loadBlockData(tag.getByteArray("BlockData"), statePalette);
            loadBlockEntityData(tag.getList("BlockEntities", Tag.TAG_COMPOUND), "Extra");

            if (tag.contains("BiomePalette")) {
                biomeBuffer = new Holder[biomeWidth][biomeHeight][biomeLength];
                Holder<Biome>[] biomePalette = loadBiomePalette(biomeLookup, tag.getInt("BiomePaletteMax"), tag.getCompound("BiomePalette"));
                loadBiomeData(tag.getByteArray("BiomeData"), biomePalette);
            } else {
                biomeBuffer = null;
            }
        } else if (version == 3) {
            CompoundTag blockTag = tag.getCompound("Blocks");
            CompoundTag palette = blockTag.getCompound("Palette");
            BlockState[] statePalette = loadPalette(blockLookup, palette.size(), palette);
            loadBlockData(blockTag.getByteArray("Data"), statePalette);
            loadBlockEntityData(blockTag.getList("BlockEntities", Tag.TAG_COMPOUND), "Data");

            if (tag.contains("Biomes")) {
                biomeBuffer = new Holder[biomeWidth][biomeHeight][biomeLength];
                CompoundTag biomeTag = tag.getCompound("Biomes");
                CompoundTag biomePaletteTag = biomeTag.getCompound("Palette");
                Holder<Biome>[] biomePalette = loadBiomePalette(biomeLookup, biomePaletteTag.size(), biomePaletteTag);
                loadBiomeData(biomeTag.getByteArray("Data"), biomePalette);
            } else {
                biomeBuffer = null;
            }
        } else {
            biomeBuffer = null;
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

    public int getTotalBlockCount() {
        return width * length * height;
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

    public boolean hasBiomeData() {
        return biomeBuffer != null;
    }

    public int getBiomeWidth() {
        return biomeWidth;
    }

    public int getBiomeHeight() {
        return biomeHeight;
    }

    public int getBiomeLength() {
        return biomeLength;
    }

    @Nullable
    Holder<Biome> getBiomeAtCell(int cellX, int cellY, int cellZ) {
        if (biomeBuffer == null) return null;
        if (cellX < 0 || cellX >= biomeWidth || cellY < 0 || cellY >= biomeHeight || cellZ < 0 || cellZ >= biomeLength) return null;
        return biomeBuffer[cellX][cellY][cellZ];
    }

    @SuppressWarnings("unchecked")
    private Holder<Biome>[] loadBiomePalette(HolderLookup.RegistryLookup<Biome> biomeLookup, int paletteMax, CompoundTag palTag) {
        Holder<Biome>[] palette = new Holder[Math.max(paletteMax, palTag.size())];
        for (String key : palTag.getAllKeys()) {
            ResourceLocation biomeId = ResourceLocation.tryParse(key);
            int idx = palTag.getInt(key);
            if (biomeId != null && idx >= 0 && idx < palette.length) {
                biomeLookup.get(ResourceKey.create(Registries.BIOME, biomeId)).ifPresentOrElse(
                        holder -> palette[idx] = holder,
                        () -> SchematicPasteManager.LOGGER.warn("unknown biome '{}' in schematic palette", key)
                );
            }
        }
        return palette;
    }

    private void loadBiomeData(byte[] biomeData, Holder<Biome>[] palette) throws IOException {
        int index = 0;
        int i = 0;
        int xyArea = width * length;
        while (i < biomeData.length) {
            int value = 0;
            int varintLength = 0;
            while (true) {
                value |= (biomeData[i] & 127) << (varintLength++ * 7);
                if (varintLength > 5) throw new IOException("VarInt too big (probably corrupted biome data)");
                if ((biomeData[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            int y = index / xyArea;
            int z = (index % xyArea) / width;
            int x = (index % xyArea) % width;

            int bx = x >> 2, by = y >> 2, bz = z >> 2;
            if (biomeBuffer != null && biomeBuffer[bx][by][bz] == null
                    && value >= 0 && value < palette.length && palette[value] != null) {
                biomeBuffer[bx][by][bz] = palette[value];
            }
            index++;
        }
    }
}
