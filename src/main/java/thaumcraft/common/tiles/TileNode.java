package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockTaintFibres;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.monster.EntityGiantBrainyZombie;
import thaumcraft.common.items.ItemCompassStone;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import net.minecraft.util.math.BlockPos;

public class TileNode extends TileThaumcraft implements INode, IWandable {
   long lastActive = 0L;
   AspectList aspects = new AspectList();
   AspectList aspectsBase = new AspectList();
   public static HashMap locations = new HashMap<>();
   private NodeType nodeType;
   private NodeModifier nodeModifier;
   int count;
   int regeneration;
   int wait;
   String id;
   byte nodeLock;
   boolean catchUp;
   public Entity drainEntity;
   public RayTraceResult drainCollision;
   public int drainColor;
   public Color targetColor;
   public Color color;

   public TileNode() {
      this.nodeType = NodeType.NORMAL;
      this.nodeModifier = null;
      this.count = 0;
      this.regeneration = -1;
      this.wait = 0;
      this.id = null;
      this.nodeLock = 0;
      this.catchUp = false;
      this.drainEntity = null;
      this.drainCollision = null;
      this.drainColor = 16777215;
      this.targetColor = new Color(16777215);
      this.color = new Color(16777215);
   }

   public String getId() {
      if (this.id == null) {
         this.id = this.generateId();
      }

      return this.id;
   }

   public String generateId() {
      this.id = this.world.provider.getDimension() + ":" + this.getPos().getX() + ":" + this.getPos().getY() + ":" + this.getPos().getZ();
      if (this.world != null && locations != null) {
         ArrayList<Integer> t = new ArrayList<>();
         t.add(this.world.provider.getDimension());
         t.add(this.getPos().getX());
         t.add(this.getPos().getY());
         t.add(this.getPos().getZ());
         locations.put(this.id, t);
      }

      return this.id;
   }

   public void onChunkUnload() {
      if (locations != null) {
         locations.remove(this.id);
      }

      super.onChunkUnload();
   }

   public void validate() {
      super.validate();
   }

   public void updateEntity() {
            if (this.id == null) {
         this.generateId();
      }

      boolean change = false;
      change = this.handleHungryNodeFirst(change);
      ++this.count;
      this.checkLock();
      if (!this.world.isRemote) {
         change = this.handleDischarge(change);
         change = this.handleRecharge(change);
         change = this.handleTaintNode(change);
         change = this.handleNodeStability(change);
         change = this.handleDarkNode(change);
         change = this.handlePureNode(change);
         change = this.handleHungryNodeSecond(change);
         if (change) {
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         }
      } else if (this.getNodeType() == NodeType.DARK && this.count % 50 == 0) {
         ItemCompassStone.sinisterNodes.put(new WorldCoordinates(this), System.currentTimeMillis());
      }

   }

   public void nodeChange() {
      this.regeneration = -1;
      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
   }

