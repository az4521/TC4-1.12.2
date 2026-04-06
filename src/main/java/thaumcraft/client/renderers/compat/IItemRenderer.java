package thaumcraft.client.renderers.compat;

import net.minecraft.item.ItemStack;

/**
 * IItemRenderer was removed in MC 1.8.
 * Item rendering now uses IBakedModel / ItemOverrideList.
 * This shim exists only to allow compilation.
 */
@Deprecated
public interface IItemRenderer {

    enum ItemRenderType {
        NONE,
        EQUIPPED,
        EQUIPPED_FIRST_PERSON,
        INVENTORY,
        ENTITY
    }

    enum ItemRendererHelper {
        BLOCK_3D,
        INVENTORY_BLOCK,
        ENTITY_ROTATION,
        ENTITY_BOBBING,
        EQUIPPED_BLOCK,
        EQUIPPED,
        BLOCK_ROTATE
    }

    boolean handleRenderType(ItemStack item, ItemRenderType type);
    boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper);
    void renderItem(ItemRenderType type, ItemStack item, Object... data);
}
