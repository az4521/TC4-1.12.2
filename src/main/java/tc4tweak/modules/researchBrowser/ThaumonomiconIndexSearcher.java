package tc4tweak.modules.researchBrowser;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import tc4tweak.CommonUtils;
import tc4tweak.ConfigurationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.client.gui.GuiResearchRecipe;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import org.lwjgl.opengl.GL11;

/**
 * Adapted from
 * <a href="https://github.com/GTNewHorizons/WitchingGadgets/blob/1.2.13-GTNH/src/main/java/witchinggadgets/client/ThaumonomiconIndexSearcher.java">Witching Gadgets</a>
 * Credit to BluSunrize for originally creating this class.
 * Slightly modified to adapt to research browser scaling functionality of this mod.
 */
public class ThaumonomiconIndexSearcher {
    private static final int mouseBufferIdent = 17;
    public static ThaumonomiconIndexSearcher instance;
    private static ByteBuffer mouseBuffer;
    private static Field f_mouseBuffer;
    private static GuiTextField thaumSearchField;
    private static int listDisplayOffset = 0;
    private static String searchCategory;
    private static List<SearchQuery> searchResults = new ArrayList<>();

    public static void init() {
        instance = new ThaumonomiconIndexSearcher();
        MinecraftForge.EVENT_BUS.register(instance);
        FMLCommonHandler.instance().bus().register(instance);
        f_mouseBuffer = CommonUtils.getField(Mouse.class, "readBuffer", mouseBufferIdent);
    }

