package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSlimyBubble;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

import java.util.Random;

public class BlockFluidDeath extends BlockFluidFinite {

   public BlockFluidDeath() {
      super(ConfigBlocks.FLUIDDEATH, Material.WATER);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setQuantaPerBlock(4);
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      if (!world.isRemote && entity instanceof EntityLivingBase) {
         int quanta = this.getQuantaValue(world, pos);
         entity.attackEntityFrom(DamageSourceThaumcraft.dissolve, (float)(quanta + 1));
      }
   }

   public int getQuanta() {
      return this.quantaPerBlock;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
      int meta = this.getQuantaValue(world, pos);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      float h = rand.nextFloat() * 0.075F;
      FXSlimyBubble ef = new FXSlimyBubble(world, (float)x + rand.nextFloat(), (float)y + 0.1F + 0.225F * (float)meta, (float)z + rand.nextFloat(), 0.075F + h);
      ef.setAlphaF(0.8F);
      ef.setRBGColorF(0.3F - rand.nextFloat() * 0.1F, 0.0F, 0.4F + rand.nextFloat() * 0.1F);
      ParticleEngine.instance.addEffect(world, ef);
      if (rand.nextInt(50) == 0) {
         world.playSound(null, pos,
               thaumcraft.common.lib.SoundsTC.get("liquid.lavapop"),
               SoundCategory.BLOCKS,
               0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F);
      }

      super.randomDisplayTick(state, world, pos, rand);
   }
}
