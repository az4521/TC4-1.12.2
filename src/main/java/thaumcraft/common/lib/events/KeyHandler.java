package thaumcraft.common.lib.events;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.Keys;
import thaumcraft.common.entities.golems.ItemGolemBell;
import thaumcraft.common.items.armor.Hover;
import thaumcraft.common.items.armor.ItemHoverHarness;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.misc.PacketFocusChangeToServer;
import thaumcraft.common.lib.network.misc.PacketItemKeyToServer;

public class KeyHandler {
   public KeyBinding keyF = Keys.keyF;
   public KeyBinding keyH = Keys.keyH;
   public KeyBinding keyG = Keys.keyG;
   private boolean keyPressedF = false;
   private boolean keyPressedH = false;
   private boolean keyPressedG = false;
   public static boolean radialActive = false;
   public static boolean radialLock = false;
   public static long lastPressF = 0L;
   public static long lastPressH = 0L;
   public static long lastPressG = 0L;

   public KeyHandler() {
      ClientRegistry.registerKeyBinding(this.keyF);
      ClientRegistry.registerKeyBinding(this.keyH);
      ClientRegistry.registerKeyBinding(this.keyG);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void playerTick(TickEvent.PlayerTickEvent event) {
      if (event.side != Side.SERVER) {
         if (event.phase == Phase.START) {
            if (this.keyF.isKeyDown()) {
               if (FMLClientHandler.instance().getClient().inGameHasFocus) {
                  EntityPlayer player = event.player;
                  if (player != null) {
                     if (!this.keyPressedF) {
                        lastPressF = System.currentTimeMillis();
                        radialLock = false;
                     }

                     if (!radialLock && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemWandCasting && !((ItemWandCasting)player.getHeldItemMainhand().getItem()).isSceptre(player.getHeldItemMainhand())) {
                        if (player.isSneaking()) {
                           PacketHandler.INSTANCE.sendToServer(new PacketFocusChangeToServer(player, "REMOVE"));
                        } else {
                           radialActive = true;
                        }
                     } else if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGolemBell && !this.keyPressedF) {
                        PacketHandler.INSTANCE.sendToServer(new PacketItemKeyToServer(player, 0));
                     }
                  }

                  this.keyPressedF = true;
               }
            } else {
               radialActive = false;
               if (this.keyPressedF) {
                  lastPressF = System.currentTimeMillis();
               }

               this.keyPressedF = false;
            }

            if (this.keyH.isKeyDown()) {
               if (FMLClientHandler.instance().getClient().inGameHasFocus) {
                  EntityPlayer player = event.player;
                  if (player != null) {
                     if (!this.keyPressedH) {
                        lastPressH = System.currentTimeMillis();
                     }

                     if (player.inventory.armorItemInSlot(2) != null && player.inventory.armorItemInSlot(2).getItem() instanceof ItemHoverHarness && !this.keyPressedH) {
                        Hover.toggleHover(player, player.getEntityId(), player.inventory.armorItemInSlot(2));
                     }
                  }

                  this.keyPressedH = true;
               }
            } else {
               if (this.keyPressedH) {
                  lastPressH = System.currentTimeMillis();
               }

               this.keyPressedH = false;
            }

            if (this.keyG.isKeyDown()) {
               if (FMLClientHandler.instance().getClient().inGameHasFocus) {
                  EntityPlayer player = event.player;
                  if (player != null && !this.keyPressedG) {
                     lastPressG = System.currentTimeMillis();
                     PacketHandler.INSTANCE.sendToServer(new PacketItemKeyToServer(player, 1));
                  }

                  this.keyPressedG = true;
               }
            } else {
               if (this.keyPressedG) {
                  lastPressG = System.currentTimeMillis();
               }

               this.keyPressedG = false;
            }
         }

      }
   }
}