    private static void initMouseEventBuffer() {
        try {
            mouseBuffer = (ByteBuffer) f_mouseBuffer.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static int getResultDisplayAreaWidth(GuiScreen gui) {
        return Math.min(gui.width - getResultDisplayAreaX(gui), 224);
    }

    private static int getResultDisplayAreaX(GuiScreen gui) {
        return gui.width / 2 + ConfigurationHandler.INSTANCE.getBrowserWidth() / 2 + (ResearchCategories.researchCategories.size() > BrowserPaging.getTabPerSide() ? 24 : 0);
    }

    private static void buildEntryList(String query) {
        if (query == null || query.isEmpty()) {
            searchResults.clear();
            return;
        }
        query = query.toLowerCase();
        List<SearchQuery> valids = new ArrayList<>();
        Set<String> keys;
        if (searchCategory != null && !searchCategory.isEmpty())
            keys = ResearchCategories.getResearchList(searchCategory).research.keySet();
        else {
            keys = new HashSet<>();
            for (ResearchCategoryList cat : ResearchCategories.researchCategories.values())
                keys.addAll(cat.research.keySet());
        }

        Set<SearchQuery> recipeBased = new HashSet<>();
        Set<String> usedResearches = new HashSet<>();
        for (String key : keys)
            if (key != null && !key.isEmpty() && ResearchCategories.getResearch(key) != null && ThaumcraftApiHelper.isResearchComplete(Minecraft.getMinecraft().player.getName(), key)) {
                if (ResearchCategories.getResearch(key).getName().startsWith("tc.research_name"))
                    continue;
                recipeBased.clear();
                ResearchPage[] pages = ResearchCategories.getResearch(key).getPages();
                if (pages != null)
                    for (ResearchPage page : pages) {
                        if (page.recipeOutput != null && page.recipeOutput.getDisplayName().toLowerCase().contains(query)) {
                            String dn;
                            if (page.recipeOutput.getItem() == ConfigItems.itemGolemCore) {
                                StringBuilder sb = new StringBuilder();
                                for (Object info : page.recipeOutput.getTooltip(Minecraft.getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL))
                                    sb.append(info).append(" ");
                                dn = sb.toString();
                            } else {
                                dn = page.recipeOutput.getDisplayName();
                            }
                            if (!usedResearches.contains(dn)) {
                                recipeBased.add(new SearchQuery(key, "Item: " + dn));
                                usedResearches.add(dn);
                            }
                        }
                    }
                boolean rAdded = false;
                if (recipeBased.size() <= 1) {
                    if (!usedResearches.contains(ResearchCategories.getResearch(key).getName()))
                        if (key.toLowerCase().contains(query) || ResearchCategories.getResearch(key).getName().toLowerCase().contains(query)) {
                            valids.add(new SearchQuery(key, null));
                            usedResearches.add(ResearchCategories.getResearch(key).getName());
                            rAdded = true;
                        }
                }
                if (!rAdded)
                    valids.addAll(recipeBased);
            }
        valids.sort(ResearchSorter.instance);
        searchResults = valids;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        searchResults.clear();
        if (ConfigurationHandler.INSTANCE.isAddResearchSearch() && event.getGui().getClass().getName().endsWith("GuiResearchBrowser")) {
            initMouseEventBuffer();
            int width = ConfigurationHandler.INSTANCE.getBrowserWidth();
            int height = ConfigurationHandler.INSTANCE.getBrowserHeight();
            thaumSearchField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, event.getGui().width / 2, event.getGui().height / 2 - height / 2 + 5, Math.min(width / 2 - 20, 120), 13);
            thaumSearchField.setTextColor(-1);
            thaumSearchField.setDisabledTextColour(-1);
            thaumSearchField.setEnableBackgroundDrawing(false);
            thaumSearchField.setMaxStringLength(40);
            Keyboard.enableRepeatEvents(true);
        }
    }

    @SubscribeEvent
    public void onGuiPreDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (thaumSearchField != null) {
            boolean cont = mouseBuffer.hasRemaining();
            if (Mouse.isCreated())
                if (cont) {
                    int mx = Mouse.getEventX() * event.getGui().width / Minecraft.getMinecraft().displayWidth;
                    int my = event.getGui().height - Mouse.getEventY() * event.getGui().height / Minecraft.getMinecraft().displayHeight - 1;
                    int button = Mouse.getEventButton();
                    int wheel = Mouse.getEventDWheel();
                    if (Mouse.getEventButtonState()) {
                        thaumSearchField.mouseClicked(mx, my, button);
                        if (thaumSearchField.isFocused() && button == 1) {
                            thaumSearchField.setText("");
                            searchResults.clear();
                        } else if (mx > (event.getGui().width / 2 + ConfigurationHandler.INSTANCE.getBrowserWidth() / 2 + (ResearchCategories.researchCategories.size() > BrowserPaging.getTabPerSide() ? 24 : 2)) && my > event.getGui().height / 2 - ConfigurationHandler.INSTANCE.getBrowserHeight() / 2 && my < event.getGui().height / 2 + ConfigurationHandler.INSTANCE.getBrowserHeight() / 2) {
                            int clicked = my - (event.getGui().height / 2 - ConfigurationHandler.INSTANCE.getBrowserHeight() / 2 + 6);
                            clicked /= 11;
                            int selected = clicked + listDisplayOffset;
                            if (selected < searchResults.size()) {
                                ResearchItem item = ResearchCategories.getResearch(searchResults.get(selected).research);
                                Minecraft.getMinecraft().displayGuiScreen(new GuiResearchRecipe(item, 0, item.displayColumn, item.displayRow));
                            }
                        }
                    } else if (wheel != 0 && mx > (event.getGui().width / 2 + ConfigurationHandler.INSTANCE.getBrowserWidth() / 2 + (ResearchCategories.researchCategories.size() > BrowserPaging.getTabPerSide() ? 24 : 2))) {
                        if (wheel < 0)
                            listDisplayOffset++;
                        else
                            listDisplayOffset--;
                        if (listDisplayOffset > searchResults.size() - 20)
                            listDisplayOffset = searchResults.size() - 20;
                        if (listDisplayOffset < 0)
                            listDisplayOffset = 0;
                    }
                }
        }
    }