   public double getDistanceTo(double par1, double par3, double par5) {
      double var7 = (double)this.getPos().getX() + (double)0.5F - par1;
      double var9 = (double)this.getPos().getY() + (double)0.5F - par3;
      double var11 = (double)this.getPos().getZ() + (double)0.5F - par5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      return -1;
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      player.setActiveHand(net.minecraft.util.EnumHand.MAIN_HAND);
      ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
      wand.setObjectInUse(wandstack, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
      return wandstack;
   }

   public AspectList getAspects() {
      return this.aspects;
   }

   public AspectList getAspectsBase() {
      return this.aspectsBase;
   }

   public void setAspects(AspectList aspects) {
      this.aspects = aspects;
      this.aspectsBase = aspects.copy();
   }

   public int addToContainer(Aspect aspect, int amount) {
      int left = amount + this.aspects.getAmount(aspect) - this.aspectsBase.getAmount(aspect);
      left = Math.max(left, 0);
      this.aspects.add(aspect, amount - left);
      return left;
   }

   public boolean takeFromContainer(Aspect aspect, int amount) {
      return this.aspects.reduce(aspect, amount);
   }

   public Aspect takeRandomPrimalFromSource() {
      Aspect[] primals = this.aspects.getPrimalAspects();
      Aspect asp = primals[this.world.rand.nextInt(primals.length)];
      return asp != null && this.aspects.reduce(asp, 1) ? asp : null;
   }

   public Aspect chooseRandomFilteredFromSource(AspectList filter, boolean preserve) {
      int min = preserve ? 1 : 0;
      ArrayList<Aspect> validaspects = new ArrayList<>();

      for(Aspect prim : this.aspects.getAspects()) {
         if (filter.getAmount(prim) > 0 && this.aspects.getAmount(prim) > min) {
            validaspects.add(prim);
         }
      }

      if (validaspects.isEmpty()) {
         return null;
      } else {
         Aspect asp = validaspects.get(this.world.rand.nextInt(validaspects.size()));
         if (asp != null && this.aspects.getAmount(asp) > min) {
            return asp;
         } else {
            return null;
         }
      }
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

   public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
      if (this.aspectsBase.getAmount(aspect) < nodeVisBase) {
         this.aspectsBase.merge(aspect, nodeVisBase);
      } else {
         this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);
      }

   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.lastActive = nbttagcompound.getLong("lastActive");
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

      int regen = 600;
      if (this.getNodeModifier() != null) {
         switch (this.getNodeModifier()) {
            case BRIGHT:
               regen = 400;
               break;
            case PALE:
               regen = 900;
               break;
            case FADING:
               regen = 0;
         }
      }

      long ct = System.currentTimeMillis();
      int inc = regen * 75;
      if (regen > 0 && this.lastActive > 0L && ct > this.lastActive + (long)inc) {
         this.catchUp = true;
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setLong("lastActive", this.lastActive);
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

      return nbttagcompound;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      System.out.println("[TC4-DEBUG] TileNode.readCustomNBT at " + this.getPos() + " hasAspectsTag=" + nbttagcompound.hasKey("Aspects") + " world=" + (this.world != null));
      this.id = nbttagcompound.getString("nodeId");
      if (this.world != null && locations != null) {
         ArrayList<Integer> t = new ArrayList<>();
         t.add(this.world.provider.getDimension());
         t.add(this.getPos().getX());
         t.add(this.getPos().getY());
         t.add(this.getPos().getZ());
         locations.put(this.id, t);
      }

      this.setNodeType(NodeType.values()[nbttagcompound.getByte("type")]);
      byte mod = nbttagcompound.getByte("modifier");
      if (mod >= 0) {
         this.setNodeModifier(NodeModifier.values()[mod]);
      } else {
         this.setNodeModifier(null);
      }

      this.aspects.readFromNBT(nbttagcompound);
      System.out.println("[TC4-DEBUG] TileNode aspects loaded: " + this.aspects.size() + " aspects at " + this.getPos());
      String de = nbttagcompound.getString("drainer");
      if (de != null && !de.isEmpty() && this.getWorld() != null) {
         this.drainEntity = this.getWorld().getPlayerEntityByName(de);
         if (this.drainEntity != null) {
            this.drainCollision = new RayTraceResult(RayTraceResult.Type.BLOCK, new Vec3d(this.drainEntity.posX, this.drainEntity.posY, this.drainEntity.posZ), EnumFacing.UP, new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
         }
      }

      this.drainColor = nbttagcompound.getInteger("draincolor");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      if (this.id == null) {
         this.id = this.generateId();
      }

      if (this.world != null && locations != null) {
         ArrayList<Integer> t = new ArrayList<>();
         t.add(this.world.provider.getDimension());
         t.add(this.getPos().getX());
         t.add(this.getPos().getY());
         t.add(this.getPos().getZ());
         locations.put(this.id, t);
      }

      nbttagcompound.setString("nodeId", this.id);
      nbttagcompound.setByte("type", (byte)this.getNodeType().ordinal());
      nbttagcompound.setByte("modifier", this.getNodeModifier() == null ? -1 : (byte)this.getNodeModifier().ordinal());
      this.aspects.writeToNBT(nbttagcompound);
      if (this.drainEntity != null && this.drainEntity instanceof EntityPlayer) {
         nbttagcompound.setString("drainer", this.drainEntity.getName());
      }

      nbttagcompound.setInteger("draincolor", this.drainColor);
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
      boolean mfu = false;
      ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
      RayTraceResult movingobjectposition = EntityUtils.getMovingObjectPositionFromPlayer(this.world, player, true);
      if (movingobjectposition != null && movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
         int i = movingobjectposition.getBlockPos().getX();
         int j = movingobjectposition.getBlockPos().getY();
         int k = movingobjectposition.getBlockPos().getZ();
         if (i != this.getPos().getX() || j != this.getPos().getY() || k != this.getPos().getZ()) {
            player.stopActiveHand();
         }
      } else {
         player.stopActiveHand();
      }

      if (count % 5 == 0) {
         int tap = 1;
         if (ResearchManager.isResearchComplete(player.getName(), "NODETAPPER1")) {
            ++tap;
         }

         if (ResearchManager.isResearchComplete(player.getName(), "NODETAPPER2")) {
            ++tap;
         }

         boolean preserve = !player.isSneaking() && ResearchManager.isResearchComplete(player.getName(), "NODEPRESERVE") && !wand.getRod(wandstack).getTag().equals("wood") && !wand.getCap(wandstack).getTag().equals("iron");
         boolean success = false;
         Aspect aspect = null;
         if ((aspect = this.chooseRandomFilteredFromSource(wand.getAspectsWithRoom(wandstack), preserve)) != null) {
            int amt = this.getAspects().getAmount(aspect);
            if (tap > amt) {
               tap = amt;
            }

            if (preserve && tap == amt) {
               --tap;
            }

            if (tap > 0) {
               int rem = wand.addVis(wandstack, aspect, tap, !this.world.isRemote);
               if (rem < tap) {
                  this.drainColor = aspect.getColor();
                  if (!this.world.isRemote) {
                     this.takeFromContainer(aspect, tap - rem);
                     mfu = true;
                  }

                  success = true;
               }
            }
         }

         if (success) {
            this.drainEntity = player;
            this.drainCollision = movingobjectposition;
            this.targetColor = new Color(this.drainColor);
         } else {
            this.drainEntity = null;
            this.drainCollision = null;
         }

         if (mfu) {
            this.markDirty();
            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.getPos());
            this.world.notifyBlockUpdate(this.getPos(), state, state, 3);
         }
      }

      if (player.world.isRemote) {
         int r = this.targetColor.getRed();
         int g = this.targetColor.getGreen();
         int b = this.targetColor.getBlue();
         int r2 = this.color.getRed() * 4;
         int g2 = this.color.getGreen() * 4;
         int b2 = this.color.getBlue() * 4;
         this.color = new Color((r + r2) / 5, (g + g2) / 5, (b + b2) / 5);
      }

   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
      this.drainEntity = null;
      this.drainCollision = null;
   }

   public boolean receiveClientEvent(int i, int j) {
      return super.receiveClientEvent(i, j);
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tag, int amount) {
      return false;
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public int containerContains(Aspect tag) {
      return 0;
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   private boolean handleHungryNodeFirst(boolean change) {
      if (this.getNodeType() == NodeType.HUNGRY) {
         if (this.world.isRemote) {
            for(int a = 0; a < Thaumcraft.proxy.particleCount(1); ++a) {
               int tx = this.getPos().getX() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
               int ty = this.getPos().getY() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
               int tz = this.getPos().getZ() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
               if (ty > this.world.getHeight(tx, tz)) {
                  ty = this.world.getHeight(tx, tz);
               }

               Vec3d v1 = new Vec3d((double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F);
               Vec3d v2 = new Vec3d((double)tx + (double)0.5F, (double)ty + (double)0.5F, (double)tz + (double)0.5F);
               RayTraceResult mop = ThaumcraftApiHelper.rayTraceIgnoringSource(this.world, v1, v2, true, false, false);
               if (mop != null && this.getPos().distanceSqToCenter(mop.getBlockPos().getX()+0.5, mop.getBlockPos().getY()+0.5, mop.getBlockPos().getZ()+0.5) < 65536.0) {
                  tx = mop.getBlockPos().getX();
                  ty = mop.getBlockPos().getY();
                  tz = mop.getBlockPos().getZ();
                  Block bi = this.world.getBlockState(new BlockPos(tx, ty, tz)).getBlock();
                  int md = world.getBlockState(new net.minecraft.util.math.BlockPos(tx, ty, tz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(tx, ty, tz)));
                  if (!this.world.isAirBlock(new net.minecraft.util.math.BlockPos(tx, ty, tz))) {
                     Thaumcraft.proxy.hungryNodeFX(this.world, tx, ty, tz, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), bi, md);
                  }
               }
            }
         }

         if (Config.hardNode) {
            List ents = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(15.0F, 15.0F, 15.0F));
            if (ents != null && !ents.isEmpty()) {
               for(Object ent : ents) {
                  Entity eo = (Entity)ent;
                  if (!(eo instanceof EntityPlayer) || !((EntityPlayer)eo).capabilities.disableDamage) {
                     if (eo.isEntityAlive() && !eo.isEntityInvulnerable(DamageSource.OUT_OF_WORLD)) {
                        double d = this.getDistanceTo(eo.posX, eo.posY, eo.posZ);
                        if (d < (double)2.0F) {
                           eo.attackEntityFrom(DamageSource.OUT_OF_WORLD, 1.0F);
                           if (!eo.isEntityAlive() && !this.world.isRemote) {
                              ScanResult scan = new ScanResult((byte)2, 0, 0, eo, "");
                              AspectList al = ScanManager.getScanAspects(scan, this.world);
                              if (al != null && al.size() > 0) {
                                 al = ResearchManager.reduceToPrimals(al.copy());
                                 if (al != null && al.size() > 0) {
                                    Aspect a = al.getAspects()[this.world.rand.nextInt(al.size())];
                                    if (this.getAspects().getAmount(a) < this.getNodeVisBase(a)) {
                                       this.addToContainer(a, 1);
                                       change = true;
                                    } else if (this.world.rand.nextInt(1 + this.getNodeVisBase(a) * 2) < al.getAmount(a)) {
                                       this.aspectsBase.add(a, 1);
                                       change = true;
                                    }
                                 }
                              }
                           }
                        }
                     }

                     double var3 = ((double)this.getPos().getX() + (double)0.5F - eo.posX) / (double)15.0F;
                     double var5 = ((double)this.getPos().getY() + (double)0.5F - eo.posY) / (double)15.0F;
                     double var7 = ((double)this.getPos().getZ() + (double)0.5F - eo.posZ) / (double)15.0F;
                     double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
                     double var11 = (double)1.0F - var9;
                     if (var11 > (double)0.0F) {
                        var11 *= var11;
                        eo.motionX += var3 / var9 * var11 * 0.15;
                        eo.motionY += var5 / var9 * var11 * (double)0.25F;
                        eo.motionZ += var7 / var9 * var11 * 0.15;
                     }
                  }
               }
            }
         }
      }

      return change;
   }

   private boolean handleDischarge(boolean change) {
      if (this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())).getBlock() == ConfigBlocks.blockAiry
              && this.getLock() != 1) {
         if (this.getNodeModifier() == NodeModifier.FADING) {
            return change;
         } else {
            boolean shiny = this.getNodeType() == NodeType.HUNGRY || this.getNodeModifier() == NodeModifier.BRIGHT;
            int inc = this.getNodeModifier() == null ? 2 : (shiny ? 1 : (this.getNodeModifier() == NodeModifier.PALE ? 3 : 2));
            if (this.count % inc != 0) {
               return change;
            } else {
               int x = this.world.rand.nextInt(5) - this.world.rand.nextInt(5);
               int y = this.world.rand.nextInt(5) - this.world.rand.nextInt(5);
               int z = this.world.rand.nextInt(5) - this.world.rand.nextInt(5);
               if (this.getNodeModifier() == NodeModifier.PALE && this.world.rand.nextBoolean()) {
                  return change;
               } else {
                  if (x != 0 || y != 0 || z != 0) {
                     TileEntity te = this.world.getTileEntity(new BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z));
                     if (te instanceof INode && this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z)).getBlock() == ConfigBlocks.blockAiry) {
                        if (te instanceof TileNode && ((TileNode)te).getLock() > 0) {
                           return change;
                        }

                        INode anotherNode = (INode)te;
                        int ndavg = (anotherNode.getAspects().visSize() + anotherNode.getAspectsBase().visSize()) / 2;
                        int thisavg = (this.getAspects().visSize() + this.getAspectsBase().visSize()) / 2;
                        if (ndavg < thisavg && anotherNode.getAspects().size() > 0) {
                           Aspect a = anotherNode.getAspects().getAspects()[this.world.rand.nextInt(anotherNode.getAspects().size())];
                           boolean u = false;
                           if (this.getAspects().getAmount(a) < this.getNodeVisBase(a) && anotherNode.takeFromContainer(a, 1)) {
                              this.addToContainer(a, 1);
                              u = true;
                           } else if (anotherNode.takeFromContainer(a, 1)) {
                              if (this.world.rand.nextInt(1 + (int)((double)this.getNodeVisBase(a) / (shiny ? (double)1.5F : (double)1.0F))) == 0) {
                                 this.aspectsBase.add(a, 1);
                                 if (this.getNodeModifier() == NodeModifier.PALE && this.world.rand.nextInt(100) == 0) {
                                    this.setNodeModifier(null);
                                    this.regeneration = -1;
                                 }

                                 if (this.world.rand.nextInt(3) == 0) {
                                    anotherNode.setNodeVisBase(a, (short)(anotherNode.getNodeVisBase(a) - 1));
                                 }
                              }

                              u = true;
                           }

                           if (u) {
                              ((TileNode)te).wait = ((TileNode)te).regeneration / 2;
                              BlockPos updatePos = new BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z);
                              this.world.notifyBlockUpdate(updatePos, this.world.getBlockState(updatePos), this.world.getBlockState(updatePos), 3);
                              te.markDirty();
                              change = true;
                              PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockZap((float)(this.getPos().getX() + x) + 0.5F, (float)(this.getPos().getY() + y) + 0.5F, (float)(this.getPos().getZ() + z) + 0.5F, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32.0F));
                           }
                        }
                     }
                  }

