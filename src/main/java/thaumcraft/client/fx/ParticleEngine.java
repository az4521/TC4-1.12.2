package thaumcraft.client.fx;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class ParticleEngine {
   public static ParticleEngine instance = new ParticleEngine();
   public static final ResourceLocation particleTexture = new ResourceLocation("thaumcraft", "textures/misc/particles.png");
   public static final ResourceLocation particleTexture2 = new ResourceLocation("thaumcraft", "textures/misc/particles2.png");
   protected World world;
   private HashMap<Integer,ArrayList<Particle>>[] particles = new HashMap[]{new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>()};
   private Random rand = new Random();

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      float frame = event.getPartialTicks();
      Entity entity = Minecraft.getMinecraft().player;
      TextureManager renderer = Minecraft.getMinecraft().renderEngine;
      int dim = Minecraft.getMinecraft().world.provider.getDimension();
      renderer.bindTexture(particleTexture);
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.alphaFunc(516, 0.003921569F);
      boolean rebound = false;

      for(int layer = 0; layer < 4; ++layer) {
         if (this.particles[layer].containsKey(dim)) {
            ArrayList<Particle> parts = this.particles[layer].get(dim);
            if (!parts.isEmpty()) {
               if (!rebound && layer >= 2) {
                  renderer.bindTexture(particleTexture2);
                  rebound = true;
               }

               GlStateManager.pushMatrix();
               switch (layer) {
                  case 0:
                  case 2:
                     GlStateManager.blendFunc(770, 1);
                     break;
                  case 1:
                  case 3:
                     GlStateManager.blendFunc(770, 771);
               }

               float f1 = ActiveRenderInfo.getRotationX();
               float f2 = ActiveRenderInfo.getRotationZ();
               float f3 = ActiveRenderInfo.getRotationYZ();
               float f4 = ActiveRenderInfo.getRotationXY();
               float f5 = ActiveRenderInfo.getRotationXZ();
               Particle.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)frame;
               Particle.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)frame;
               Particle.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)frame;
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder buffer = tessellator.getBuffer();

               // Separate particles that manage their own buffer from simple ones
               ArrayList<Particle> simple = new ArrayList<>();
               ArrayList<Particle> complex = new ArrayList<>();
               for (Particle part : new ArrayList<>(parts)) {
                  if (part != null) {
                     String pkg = part.getClass().getPackage().getName();
                     if (pkg.contains(".beams") || pkg.contains(".bolt") || pkg.contains(".other") ||
                         part instanceof thaumcraft.client.fx.particles.FXBlockRunes ||
                         part instanceof thaumcraft.client.fx.particles.FXBurst) {
                        complex.add(part);
                     } else {
                        simple.add(part);
                     }
                  }
               }

               // Batch render simple particles
               if (!simple.isEmpty()) {
                  buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                  for (Particle part : simple) {
                     try {
                        part.renderParticle(buffer, entity, frame, f1, f5, f2, f3, f4);
                     } catch (Throwable ignored) {}
                  }
                  try { tessellator.draw(); } catch (Throwable ignored) {}
               }

               // Render complex particles individually — they manage their own begin/draw
               for (Particle part : complex) {
                  try {
                     part.renderParticle(buffer, entity, frame, f1, f5, f2, f3, f4);
                  } catch (Throwable ignored) {
                     try { tessellator.draw(); } catch (Throwable ignored2) {}
                  }
                  // Restore state for next particle
                  if (layer < 2) renderer.bindTexture(particleTexture);
                  else renderer.bindTexture(particleTexture2);
                  switch (layer) {
                     case 0: case 2: GlStateManager.blendFunc(770, 1); break;
                     case 1: case 3: GlStateManager.blendFunc(770, 771); break;
                  }
               }
               GlStateManager.popMatrix();
            }
         }
      }

      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.popMatrix();
   }

   public void addEffect(World world, Particle fx) {
      int dim = world.provider.getDimension();
      if (!this.particles[fx.getFXLayer()].containsKey(dim)) {
         this.particles[fx.getFXLayer()].put(dim, new ArrayList());
      }

      ArrayList<Particle> parts = this.particles[fx.getFXLayer()].get(dim);
      if (parts.size() >= 2000) {
         parts.remove(0);
      }

      parts.add(fx);
      this.particles[fx.getFXLayer()].put(dim, parts);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void updateParticles(TickEvent.ClientTickEvent event) {
      if (event.side != Side.SERVER) {
         Minecraft mc = FMLClientHandler.instance().getClient();
         World world = mc.world;
         if (world != null) {
            int dim = world.provider.getDimension();
            if (event.phase == Phase.START) {
               for(int layer = 0; layer < 4; ++layer) {
                  if (this.particles[layer].containsKey(dim)) {
                     ArrayList<Particle> parts = this.particles[layer].get(dim);

                     for(int j = 0; j < parts.size(); ++j) {
                        final Particle entityfx = parts.get(j);

                        try {
                           if (entityfx != null) {
                              entityfx.onUpdate();
                           }
                        } catch (Throwable throwable) {
                           CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
                           CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
                           crashreportcategory.addDetail("Particle", entityfx::toString);
                           crashreportcategory.addDetail("Particle Type", () -> "ENTITY_PARTICLE_TEXTURE");
                           throw new ReportedException(crashreport);
                        }

                        if (entityfx == null || !entityfx.isAlive()) {
                           parts.remove(j--);
                           this.particles[layer].put(dim, parts);
                        }
                     }
                  }
               }
            }

         }
      }
   }
}
