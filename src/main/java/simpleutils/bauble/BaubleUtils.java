package simpleutils.bauble;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BaubleUtils {

    public static boolean forEachBauble(EntityPlayer player, BaubleConsumer<Item> operation) {
        for (BaubleType type : BaubleType.values()) {
            if (forEachBaubleWithBaubleType(type, player, operation)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean forEachBauble(EntityPlayer player, Class<T> expectedItemType, BaubleConsumer<T> operation) {
        for (BaubleType type : BaubleType.values()) {
            if (forEachBaubleWithBaubleType(type, player, expectedItemType, operation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean forEachBaubleWithBaubleType(BaubleType baubleType, EntityPlayer player, BaubleConsumer<Item> operation) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles == null) return false;
        for (int slot : baubleType.getValidSlots()) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (operation.accept(slot, stack, stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean forEachBaubleWithBaubleType(BaubleType baubleType, EntityPlayer player, Class<T> expectedItemType, BaubleConsumer<T> operation) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles == null) return false;
        for (int slot : baubleType.getValidSlots()) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (expectedItemType.isAssignableFrom(item.getClass())) {
                if (operation.accept(slot, stack, (T) item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
