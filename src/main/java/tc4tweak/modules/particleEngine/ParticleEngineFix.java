package tc4tweak.modules.particleEngine;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.particle.Particle;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import thaumcraft.client.fx.ParticleEngine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.world.World;

public enum ParticleEngineFix {
    INSTANCE;
    private static final Field particlesField = FieldUtils.getDeclaredField(ParticleEngine.class, "particles", true);

    public HashMap<Integer, ArrayList<Particle>>[] getParticles() {
        return getParticles(ParticleEngine.instance);
    }

    @SuppressWarnings("unchecked")
    private HashMap<Integer, ArrayList<Particle>>[] getParticles(ParticleEngine instance) {
        try {
            return (HashMap<Integer, ArrayList<Particle>>[]) particlesField.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // flush the particles that don't belong to this current game session
    @SubscribeEvent
    public void onServerConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        for (HashMap<Integer, ArrayList<Particle>> map : getParticles()) {
            map.clear();
        }
    }

    // reset world to try to prevent weird crashes
    // still skeptical as whether this works at all
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (!world.isRemote) return;
        int dim = world.provider.getDimension();
        for (HashMap<Integer, ArrayList<Particle>> map : getParticles()) {
            ArrayList<Particle> list = map.get(dim);
            if (list == null) continue;
            for (Particle e : list) {
                try {
                    // Particle.world is a protected field; set it via reflection
                    FieldUtils.writeField(e, "world", world, true);
                } catch (IllegalAccessException ex) {
                    // ignore — best-effort reset
                }
            }
        }
    }

    public static void init() {
        FMLCommonHandler.instance().bus().register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }
}
