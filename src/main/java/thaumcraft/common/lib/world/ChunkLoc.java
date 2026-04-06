package thaumcraft.common.lib.world;

import java.io.Serializable;
import net.minecraft.util.math.ChunkPos;

public class ChunkLoc implements Serializable {
   public final int x;
   public final int z;

   public ChunkLoc(int par1, int par2) {
      this.x = par1;
      this.z = par2;
   }

   public boolean equals(ChunkLoc par1Obj) {
      return par1Obj.x == this.x && par1Obj.z == this.z;
   }

   public boolean equals(ChunkPos par1Obj) {
      return par1Obj.x == this.x && par1Obj.z == this.z;
   }

   public int getCenterXPos() {
      return (this.x << 4) + 8;
   }

   public int getCenterZPosition() {
      return (this.z << 4) + 8;
   }

   public String toString() {
      return "[" + this.x + ", " + this.z + "]";
   }
}
