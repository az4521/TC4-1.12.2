package thaumcraft.client.renderers.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

/**
 * Reimplementation of 1.7.10's IIconRegister for 1.12.2.
 * registerSprite returns a TextureAtlasSprite from the block texture atlas.
 * All thaumcraft block textures are pre-registered during TextureStitchEvent.
 */
public interface IIconRegister {
    TextureAtlasSprite registerSprite(String name);

    /**
     * Default implementation that looks up sprites from the block texture atlas.
     */
    class Impl implements IIconRegister {
        private static final Impl INSTANCE = new Impl();

        public static Impl getInstance() { return INSTANCE; }

        @Override
        public TextureAtlasSprite registerSprite(String name) {
            ResourceLocation rl;
            if (name.contains(":")) {
                String[] parts = name.split(":", 2);
                rl = new ResourceLocation(parts[0], "blocks/" + parts[1]);
            } else {
                rl = new ResourceLocation("thaumcraft", "blocks/" + name);
            }
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());
        }
    }
}