    @SubscribeEvent
    public void onGuiPostDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (thaumSearchField != null) {
            int x = getResultDisplayAreaX(event.getGui());
            int y = event.getGui().height / 2 - ConfigurationHandler.INSTANCE.getBrowserHeight() / 2;
            int maxWidth = getResultDisplayAreaWidth(event.getGui());

            if (!searchResults.isEmpty()) {
                UtilsFX.bindTexture("textures/misc/parchment3.png");
                GlStateManager.enableBlend();
                Tessellator tes = Tessellator.getInstance();
                BufferBuilder tesBuf = tes.getBuffer();
                tesBuf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); // TODO_PORT: verify vertex format
                // TODO_PORT: unpack int color (opaque) into r,g,b and use buf...color() per vertex
                tesBuf.pos(x, y + 230, 0).tex(0, 150 / 256f).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color
        .endVertex();
                tesBuf.pos(x + maxWidth, y + 230, 0).tex(150 / 256f, 150 / 256f).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color
        .endVertex();
                tesBuf.pos(x + maxWidth, y, 0).tex(150 / 256f, 0).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color
        .endVertex();
                tesBuf.pos(x, y, 0).tex(0, 0).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color
        .endVertex();
                tes.draw();
            }
            UtilsFX.bindTexture("textures/gui/guiresearchtable2.png");
            GlStateManager.color(1,1,1);
            event.getGui().drawTexturedModalRect(thaumSearchField.x - 2, thaumSearchField.y - 4, 94, 8, thaumSearchField.width + 8, thaumSearchField.height);
            event.getGui().drawTexturedModalRect(thaumSearchField.x - 2, thaumSearchField.y + thaumSearchField.height - 4, 138, 158, thaumSearchField.width + 8, 2);
            event.getGui().drawTexturedModalRect(thaumSearchField.x + thaumSearchField.width + 6, thaumSearchField.y - 4, 244, 136, 2, thaumSearchField.height + 2);

            if ((searchResults == null || searchResults.isEmpty()) && !thaumSearchField.isFocused())
                event.getGui().drawString(Minecraft.getMinecraft().fontRenderer, I18n.translateToLocal("tc4tweaks.gui.search"), thaumSearchField.x, thaumSearchField.y, 0x777777);
            else
                for (int i = 0; i < 20; i++)
                    if (i + listDisplayOffset < searchResults.size()) {
                        String name = searchResults.get(listDisplayOffset + i).display != null ? searchResults.get(listDisplayOffset + i).display : ResearchCategories.getResearch(searchResults.get(listDisplayOffset + i).research).getName();
                        name = searchResults.get(listDisplayOffset + i).modifier + Minecraft.getMinecraft().fontRenderer.trimStringToWidth(name, maxWidth - 10);
                        Minecraft.getMinecraft().fontRenderer.drawString(name, x + 6, y + 6 + i * 11, 0xffffff, false);
                    }

            thaumSearchField.drawTextBox();
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (thaumSearchField != null) {
            thaumSearchField = null;
            mouseBuffer = null;
            Keyboard.enableRepeatEvents(false);
        }
    }

    @SubscribeEvent
    public void renderTick(TickEvent.ClientTickEvent event) {
        if (thaumSearchField != null)
            if (Keyboard.isCreated() && thaumSearchField.isFocused())
                while (Keyboard.next())
                    if (Keyboard.getEventKeyState()) {
                        if (Keyboard.getEventKey() == 1)
                            Minecraft.getMinecraft().displayGuiScreen(null);
                        else {
                            thaumSearchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
                            listDisplayOffset = 0;
                            if (ConfigurationHandler.INSTANCE.isLimitBookSearchToCategory())
                                searchCategory = Utils.getActiveCategory();
                            buildEntryList(thaumSearchField.getText());
                        }
                    }
    }

    private static class ResearchSorter implements Comparator<SearchQuery> {
        static final ResearchSorter instance = new ResearchSorter();

        @Override
        public int compare(SearchQuery o1, SearchQuery o2) {
            String c1 = o1.display != null ? o1.display : ResearchCategories.getResearch(o1.research).getName();
            String c2 = o2.display != null ? o2.display : ResearchCategories.getResearch(o2.research).getName();
            return c1.compareToIgnoreCase(c2);
        }
    }

    private static class SearchQuery {
        public final String research;
        public final String display;
        public final String modifier;

        public SearchQuery(String research, String display) {
            this.research = research;
            this.display = display;
            modifier = display != null ? TextFormatting.DARK_GRAY.toString() : "";
        }
    }
}