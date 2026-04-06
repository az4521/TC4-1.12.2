package thaumcraft.api.expands.warp.consts;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.expands.warp.PickWarpEventContext;
import thaumcraft.api.expands.warp.WarpEvent;
import thaumcraft.api.expands.warp.listeners.WarpEventListenerAfter;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketResearchComplete;

import javax.annotation.Nonnull;

import static thaumcraft.common.lib.WarpEvents.grantResearch;

public class AfterWarpEventListeners {
    public static final WarpEventListenerAfter CHECK_RESEARCH = new WarpEventListenerAfter(0) {
        @Override
        public void onWarpEvent(@Nonnull PickWarpEventContext warpContext, @Nonnull WarpEvent e, @Nonnull EntityPlayer player) {
            if (warpContext.actualWarp > 10
                    && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "BATHSALTS")
                    && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "@BATHSALTS")) {
                player.sendMessage(new TextComponentString("§5§o" + I18n.translateToLocal("warp.text.8")));
                if (player instanceof EntityPlayerMP) {
                    PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("@BATHSALTS"), (EntityPlayerMP) player);
                }
                Thaumcraft.proxy.getResearchManager().completeResearch(player, "@BATHSALTS");
            }

            if (warpContext.actualWarp > 25
                    && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "ELDRITCHMINOR")) {
                grantResearch(player, 10);
                if (player instanceof EntityPlayerMP) {
                    PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("ELDRITCHMINOR"), (EntityPlayerMP) player);
                }
                Thaumcraft.proxy.getResearchManager().completeResearch(player, "ELDRITCHMINOR");
            }

            if (warpContext.actualWarp > 50
                    && !ThaumcraftApiHelper.isResearchComplete(player.getName(), "ELDRITCHMAJOR")) {
                grantResearch(player, 20);
                if (player instanceof EntityPlayerMP) {
                    PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("ELDRITCHMAJOR"), (EntityPlayerMP) player);
                }
                Thaumcraft.proxy.getResearchManager().completeResearch(player, "ELDRITCHMAJOR");
            }
        }
    };
    public static final WarpEventListenerAfter DECREASE_A_TEMP_WARP = new WarpEventListenerAfter(1) {
        @Override
        public void onWarpEvent(@Nonnull PickWarpEventContext warpContext, @Nonnull WarpEvent e, @Nonnull EntityPlayer player) {
            Thaumcraft.proxy.getPlayerKnowledge().addWarpTemp(player.getName(), -1);
        }
    };
    public static final WarpEventListenerAfter DONT_SEND_MISC_FOR_EMPTY = new WarpEventListenerAfter(2) {
        @Override
        public void onWarpEvent(@Nonnull PickWarpEventContext warpContext, @Nonnull WarpEvent e, @Nonnull EntityPlayer player) {
            if (e == WarpEvent.EMPTY){
                e.sendMiscPacket = false;
            }
        }
    };
}
