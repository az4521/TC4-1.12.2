package thaumcraft.client.renderers.models;

import net.minecraft.util.ResourceLocation;

@Deprecated
public class AdvancedModelLoader {
    public static IModelCustom loadModel(ResourceLocation location) {
        return new WavefrontObject(location);
    }
}
