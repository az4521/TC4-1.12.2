package thaumcraft.common.tiles;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileJarNode extends TileJar implements IAspectContainer, INode, IWandable {
   private AspectList aspects = new AspectList();
   private AspectList aspectsBase = new AspectList();
   private NodeType nodeType;
   private NodeModifier nodeModifier;
   private String id;
   public long animate;
   public boolean drop;

   public TileJarNode() {
      this.nodeType = NodeType.NORMAL;
      this.nodeModifier = null;
      this.id = "";
      this.animate = 0L;
      this.drop = true;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspects.readFromNBT(nbttagcompound);
      this.id = nbttagcompound.getString("nodeId");
      AspectList al = new AspectList();
      NBTTagList tlist = nbttagcompound.getTagList("AspectsBase", 10);

      for(int j = 0; j < tlist.tagCount(); ++j) {
         NBTTagCompound rs = tlist.getCompoundTagAt(j);
         if (rs.hasKey("key")) {
            al.add(Aspect.getAspect(rs.getString("key")), rs.getInteger("amount"));
         }
      }

      short oldBase = nbttagcompound.getShort("nodeVisBase");
      this.aspectsBase = new AspectList();
      if (oldBase > 0 && al.size() == 0) {
         for(Aspect a : this.aspects.getAspects()) {
            this.aspectsBase.merge(a, oldBase);
         }
      } else {
         this.aspectsBase = al.copy();
      }

      this.setNodeType(NodeType.values()[nbttagcompound.getByte("type")]);
      byte mod = nbttagcompound.getByte("modifier");
      if (mod >= 0) {
         this.setNodeModifier(NodeModifier.values()[mod]);
      } else {
         this.setNodeModifier(null);
      }

   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspects.writeToNBT(nbttagcompound);
      nbttagcompound.setString("nodeId", this.id);
      NBTTagList tlist = new NBTTagList();
      nbttagcompound.setTag("AspectsBase", tlist);

      for(Aspect aspect : this.aspectsBase.getAspects()) {
         if (aspect != null) {
            NBTTagCompound f = new NBTTagCompound();
            f.setString("key", aspect.getTag());
            f.setInteger("amount", this.aspectsBase.getAmount(aspect));
            tlist.appendTag(f);
         }
      }

      nbttagcompound.setByte("type", (byte)this.getNodeType().ordinal());
      nbttagcompound.setByte("modifier", this.getNodeModifier() == null ? -1 : (byte)this.getNodeModifier().ordinal());
   }

   public AspectList getAspects() {
      return this.aspects;
   }

   public AspectList getAspectsBase() {
      return this.aspectsBase;
   }

   public void setAspects(AspectList aspects) {
      this.aspects = aspects.copy();
      this.aspectsBase = aspects.copy();
   }

   public int addToContainer(Aspect tt, int am) {
      int out = 0;
      if (this.aspects.getAmount(tt) + am > this.aspectsBase.getAmount(tt)) {
         out = this.aspects.getAmount(tt) + am - this.aspectsBase.getAmount(tt);
      }

      this.aspects.add(tt, am - out);
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      this.markDirty();
      return out;
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.aspects.getAmount(tt) >= am) {
         this.aspects.remove(tt, am);
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tag, int amt) {
      return this.aspects.getAmount(tag) >= amt;
   }

   public boolean doesContainerContain(AspectList ot) {
      for(Aspect tt : ot.getAspects()) {
         if (this.aspects.getAmount(tt) < ot.getAmount(tt)) {
            return false;
         }
      }

      return true;
   }

   public int containerContains(Aspect tag) {
      return this.aspects.getAmount(tag);
   }

   public NodeType getNodeType() {
      return this.nodeType;
   }

   public void setNodeType(NodeType nodeType) {
      this.nodeType = nodeType;
   }

   public void setNodeModifier(NodeModifier nodeModifier) {
      this.nodeModifier = nodeModifier;
   }

   public NodeModifier getNodeModifier() {
      return this.nodeModifier;
   }

   public int getNodeVisBase(Aspect aspect) {
      return this.aspectsBase.getAmount(aspect);
   }

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
      if (this.aspectsBase.getAmount(aspect) < nodeVisBase) {
         this.aspectsBase.merge(aspect, nodeVisBase);
      } else {
         this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);
      }

   }

   public boolean receiveClientEvent(int i, int j) {
      if (i != 9) {
         return super.receiveClientEvent(i, j);
      } else {
         if (this.world.isRemote) {
            for(int yy = -1; yy < 3; ++yy) {
               for(int xx = -1; xx < 2; ++xx) {
                  for(int zz = -1; zz < 2; ++zz) {
                     Thaumcraft.proxy.blockSparkle(this.world, this.getPos().getX() + xx, this.getPos().getY() + yy, this.getPos().getZ() + zz, -9999, 5);
                  }
               }
            }

            this.animate = System.currentTimeMillis() + 1000L;
         }

         return true;
      }
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      if (!world.isRemote) {
         this.drop = false;
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), (ConfigBlocks.blockAiry).getStateFromMeta(0), 3);
         TileNode tn = (TileNode)world.getTileEntity(new BlockPos(x, y, z));
         if (tn != null) {
            tn.setAspects(this.getAspects());
            tn.setNodeModifier(this.getNodeModifier());
            tn.setNodeType(this.getNodeType());
            tn.id = this.getId();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
            tn.markDirty();
         }
      }

      world.playEvent(2001, new BlockPos(x, y, z), Block.getIdFromBlock(ConfigBlocks.blockJar) + 0xF000);
      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:random.glass")); if (_snd != null) player.world.playSound(null, (double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 0.9F + player.world.rand.nextFloat() * 0.2F); };
      player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
      return 0;
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      return null;
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }
}