                  return change;
               }
            }
         }
      }
      else {
         return change;
      }
   }

   private boolean handleRecharge(boolean change) {
      if (this.regeneration < 0) {
         this.regeneration = 600;
         if (this.getNodeModifier() != null) {
            switch (this.getNodeModifier()) {
               case BRIGHT:
                  this.regeneration = 400;
                  break;
               case PALE:
                  this.regeneration = 900;
                  break;
               case FADING:
                  this.regeneration = 0;
            }
         }

         if (this.getLock() == 1) {
            this.regeneration *= 2;
         }

         if (this.getLock() == 2) {
            this.regeneration *= 20;
         }
      }

      if (this.catchUp) {
         this.catchUp = false;
         long ct = System.currentTimeMillis();
         int inc = this.regeneration * 75;
         int amt = inc > 0 ? (int)((ct - this.lastActive) / (long)inc) : 0;
         if (amt > 0) {
            for(int a = 0; a < Math.min(amt, this.aspectsBase.visSize()); ++a) {
               AspectList al = new AspectList();

               for(Aspect aspect : this.getAspects().getAspects()) {
                  if (this.getAspects().getAmount(aspect) < this.getNodeVisBase(aspect)) {
                     al.add(aspect, 1);
                  }
               }

               if (al.size() > 0) {
                  this.addToContainer(al.getAspects()[this.world.rand.nextInt(al.size())], 1);
               }
            }
         }
      }

      if (this.count % 1200 == 0) {
         for(Aspect aspect : this.getAspects().getAspects()) {
            if (this.getAspects().getAmount(aspect) <= 0) {
               this.setNodeVisBase(aspect, (short)(this.getNodeVisBase(aspect) - 1));
               if (this.world.rand.nextInt(20) == 0 || this.getNodeVisBase(aspect) <= 0) {
                  this.getAspects().remove(aspect);
                  if (this.world.rand.nextInt(5) == 0) {
                     if (this.getNodeModifier() == NodeModifier.BRIGHT) {
                        this.setNodeModifier(null);
                     } else if (this.getNodeModifier() == null) {
                        this.setNodeModifier(NodeModifier.PALE);
                     }

                     if (this.getNodeModifier() == NodeModifier.PALE && this.world.rand.nextInt(5) == 0) {
                        this.setNodeModifier(NodeModifier.FADING);
                     }
                  }

                  this.nodeChange();
                  break;
               }

               this.nodeChange();
            }
         }

         if (this.getAspects().size() <= 0) {
            this.invalidate();
            if (this.getBlockType() == ConfigBlocks.blockAiry) {
               this.world.setBlockToAir(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
            } else if (this.getBlockType() == ConfigBlocks.blockMagicalLog) {
               { net.minecraft.util.math.BlockPos _p = this.getPos(); int _m = this.world.getBlockState(_p).getBlock().getMetaFromState(this.world.getBlockState(_p)); this.world.setBlockState(_p, this.world.getBlockState(_p).getBlock().getStateFromMeta(_m - 1), 3); }
            }
         }
      }

      if (this.wait > 0) {
         --this.wait;
      }

      if (this.regeneration > 0 && this.wait == 0 && this.count % this.regeneration == 0) {
         this.lastActive = System.currentTimeMillis();
         AspectList al = new AspectList();

         for(Aspect aspect : this.getAspects().getAspects()) {
            if (this.getAspects().getAmount(aspect) < this.getNodeVisBase(aspect)) {
               al.add(aspect, 1);
            }
         }

         if (al.size() > 0) {
            this.addToContainer(al.getAspects()[this.world.rand.nextInt(al.size())], 1);
            change = true;
         }
      }

      return change;
   }

   private boolean handleTaintNode(boolean change) {
      if (this.getNodeType() == NodeType.TAINTED && this.count % 50 == 0) {
         int x = 0;
         int z = 0;
         int y = 0;
         x = this.getPos().getX() + this.world.rand.nextInt(8) - this.world.rand.nextInt(8);
         z = this.getPos().getZ() + this.world.rand.nextInt(8) - this.world.rand.nextInt(8);
         Biome bg = this.world.getBiome(new BlockPos(x, 0, z));
         if (Biome.getIdForBiome(bg) != Biome.getIdForBiome(ThaumcraftWorldGenerator.biomeTaint)) {
            Utils.setBiomeAt(this.world, x, z, ThaumcraftWorldGenerator.biomeTaint);
         }

         if (Config.hardNode && this.world.rand.nextBoolean()) {
            x = this.getPos().getX() + this.world.rand.nextInt(5) - this.world.rand.nextInt(5);
            z = this.getPos().getZ() + this.world.rand.nextInt(5) - this.world.rand.nextInt(5);
            y = this.getPos().getY() + this.world.rand.nextInt(5) - this.world.rand.nextInt(5);
            if (BlockTaintFibres.spreadFibres(this.world, x, y, z)) {
            }
         }
      } else if (this.getNodeType() != NodeType.PURE && this.getNodeType() != NodeType.TAINTED && this.count % 100 == 0) {
         Biome bg = this.world.getBiome(new BlockPos(this.getPos().getX(), 0, this.getPos().getZ()));
         if (Biome.getIdForBiome(bg) == Biome.getIdForBiome(ThaumcraftWorldGenerator.biomeTaint) && this.world.rand.nextInt(500) == 0) {
            this.setNodeType(NodeType.TAINTED);
            this.nodeChange();
         }
      }

      return change;
   }

   private boolean handleNodeStability(boolean change) {
      if (this.count % 100 == 0) {
         if (this.getNodeType() == NodeType.UNSTABLE && this.world.rand.nextBoolean()) {
            if (this.getLock() == 0) {
               Aspect aspect = null;
               if ((aspect = this.takeRandomPrimalFromSource()) != null) {
                  EntityAspectOrb orb = new EntityAspectOrb(this.world, (double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F, aspect, 1);
                  this.world.spawnEntity(orb);
                  change = true;
               }
            } else if (this.world.rand.nextInt(10000 / this.getLock()) == 42) {
               this.setNodeType(NodeType.NORMAL);
               change = true;
            }
         }

         if (this.getNodeModifier() == NodeModifier.FADING && this.getLock() > 0 && this.world.rand.nextInt(12500 / this.getLock()) == 69) {
            this.setNodeModifier(NodeModifier.PALE);
            change = true;
         }
      }

      return change;
   }

   private boolean handlePureNode(boolean change) {
      int dimbl = ThaumcraftWorldGenerator.getDimBlacklist(this.world.provider.getDimension());
      if (this.world.provider.getDimension() != -1 && this.world.provider.getDimension() != 1 && dimbl != 0 && dimbl != 2 && this.getNodeType() == NodeType.PURE && this.count % 50 == 0) {
         int x = this.getPos().getX() + this.world.rand.nextInt(8) - this.world.rand.nextInt(8);
         int z = this.getPos().getZ() + this.world.rand.nextInt(8) - this.world.rand.nextInt(8);
         Biome bg = this.world.getBiome(new BlockPos(x, 0, z));
         int biobl = ThaumcraftWorldGenerator.getBiomeBlacklist(Biome.getIdForBiome(bg));
         if (biobl != 0 && biobl != 2 && Biome.getIdForBiome(bg) != Biome.getIdForBiome(ThaumcraftWorldGenerator.biomeMagicalForest)) {
            if (Biome.getIdForBiome(bg) == Biome.getIdForBiome(ThaumcraftWorldGenerator.biomeTaint)) {
               Utils.setBiomeAt(this.world, x, z, ThaumcraftWorldGenerator.biomeMagicalForest);
            } else if (this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())).getBlock() == ConfigBlocks.blockMagicalLog) {
               Utils.setBiomeAt(this.world, x, z, ThaumcraftWorldGenerator.biomeMagicalForest);
            }
         }
      }

      return change;
   }

   private boolean handleDarkNode(boolean change) {
      int dimbl = ThaumcraftWorldGenerator.getDimBlacklist(this.world.provider.getDimension());
      int biobl = ThaumcraftWorldGenerator.getBiomeBlacklist(Biome.getIdForBiome(this.world.getBiome(new BlockPos(this.getPos().getX(), 0, this.getPos().getZ()))));
      if (biobl != 0 && biobl != 2 && this.world.provider.getDimension() != -1 && this.world.provider.getDimension() != 1 && dimbl != 0 && dimbl != 2 && this.getNodeType() == NodeType.DARK && this.count % 50 == 0) {
         int x = this.getPos().getX() + this.world.rand.nextInt(12) - this.world.rand.nextInt(12);
         int z = this.getPos().getZ() + this.world.rand.nextInt(12) - this.world.rand.nextInt(12);
         Biome bg = this.world.getBiome(new BlockPos(x, 0, z));
         if (Biome.getIdForBiome(bg) != Biome.getIdForBiome(ThaumcraftWorldGenerator.biomeEerie)) {
            Utils.setBiomeAt(this.world, x, z, ThaumcraftWorldGenerator.biomeEerie);
         }

         if (Config.hardNode && this.world.rand.nextBoolean() && this.world.getClosestPlayer((double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F, 24.0F, false) != null) {
            EntityGiantBrainyZombie entity = new EntityGiantBrainyZombie(this.world);
            if (entity != null) {
               int j = this.world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(10.0F, 6.0F, 10.0F)).size();
               if (j <= 3) {
                  double d0 = (double)this.getPos().getX() + (this.world.rand.nextDouble() - this.world.rand.nextDouble()) * (double)5.0F;
                  double d3 = this.getPos().getY() + this.world.rand.nextInt(3) - 1;
                  double d4 = (double)this.getPos().getZ() + (this.world.rand.nextDouble() - this.world.rand.nextDouble()) * (double)5.0F;
                  EntityLiving entityliving = entity instanceof EntityLiving ? entity : null;
                  entity.setLocationAndAngles(d0, d3, d4, this.world.rand.nextFloat() * 360.0F, 0.0F);
                  if (entityliving == null || entityliving.getCanSpawnHere()) {
                     this.world.spawnEntity(entityliving);
                     this.world.playEvent(2004, this.getPos(), 0);
                     if (entityliving != null) {
                        entityliving.spawnExplosionParticle();
                     }
                  }
               }
            }
         }
      }

      return change;
   }

   private boolean handleHungryNodeSecond(boolean change) {
      if (this.getNodeType() == NodeType.HUNGRY && this.count % 50 == 0) {
         int tx = this.getPos().getX() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
         int ty = this.getPos().getY() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
         int tz = this.getPos().getZ() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
         if (ty > this.world.getHeight(tx, tz)) {
            ty = this.world.getHeight(tx, tz);
         }

         Vec3d v1 = new Vec3d((double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F);
         Vec3d v2 = new Vec3d((double)tx + (double)0.5F, (double)ty + (double)0.5F, (double)tz + (double)0.5F);
         RayTraceResult mop = ThaumcraftApiHelper.rayTraceIgnoringSource(this.world, v1, v2, true, false, false);
         if (mop != null && this.getPos().distanceSqToCenter(mop.getBlockPos().getX()+0.5, mop.getBlockPos().getY()+0.5, mop.getBlockPos().getZ()+0.5) < 65536.0) {
            tx = mop.getBlockPos().getX();
            ty = mop.getBlockPos().getY();
            tz = mop.getBlockPos().getZ();
            if (!this.world.isAirBlock(new net.minecraft.util.math.BlockPos(tx, ty, tz))) {
               BlockPos destroyPos = new BlockPos(tx, ty, tz);
               float h = this.world.getBlockState(destroyPos).getBlockHardness(this.world, destroyPos);
               if (h >= 0.0F && h < 5.0F) {
                  this.world.destroyBlock(destroyPos, true);
               }
            }
         }
      }

      return change;
   }

   public byte getLock() {
      return this.nodeLock;
   }

   public void checkLock() {
      if ((this.count <= 1 || this.count % 50 == 0) && this.getPos().getY() > 0 && this.getBlockType() == ConfigBlocks.blockAiry) {
         byte oldLock = this.nodeLock;
         this.nodeLock = 0;
         if (!this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ()))
                 && this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock() == ConfigBlocks.blockStoneDevice) {
            if (world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ()))) == 9) {
               this.nodeLock = 1;
            } else if (world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ()))) == 10) {
               this.nodeLock = 2;
            }
         }

         if (oldLock != this.nodeLock) {
            this.regeneration = -1;
         }
      }

   }
}
