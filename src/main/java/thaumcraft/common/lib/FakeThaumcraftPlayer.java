package thaumcraft.common.lib;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class FakeThaumcraftPlayer extends EntityPlayer {
   public FakeThaumcraftPlayer(World world, GameProfile name) {
      super(world, name);
   }

   public void addChatMessage(String s) {
   }

   public boolean canCommandSenderUseCommand(int i, String s) {
      return false;
   }

   public BlockPos getPlayerCoordinates() {
      return new BlockPos(0, 0, 0);
   }

   public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
   }

   public void sendMessage(ITextComponent var1) {
   }

   @Override
   public boolean isCreative() {
      return false;
   }

   @Override
   public boolean isSpectator() {
      return false;
   }
}
