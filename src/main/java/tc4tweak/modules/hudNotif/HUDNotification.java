package tc4tweak.modules.hudNotif;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tc4tweak.ConfigurationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.client.lib.PlayerNotifications;

import java.util.List;

public class HUDNotification {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler {
        @SubscribeEvent
        public void onGuiInitPost(GuiScreenEvent.InitGuiEvent.Post e) {
            if (e.getGui() instanceof GuiChat && ConfigurationHandler.INSTANCE.isAddClearButton()) {
                String caption = I18n.format("tc4tweaks.gui.clear_notification");
                int width = e.getGui().mc.fontRenderer.getStringWidth(caption) + 8;
                e.getButtonList().add(new GuiButtonExt(114514, e.getGui().width - width, e.getGui().height - 18, width, 18, caption) {
                    @Override
                    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                        this.visible = !PlayerNotifications.notificationList.isEmpty() || !PlayerNotifications.aspectList.isEmpty();
                        super.drawButton(mc, mouseX, mouseY, partialTicks);
                    }
                });
            }
        }

        @SubscribeEvent
        public void onGuiClick(GuiScreenEvent.ActionPerformedEvent.Pre e) {
            if (e.getGui() instanceof GuiChat && e.getButton().id == 114514 && ConfigurationHandler.INSTANCE.isAddClearButton()) {
                PlayerNotifications.notificationList.clear();
                PlayerNotifications.aspectList.clear();
            }
        }
    }
}
