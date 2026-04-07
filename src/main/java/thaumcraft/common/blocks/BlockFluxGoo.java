package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXBubble;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.EntityThaumicSlime;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

import java.util.Random;

public class BlockFluxGoo extends BlockFluidFinite {

   public BlockFluxGoo() {
      super(ConfigBlocks.FLUXGOO, Config.fluxGoomaterial);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public int getQuanta() {
      return this.quantaPerBlock;
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      // Quanta level (0..quantaPerBlock-1) maps to the old metadata value
      int md = this.getQuantaValue(world, pos);
      if (entity instanceof EntityThaumicSlime) {
         EntityThaumicSlime slime = (EntityThaumicSlime) entity;
         if (slime.getSlimeSize() < md && world.rand.nextBoolean()) {
            slime.setSlimeSize(slime.getSlimeSize() + 1);
            if (md > 1) {
               world.setBlockState(pos, state.withProperty(LEVEL, md - 1), 3);
            } else {
               world.setBlockToAir(pos);
            }
         }
      } else {
         entity.motionX *= 1.0F - this.getQuantaPercentage(world, pos);
         entity.motionZ *= 1.0F - this.getQuantaPercentage(world, pos);
         if (entity instanceof EntityLivingBase) {
            PotionEffect pe = new PotionEffect(Potion.getPotionById(Config.potionVisExhaustID), 600, md / 3, true, false);
            pe.getCurativeItems().clear();
            ((EntityLivingBase) entity).addPotionEffect(pe);
         }
      }
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
      int meta = this.getQuantaValue(world, pos);
      if (rand.nextInt(50 - Thaumcraft.proxy.particleCount(10)) <= meta) {
         FXBubble fb = new FXBubble(world,
               (float) pos.getX() + rand.nextFloat(),
               (float) pos.getY() + 0.125F * (float) meta,
               (float) pos.getZ() + rand.nextFloat(),
               0.0F, 0.0F, 0.0F, 0);
         fb.setAlphaF(0.25F);
         ParticleEngine.instance.addEffect(world, fb);
      }
      super.randomDisplayTick(state, world, pos, rand);
   }

   @Override
   public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
      super.updateTick(world, pos, state, rand);
      int meta = this.getQuantaValue(world, pos);
      if (meta >= 2 && meta < 6 && world.isAirBlock(pos.up()) && rand.nextInt(25) == 0) {
         world.setBlockToAir(pos);
         EntityThaumicSlime slime = new EntityThaumicSlime(world);
         slime.setLocationAndAngles(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, 0.0F, 0.0F);
         slime.setSlimeSize(1);
         world.spawnEntity(slime);
         world.playSound(null, slime.posX, slime.posY, slime.posZ,
               thaumcraft.common.lib.SoundsTC.get("thaumcraft:gore"),
               net.minecraft.util.SoundCategory.HOSTILE, 1.0F, 1.0F);
      } else if (meta >= 6 && world.isAirBlock(pos.up())) {
         if (rand.nextInt(25) == 0) {
            world.setBlockToAir(pos);
            EntityThaumicSlime slime = new EntityThaumicSlime(world);
            slime.setLocationAndAngles(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, 0.0F, 0.0F);
            slime.setSlimeSize(2);
            world.spawnEntity(slime);
            world.playSound(null, slime.posX, slime.posY, slime.posZ,
                  thaumcraft.common.lib.SoundsTC.get("thaumcraft:gore"),
                  net.minecraft.util.SoundCategory.HOSTILE, 1.0F, 1.0F);
         } else if (Config.taintFromFlux && rand.nextInt(50) == 0) {
            Utils.setBiomeAt(world, pos.getX(), pos.getZ(), ThaumcraftWorldGenerator.biomeTaint);
            world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getDefaultState(), 3);
            world.addBlockEvent(pos, ConfigBlocks.blockTaintFibres, 1, 0);
         }
      } else if (rand.nextInt(30) == 0) {
         if (meta == 0) {
            world.setBlockToAir(pos);
         } else {
            world.setBlockState(pos, state.withProperty(LEVEL, meta - 1), 3);
            if (rand.nextBoolean() && world.isAirBlock(pos.up())) {
               world.setBlockState(pos.up(), ConfigBlocks.blockFluxGas.getDefaultState(), 3);
            }
         }
      }
   }

   @Override
   public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
      int meta = world.getBlockState(pos).getBlock() == this
            ? this.getQuantaValue(world, pos) : 0;
      return meta < 2;
   }

   public boolean isInsideOfMaterial(World world, Entity entity) {
      double d0 = entity.posY + (double) entity.getEyeHeight();
      int i = MathHelper.floor(entity.posX);
      int j = MathHelper.floor(d0);
      int k = MathHelper.floor(entity.posZ);
      BlockPos bp = new BlockPos(i, j, k);
      IBlockState bs = world.getBlockState(bp);
      if (bs.getMaterial() == this.getMaterial(this.getDefaultState())) {
         float f = this.getQuantaPercentage(world, bp) - 0.11111111F;
         float f1 = (float) (j + 1) - f;
         return d0 < (double) f1;
      } else {
         return false;
      }
   }

   static {
      defaultDisplacements.put(ConfigBlocks.blockTaintFibres, true);
   }
}
