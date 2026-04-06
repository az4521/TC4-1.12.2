package thaumcraft.client.renderers.models;

/**
 * AdvancedModelLoader/IModelCustom were removed in MC 1.8.
 * Shim moved out of net.minecraftforge package to avoid JAR signing conflict.
 * Replace with OBJLoader / ModelLoaderRegistry when rendering is migrated.
 */
@Deprecated
public interface IModelCustom {
    void renderAll();
    void renderPart(String partName);
    void renderOnly(String... groupNames);
    void renderAllExcept(String... excludedGroupNames);
}
