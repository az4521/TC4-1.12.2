package thaumcraft.client.renderers.compat;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

public final class ItemRenderContext {
    private static final ThreadLocal<ItemCameraTransforms.TransformType> CURRENT = new ThreadLocal<>();

    private ItemRenderContext() {
    }

    public static void set(ItemCameraTransforms.TransformType transformType) {
        CURRENT.set(transformType);
    }

    public static ItemCameraTransforms.TransformType get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
