package thaumcraft.common.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thaumcraft.common.Thaumcraft;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Thaumcraft.MOD_ID)
public final class SoundsTC {
   private static final String[] THAUMCRAFT_SOUNDS = new String[]{
      "heartbeat", "spill", "bubble", "alembicknock", "creak", "squeek", "golemironshoot",
      "jar", "swarm", "swarmattack", "fly", "key", "doorfail", "cameraticks",
      "cameraclack", "pump", "page", "learn", "write", "erase", "brain", "crystal",
      "wispdead", "wisplive", "wand", "wandfail", "rumble", "ice", "jacobs",
      "hhoff", "hhon", "pech_idle", "pech_trade", "pech_dice", "pech_hit",
      "pech_death", "pech_charge", "shock", "fireloop", "zap", "craftfail",
      "craftstart", "runicShieldEffect", "runicShieldCharge", "swing", "wind",
      "tool", "gore", "roots", "tentacle", "upgrade", "whispers", "monolith",
      "infuser", "infuserstart", "egidle", "egattack", "egdeath", "egscreech",
      "crabclaw", "crabdeath", "crabtalk", "chant", "coins", "urnbreak", "evilportal"
   };
   private static final Map<String, String> LEGACY_SOUND_REMAP = new HashMap<>();

   static {
      LEGACY_SOUND_REMAP.put("random.fizz", "minecraft:block.fire.extinguish");
      LEGACY_SOUND_REMAP.put("minecraft:random.fizz", "minecraft:block.fire.extinguish");
      LEGACY_SOUND_REMAP.put("game.neutral.swim", "minecraft:entity.generic.swim");
      LEGACY_SOUND_REMAP.put("minecraft:game.neutral.swim", "minecraft:entity.generic.swim");
      LEGACY_SOUND_REMAP.put("liquid.lavapop", "minecraft:block.lava.pop");
      LEGACY_SOUND_REMAP.put("minecraft:liquid.lavapop", "minecraft:block.lava.pop");
      LEGACY_SOUND_REMAP.put("random.orb", "minecraft:entity.experience_orb.pickup");
      LEGACY_SOUND_REMAP.put("minecraft:random.orb", "minecraft:entity.experience_orb.pickup");
      LEGACY_SOUND_REMAP.put("random.bow", "minecraft:entity.arrow.shoot");
      LEGACY_SOUND_REMAP.put("minecraft:random.bow", "minecraft:entity.arrow.shoot");
      LEGACY_SOUND_REMAP.put("mob.endermen.portal", "minecraft:entity.endermen.teleport");
      LEGACY_SOUND_REMAP.put("minecraft:mob.endermen.portal", "minecraft:entity.endermen.teleport");
   }

   private SoundsTC() {
   }

   @SubscribeEvent
   public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
      for (String name : THAUMCRAFT_SOUNDS) {
         ResourceLocation id = new ResourceLocation(Thaumcraft.MOD_ID, name);
         event.getRegistry().register(new SoundEvent(id).setRegistryName(id));
      }
   }

   public static SoundEvent get(String id) {
      String remapped = LEGACY_SOUND_REMAP.getOrDefault(id, id);
      ResourceLocation rl = remapped.indexOf(':') >= 0 ? new ResourceLocation(remapped) : new ResourceLocation(Thaumcraft.MOD_ID, remapped);
      return SoundEvent.REGISTRY.getObject(rl);
   }

   public static SoundEvent get(String domain, String path) {
      return get(domain + ":" + path);
   }
}
