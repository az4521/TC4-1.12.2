package thaumcraft.client.renderers.compat;

/**
 * Marker interface for blocks/items that have a registerIcons method
 * from the 1.7.10 IIconRegister system.
 */
public interface IIconRegisterable {
    void registerIcons(IIconRegister ir);
}
