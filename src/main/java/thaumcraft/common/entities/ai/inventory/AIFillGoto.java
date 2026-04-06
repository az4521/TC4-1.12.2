package thaumcraft.common.entities.ai.inventory;

import java.util.ArrayList;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;

public class AIFillGoto extends EntityAIBase {
    private EntityGolemBase theGolem;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
    private BlockPos dest = null;
    int count = 0;
    int prevX = 0;
    int prevY = 0;
    int prevZ = 0;

    public AIFillGoto(EntityGolemBase par1EntityCreature) {
        this.theGolem = par1EntityCreature;
        this.setMutexBits(3);
    }

    public boolean shouldExecute() {
        if (this.theGolem.getCarried() == null && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.hasSomething()) {
            ArrayList<ItemStack> mi = GolemHelper.getMissingItems(this.theGolem);
            if (mi != null && !mi.isEmpty()) {
                ArrayList<ItemStack> missingItems = new ArrayList<>();
                if (this.theGolem.getUpgradeAmount(5) > 0) {
                    for (ItemStack stack : mi) {
                       for (int oreID:OreDictionary.getOreIDs(stack)){
                          if (oreID != -1) {
                             ItemStack[] ores = OreDictionary.getOres(OreDictionary.getOreName(oreID)).toArray(new ItemStack[0]);

                             for (ItemStack ore : ores) {
                                missingItems.add(ore.copy());
                             }
                          } else {
                             missingItems.add(stack.copy());
                          }
                       }
                    }
                } else {
                    for (ItemStack stack : mi) {
                        missingItems.add(stack.copy());
                    }
                }

                ArrayList<IInventory> results = new ArrayList<>();

                for (ItemStack stack : missingItems) {
                    this.theGolem.itemWatched = stack.copy();

                    for (byte color : this.theGolem.getColorsMatching(this.theGolem.itemWatched)) {
                        results = GolemHelper.getContainersWithGoods(this.theGolem.world, this.theGolem, this.theGolem.itemWatched, color);
                    }

                    if (!results.isEmpty()) {
                        break;
                    }
                }

                if (results != null && !results.isEmpty()) {
                    EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
                    BlockPos home = this.theGolem.getHomePosition();
                    int cX = home.getX() - facing.getXOffset();
                    int cY = home.getY() - facing.getYOffset();
                    int cZ = home.getZ() - facing.getZOffset();
                    int tX = 0;
                    int tY = 0;
                    int tZ = 0;
                    double range = Double.MAX_VALUE;
                    float dmod = this.theGolem.getRange();

                    for (IInventory i : results) {
                        TileEntity te = (TileEntity) i;
                        double distance = this.theGolem.getDistanceSq((double) te.getPos().getX() + (double) 0.5F, (double) te.getPos().getY() + (double) 0.5F, (double) te.getPos().getZ() + (double) 0.5F);
                        if (distance < range && distance <= (double) (dmod * dmod) && (te.getPos().getX() != cX || te.getPos().getY() != cY || te.getPos().getZ() != cZ)) {
                            range = distance;
                            tX = te.getPos().getX();
                            tY = te.getPos().getY();
                            tZ = te.getPos().getZ();
                            this.dest = new BlockPos(tX, tY, tZ);
                        }
                    }

                    if (this.dest != null) {
                        this.movePosX = tX;
                        this.movePosY = tY;
                        this.movePosZ = tZ;
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean continueExecuting() {
        return this.count > 0 && !this.theGolem.getNavigator().noPath();
    }

    public void updateTask() {
        --this.count;
        if (this.count == 0 && this.prevX == MathHelper.floor(this.theGolem.posX) && this.prevY == MathHelper.floor(this.theGolem.posY) && this.prevZ == MathHelper.floor(this.theGolem.posZ)) {
            Vec3d var2 = RandomPositionGenerator.findRandomTarget(this.theGolem, 2, 1);
            if (var2 != null) {
                this.count = 20;
                this.theGolem.getNavigator().tryMoveToXYZ(var2.x, var2.y, var2.z, this.theGolem.getAIMoveSpeed());
            }
        }

        super.updateTask();
    }

    public void resetTask() {
        this.dest = null;
        this.count = 0;
    }

    public void startExecuting() {
        this.count = 200;
        this.prevX = MathHelper.floor(this.theGolem.posX);
        this.prevY = MathHelper.floor(this.theGolem.posY);
        this.prevZ = MathHelper.floor(this.theGolem.posZ);
        this.theGolem.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.theGolem.getAIMoveSpeed());
    }
}
