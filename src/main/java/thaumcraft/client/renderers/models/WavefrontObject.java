package thaumcraft.client.renderers.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Wavefront OBJ model loader for direct GL rendering.
 * Replaces the removed CodeChickenLib/ForgeMultipart AdvancedModelLoader.
 */
public class WavefrontObject implements IModelCustom {
    private final List<float[]> vertices = new ArrayList<>();
    private final List<float[]> texCoords = new ArrayList<>();
    private final List<float[]> normals = new ArrayList<>();
    private final List<int[][]> faces = new ArrayList<>();
    private int displayList = -1;

    public WavefrontObject(ResourceLocation resource) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] parts = line.substring(2).trim().split("\\s+");
                    vertices.add(new float[]{Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])});
                } else if (line.startsWith("vt ")) {
                    String[] parts = line.substring(3).trim().split("\\s+");
                    texCoords.add(new float[]{Float.parseFloat(parts[0]), 1.0f - Float.parseFloat(parts[1])});
                } else if (line.startsWith("vn ")) {
                    String[] parts = line.substring(3).trim().split("\\s+");
                    normals.add(new float[]{Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])});
                } else if (line.startsWith("f ")) {
                    String[] parts = line.substring(2).trim().split("\\s+");
                    int[][] face = new int[parts.length][3];
                    for (int i = 0; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        face[i][0] = Integer.parseInt(indices[0]) - 1; // vertex
                        face[i][1] = indices.length > 1 && !indices[1].isEmpty() ? Integer.parseInt(indices[1]) - 1 : -1; // texcoord
                        face[i][2] = indices.length > 2 && !indices[2].isEmpty() ? Integer.parseInt(indices[2]) - 1 : -1; // normal
                    }
                    faces.add(face);
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("Failed to load OBJ model: " + resource);
            e.printStackTrace();
        }
    }

    private void compile() {
        displayList = GL11.glGenLists(1);
        GL11.glNewList(displayList, GL11.GL_COMPILE);
        // Use direct GL calls instead of Tessellator to avoid conflicts with shared singleton
        for (int[][] face : faces) {
            if (face.length == 3) {
                GL11.glBegin(GL11.GL_TRIANGLES);
            } else if (face.length == 4) {
                GL11.glBegin(GL11.GL_QUADS);
            } else {
                GL11.glBegin(GL11.GL_POLYGON);
            }
            for (int[] indices : face) {
                emitVertexGL(indices);
            }
            GL11.glEnd();
        }
        GL11.glEndList();
    }

    private void emitVertexGL(int[] indices) {
        if (indices[2] >= 0 && indices[2] < normals.size()) {
            float[] n = normals.get(indices[2]);
            GL11.glNormal3f(n[0], n[1], n[2]);
        }
        if (indices[1] >= 0 && indices[1] < texCoords.size()) {
            float[] t = texCoords.get(indices[1]);
            GL11.glTexCoord2f(t[0], t[1]);
        }
        float[] v = vertices.get(indices[0]);
        GL11.glVertex3f(v[0], v[1], v[2]);
    }

    @Override
    public void renderAll() {
        if (displayList == -1) {
            compile();
        }
        GL11.glCallList(displayList);
    }

    @Override
    public void renderPart(String partName) {
        renderAll();
    }

    @Override
    public void renderOnly(String... groupNames) {
        renderAll();
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames) {
        renderAll();
    }
}
