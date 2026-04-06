package thaumcraft.common.lib.world;

import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import thaumcraft.common.config.ConfigEntities;

public class ComponentBankerHome extends StructureVillagePieces.Village {
   private boolean isTallHouse;
   private int tablePosition;

   public ComponentBankerHome() {
   }

   public ComponentBankerHome(StructureVillagePieces.Start startPiece, int generationDepth, Random random, StructureBoundingBox box, int facing) {
      super(startPiece, generationDepth);
      this.setCoordBaseMode(EnumFacing.byIndex(facing));
      this.boundingBox = box;
      this.isTallHouse = random.nextBoolean();
      this.tablePosition = random.nextInt(3);
   }

   protected void writeStructureToNBT(NBTTagCompound tagCompound) {
      super.writeStructureToNBT(tagCompound);
      tagCompound.setInteger("T", this.tablePosition);
      tagCompound.setBoolean("C", this.isTallHouse);
   }

   protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
      super.readStructureFromNBT(tagCompound, p_143011_2_);
      this.tablePosition = tagCompound.getInteger("T");
      this.isTallHouse = tagCompound.getBoolean("C");
   }

   public static Object buildComponent(StructureVillagePieces.Start startPiece, List pieces, Random random, int x, int y, int z, int facing, int generationDepth) {
      StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 4, 6, 5, EnumFacing.byIndex(facing));
      return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(pieces, structureboundingbox) == null ? new ComponentBankerHome(startPiece, generationDepth, random, structureboundingbox, facing) : null;
   }

   public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
      if (this.averageGroundLvl < 0) {
         this.averageGroundLvl = this.getAverageGroundLevel(worldIn, structureBoundingBoxIn);
         if (this.averageGroundLvl < 0) {
            return true;
         }

         this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 6 - 1, 0);
      }

      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 1, 3, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 3, 0, 4, Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 0, 1, 2, 0, 3, Blocks.DIRT.getDefaultState(), Blocks.DIRT.getDefaultState(), false);
      if (this.isTallHouse) {
         this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 4, 1, 2, 4, 3, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
      } else {
         this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 5, 1, 2, 5, 3, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
      }

      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 1, 4, 0, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 2, 4, 0, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 1, 4, 4, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 2, 4, 4, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 0, 4, 1, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 0, 4, 2, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 0, 4, 3, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 3, 4, 1, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 3, 4, 2, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.LOG.getDefaultState(), 3, 4, 3, structureBoundingBoxIn);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 0, 0, 3, 0, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 0, 3, 3, 0, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 4, 0, 3, 4, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 4, 3, 3, 4, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 3, 1, 1, 3, 3, 3, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 0, 2, 3, 0, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
      this.fillWithBlocks(worldIn, structureBoundingBoxIn, 1, 1, 4, 2, 3, 4, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
      this.setBlockState(worldIn, Blocks.IRON_BARS.getDefaultState(), 0, 2, 2, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.IRON_BARS.getDefaultState(), 3, 2, 2, structureBoundingBoxIn);
      if (this.tablePosition > 0) {
         this.setBlockState(worldIn, Blocks.COBBLESTONE_WALL.getDefaultState(), this.tablePosition, 1, 3, structureBoundingBoxIn);
         this.setBlockState(worldIn, Blocks.STONE_PRESSURE_PLATE.getDefaultState(), this.tablePosition, 2, 3, structureBoundingBoxIn);
      }

      this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 1, 1, 0, structureBoundingBoxIn);
      this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), 1, 2, 0, structureBoundingBoxIn);
      this.createVillageDoor(worldIn, structureBoundingBoxIn, randomIn, 1, 1, 0, EnumFacing.NORTH);
      if (this.getBlockStateFromPos(worldIn, 1, 0, -1, structureBoundingBoxIn).getMaterial() == Material.AIR && this.getBlockStateFromPos(worldIn, 1, -1, -1, structureBoundingBoxIn).getMaterial() != Material.AIR) {
         this.setBlockState(worldIn, Blocks.STONE_STAIRS.getDefaultState(), 1, 0, -1, structureBoundingBoxIn);
      }

      for(int i = 0; i < 5; ++i) {
         for(int j = 0; j < 4; ++j) {
            this.clearCurrentPositionBlocksUpwards(worldIn, j, 6, i, structureBoundingBoxIn);
            this.replaceAirAndLiquidDownwards(worldIn, Blocks.COBBLESTONE.getDefaultState(), j, -1, i, structureBoundingBoxIn);
         }
      }

      this.spawnVillagers(worldIn, structureBoundingBoxIn, 1, 1, 2, 1);
      return true;
   }

   protected int getVillagerType(int par1) {
      return ConfigEntities.entBankerId;
   }
}
