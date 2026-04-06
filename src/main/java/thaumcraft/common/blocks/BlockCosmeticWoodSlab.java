package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

import java.util.Random;

public class BlockCosmeticWoodSlab extends BlockSlab {

   public static final PropertyEnum<BlockCosmeticWoodSlab.EnumType> VARIANT =
         PropertyEnum.create("variant", BlockCosmeticWoodSlab.EnumType.class);

   private final boolean doubleSlab;

   public BlockCosmeticWoodSlab(boolean doubleSlab) {
      super(Material.WOOD);
      this.doubleSlab = doubleSlab;
      IBlockState state = this.blockState.getBaseState();
      if (!doubleSlab) {
         state = state.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      }
      this.setDefaultState(state.withProperty(VARIANT, EnumType.GREATWOOD));
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setLightOpacity(0);
   }

   @Override
   public boolean isDouble() {
      return doubleSlab;
   }

   @Override
   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @Override
   public Comparable<?> getTypeForItem(ItemStack stack) {
      return EnumType.byMetadata(stack.getMetadata() & 7);
   }

   @Override
   public String getTranslationKey(int meta) {
      return super.getTranslationKey() + "." + EnumType.byMetadata(meta).getName();
   }

   @Override
   public Item getItemDropped(IBlockState state, Random random, int fortune) {
      return Item.getItemFromBlock(ConfigBlocks.blockSlabWood);
   }

   @Override
   public int damageDropped(IBlockState state) {
      return ((EnumType) state.getValue(VARIANT)).getMetadata();
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      if (this != ConfigBlocks.blockDoubleSlabWood) {
         for (EnumType type : EnumType.values()) {
            list.add(new ItemStack(this, 1, type.getMetadata()));
         }
      }
   }

   @Override
   public IBlockState getStateFromMeta(int meta) {
      IBlockState state = this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta & 7));
      if (!isDouble()) {
         state = state.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
      }
      return state;
   }

   @Override
   public int getMetaFromState(IBlockState state) {
      int meta = ((EnumType) state.getValue(VARIANT)).getMetadata();
      if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
         meta |= 8;
      }
      return meta;
   }

   @Override
   protected BlockStateContainer createBlockState() {
      return isDouble()
            ? new BlockStateContainer(this, VARIANT)
            : new BlockStateContainer(this, HALF, VARIANT);
   }

   public enum EnumType implements IStringSerializable {
      GREATWOOD(0, "greatwood"),
      SILVERWOOD(1, "silverwood");

      private final int meta;
      private final String name;

      EnumType(int meta, String name) {
         this.meta = meta;
         this.name = name;
      }

      public int getMetadata() { return meta; }

      @Override
      public String getName() { return name; }

      @Override
      public String toString() { return name; }

      public static EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= values().length) meta = 0;
         return values()[meta];
      }
   }
}
