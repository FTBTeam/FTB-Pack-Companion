package dev.ftb.packcompanion.features.jigsaw;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;

/**
 * Pretty much a copy of JigsawPlacement, with the hardcoded 80 size replaced by MAX_SIZE (256)
 */
public class CustomJigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_SIZE = 256;

    public static Optional<PieceGenerator<JigsawConfiguration>> addPieces(PieceGeneratorSupplier.Context<JigsawConfiguration> context, JigsawPlacement.PieceFactory pieceFactory, BlockPos startPos, boolean expansionHack, boolean projectStartToHeightmap) {
        WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(0L));
        rand.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        RegistryAccess registryAccess = context.registryAccess();
        JigsawConfiguration jigsawConfiguration = context.config();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        StructureManager structureManager = context.structureManager();
        LevelHeightAccessor levelHeightAccessor = context.heightAccessor();
        Predicate<Holder<Biome>> predicate = context.validBiome();
        StructureFeature.bootstrap();
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Rotation rotation = Rotation.getRandom(rand);
        StructureTemplatePool structureTemplatePool = jigsawConfiguration.startPool().value();
        StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(rand);
        
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        } else {
            PoolElementStructurePiece poolElementStructurePiece = pieceFactory.create(structureManager, structurePoolElement, startPos, structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureManager, startPos, rotation));
            BoundingBox boundingbox = poolElementStructurePiece.getBoundingBox();
            int startX = (boundingbox.maxX() + boundingbox.minX()) / 2;
            int startZ = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
            int startY;
            if (projectStartToHeightmap) {
                startY = startPos.getY() + chunkGenerator.getFirstFreeHeight(startX, startZ, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
            } else {
                startY = startPos.getY();
            }

            if (!predicate.test(chunkGenerator.getNoiseBiome(QuartPos.fromBlock(startX), QuartPos.fromBlock(startY), QuartPos.fromBlock(startZ)))) {
                return Optional.empty();
            } else {
                int yOff = boundingbox.minY() + poolElementStructurePiece.getGroundLevelDelta();
                poolElementStructurePiece.move(0, startY - yOff, 0);
                return Optional.of((builder, configurationContext) -> {
                    List<PoolElementStructurePiece> list = Lists.newArrayList();
                    list.add(poolElementStructurePiece);
                    if (jigsawConfiguration.maxDepth() > 0) {

                        AABB aabb = new AABB(startX - MAX_SIZE, startY - MAX_SIZE, startZ - MAX_SIZE, startX + MAX_SIZE + 1, startY + MAX_SIZE + 1, startZ + MAX_SIZE + 1);
                        Placer placer = new Placer(registry, jigsawConfiguration.maxDepth(), pieceFactory, chunkGenerator, structureManager, list, rand);
                        placer.placing.addLast(new PieceState(poolElementStructurePiece, new MutableObject<>(Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)), 0));

                        while (!placer.placing.isEmpty()) {
                            PieceState pieceState = placer.placing.removeFirst();
                            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, expansionHack, levelHeightAccessor);
                        }

                        list.forEach(builder::addPiece);
                    }
                });
            }
        }
    }

    record PieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth) {
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final JigsawPlacement.PieceFactory factory;
        private final ChunkGenerator chunkGenerator;
        private final StructureManager structureManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final Random random;
        final Deque<PieceState> placing = Queues.newArrayDeque();

        Placer(Registry<StructureTemplatePool> p_210323_, int p_210324_, JigsawPlacement.PieceFactory p_210325_, ChunkGenerator p_210326_, StructureManager p_210327_, List<? super PoolElementStructurePiece> p_210328_, Random p_210329_) {
            this.pools = p_210323_;
            this.maxDepth = p_210324_;
            this.factory = p_210325_;
            this.chunkGenerator = p_210326_;
            this.structureManager = p_210327_;
            this.pieces = p_210328_;
            this.random = p_210329_;
        }

        void tryPlacingChildren(PoolElementStructurePiece structurePiece, MutableObject<VoxelShape> freeSpace, int depth, boolean expansionHack, LevelHeightAccessor heightAccessor) {
            StructurePoolElement structurepoolelement = structurePiece.getElement();
            BlockPos blockpos = structurePiece.getPosition();
            Rotation rotation = structurePiece.getRotation();
            StructureTemplatePool.Projection structuretemplatepool$projection = structurepoolelement.getProjection();
            boolean flag = structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableobject = new MutableObject<>();
            BoundingBox boundingbox = structurePiece.getBoundingBox();
            int i = boundingbox.minY();

            label139:
            for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : structurepoolelement.getShuffledJigsawBlocks(this.structureManager, blockpos, rotation, this.random)) {
                Direction direction = JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state);
                BlockPos blockpos1 = structuretemplate$structureblockinfo.pos;
                BlockPos blockpos2 = blockpos1.relative(direction);
                int j = blockpos1.getY() - i;
                int k = -1;
                ResourceLocation resourcelocation = new ResourceLocation(structuretemplate$structureblockinfo.nbt.getString("pool"));
                Optional<StructureTemplatePool> optional = this.pools.getOptional(resourcelocation);
                if (optional.isPresent() && (optional.get().size() != 0 || Objects.equals(resourcelocation, Pools.EMPTY.location()))) {
                    ResourceLocation resourcelocation1 = optional.get().getFallback();
                    Optional<StructureTemplatePool> optional1 = this.pools.getOptional(resourcelocation1);
                    if (optional1.isPresent() && (optional1.get().size() != 0 || Objects.equals(resourcelocation1, Pools.EMPTY.location()))) {
                        boolean flag1 = boundingbox.isInside(blockpos2);
                        MutableObject<VoxelShape> mutableobject1;
                        if (flag1) {
                            mutableobject1 = mutableobject;
                            if (mutableobject.getValue() == null) {
                                mutableobject.setValue(Shapes.create(AABB.of(boundingbox)));
                            }
                        } else {
                            mutableobject1 = freeSpace;
                        }

                        List<StructurePoolElement> list = Lists.newArrayList();
                        if (depth != this.maxDepth) {
                            list.addAll(optional.get().getShuffledTemplates(this.random));
                        }

                        list.addAll(optional1.get().getShuffledTemplates(this.random));

                        for(StructurePoolElement structurepoolelement1 : list) {
                            if (structurepoolelement1 == EmptyPoolElement.INSTANCE) {
                                break;
                            }

                            for(Rotation rotation1 : Rotation.getShuffled(this.random)) {
                                List<StructureTemplate.StructureBlockInfo> list1 = structurepoolelement1.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation1, this.random);
                                BoundingBox boundingbox1 = structurepoolelement1.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation1);
                                int l;
                                if (expansionHack && boundingbox1.getYSpan() <= 16) {
                                    l = list1.stream().mapToInt((p_210332_) -> {
                                        if (!boundingbox1.isInside(p_210332_.pos.relative(JigsawBlock.getFrontFacing(p_210332_.state)))) {
                                            return 0;
                                        } else {
                                            ResourceLocation resourcelocation2 = new ResourceLocation(p_210332_.nbt.getString("pool"));
                                            Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourcelocation2);
                                            Optional<StructureTemplatePool> optional3 = optional2.flatMap((p_210344_) -> {
                                                return this.pools.getOptional(p_210344_.getFallback());
                                            });
                                            int j3 = optional2.map((p_210342_) -> {
                                                return p_210342_.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            int k3 = optional3.map((p_210340_) -> {
                                                return p_210340_.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            return Math.max(j3, k3);
                                        }
                                    }).max().orElse(0);
                                } else {
                                    l = 0;
                                }

                                for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : list1) {
                                    if (JigsawBlock.canAttach(structuretemplate$structureblockinfo, structuretemplate$structureblockinfo1)) {
                                        BlockPos blockpos3 = structuretemplate$structureblockinfo1.pos;
                                        BlockPos blockpos4 = blockpos2.subtract(blockpos3);
                                        BoundingBox boundingbox2 = structurepoolelement1.getBoundingBox(this.structureManager, blockpos4, rotation1);
                                        int i1 = boundingbox2.minY();
                                        StructureTemplatePool.Projection structuretemplatepool$projection1 = structurepoolelement1.getProjection();
                                        boolean flag2 = structuretemplatepool$projection1 == StructureTemplatePool.Projection.RIGID;
                                        int j1 = blockpos3.getY();
                                        int k1 = j - j1 + JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state).getStepY();
                                        int l1;
                                        if (flag && flag2) {
                                            l1 = i + k1;
                                        } else {
                                            if (k == -1) {
                                                k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor);
                                            }

                                            l1 = k - j1;
                                        }

                                        int i2 = l1 - i1;
                                        BoundingBox boundingbox3 = boundingbox2.moved(0, i2, 0);
                                        BlockPos blockpos5 = blockpos4.offset(0, i2, 0);
                                        if (l > 0) {
                                            int j2 = Math.max(l + 1, boundingbox3.maxY() - boundingbox3.minY());
                                            boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(), boundingbox3.minY() + j2, boundingbox3.minZ()));
                                        }

                                        if (!Shapes.joinIsNotEmpty(mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3).deflate(0.25D)), BooleanOp.ONLY_SECOND)) {
                                            mutableobject1.setValue(Shapes.joinUnoptimized(mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST));
                                            int i3 = structurePiece.getGroundLevelDelta();
                                            int k2;
                                            if (flag2) {
                                                k2 = i3 - k1;
                                            } else {
                                                k2 = structurepoolelement1.getGroundLevelDelta();
                                            }

                                            PoolElementStructurePiece poolelementstructurepiece = this.factory.create(this.structureManager, structurepoolelement1, blockpos5, k2, rotation1, boundingbox3);
                                            int l2;
                                            if (flag) {
                                                l2 = i + j;
                                            } else if (flag2) {
                                                l2 = l1 + j1;
                                            } else {
                                                if (k == -1) {
                                                    k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor);
                                                }

                                                l2 = k + k1 / 2;
                                            }

                                            structurePiece.addJunction(new JigsawJunction(blockpos2.getX(), l2 - j + i3, blockpos2.getZ(), k1, structuretemplatepool$projection1));
                                            poolelementstructurepiece.addJunction(new JigsawJunction(blockpos1.getX(), l2 - j1 + k2, blockpos1.getZ(), -k1, structuretemplatepool$projection));
                                            this.pieces.add(poolelementstructurepiece);
                                            if (depth + 1 <= this.maxDepth) {
                                                this.placing.addLast(new PieceState(poolelementstructurepiece, mutableobject1, depth + 1));
                                            }
                                            continue label139;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)resourcelocation1);
                    }
                } else {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourcelocation);
                }
            }

        }
    }
}
