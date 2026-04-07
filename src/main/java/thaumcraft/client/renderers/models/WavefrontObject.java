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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Wavefront OBJ model loader for direct GL rendering.
 * Replaces the removed CodeChickenLib/ForgeMultipart AdvancedModelLoader.
 */
public class WavefrontObject implements IModelCustom {
    private final List<float[]> vertices = new ArrayList<>();
    private final List<float[]> texCoords = new ArrayList<>();
    private final List<float[]> normals = new ArrayList<>();
    private final List<int[][]> faces = new ArrayList<>();
    private final List<String> faceGroups = new ArrayList<>();
    private int displayList = -1;
    private final Map<String, Integer> groupDisplayLists = new HashMap<>();

    public WavefrontObject(ResourceLocation resource) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream()));
            String line;
            String currentGroup = "default";
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
                } else if (line.startsWith("g ")) {
                    String[] parts = line.substring(2).trim().split("\\s+");
                    currentGroup = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "default";
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
                    faceGroups.add(currentGroup);
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
        renderFaces(null);
        GL11.glEndList();

        for (String group : faceGroups) {
            if (!groupDisplayLists.containsKey(group)) {
                int list = GL11.glGenLists(1);
                GL11.glNewList(list, GL11.GL_COMPILE);
                renderFaces(group);
                GL11.glEndList();
                groupDisplayLists.put(group, list);
            }
        }
    }

    private void renderFaces(String group) {
        // Use direct GL calls instead of Tessellator to avoid conflicts with shared singleton
        for (int i = 0; i < faces.size(); i++) {
            if (group != null && !group.equals(faceGroups.get(i))) {
                continue;
            }
            int[][] face = faces.get(i);
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
        if (displayList == -1) {
            compile();
        }
        Integer list = groupDisplayLists.get(partName);
        if (list != null) {
            GL11.glCallList(list);
        }
    }

    @Override
    public void renderOnly(String... groupNames) {
        if (displayList == -1) {
            compile();
        }
        for (String groupName : groupNames) {
            renderPart(groupName);
        }
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames) {
        if (displayList == -1) {
            compile();
        }
        outer:
        for (Map.Entry<String, Integer> entry : groupDisplayLists.entrySet()) {
            for (String excludedGroupName : excludedGroupNames) {
                if (entry.getKey().equals(excludedGroupName)) {
                    continue outer;
                }
            }
            GL11.glCallList(entry.getValue());
        }
    }
}
