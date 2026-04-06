package tc4tweak.config;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.client.config.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.List;

import static net.minecraftforge.fml.client.config.GuiUtils.INVALID;
import static net.minecraftforge.fml.client.config.GuiUtils.VALID;
import static tc4tweak.CommonUtils.reflectGet;

public class StringOrderingEntry extends GuiEditArrayEntries.StringEntry {
    private static final String[] upLabels = {"↑", "5", "↟"};
    private static final String[] downLabels = {"↓", "↓5", "↡"};
    private static final Field field_GuiEditArray_enabled = ReflectionHelper.findField(GuiEditArray.class, "enabled");
    private final GuiButtonExt btnMoveUp, btnMoveDown;
    private final HoverChecker moveUpHoverChecker, moveDownHoverChecker;
    private final List<List<String>> moveUpToolTip, moveDownToolTip;

    public StringOrderingEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value) {
        super(owningScreen, owningEntryList, configElement, value);
        boolean enabled = reflectGet(field_GuiEditArray_enabled, owningScreen);
        btnMoveUp = new GuiButtonExt(0, 0, 0, 18, 18, "↑");
        btnMoveUp.packedFGColour = GuiUtils.getColorCode('7', true);
        btnMoveUp.enabled = enabled;
        btnMoveDown = new GuiButtonExt(0, 0, 0, 18, 18, "↓");
        btnMoveDown.packedFGColour = GuiUtils.getColorCode('7', true);
        btnMoveDown.enabled = enabled;
        moveUpHoverChecker = new HoverChecker(btnMoveUp, 800);
        moveDownHoverChecker = new HoverChecker(btnMoveDown, 800);
        moveUpToolTip = ImmutableList.of(
                ImmutableList.of(I18n.format("tc4tweaks.configgui.tooltip.moveUp")),
                ImmutableList.of(I18n.format("tc4tweaks.configgui.tooltip.moveUp.shift")),
                ImmutableList.of(I18n.format("tc4tweaks.configgui.tooltip.moveUp.ctrl"))
        );
        moveDownToolTip = ImmutableList.of(
                ImmutableList.of(I18n.format("tc4tweaks.configgui.tooltip.moveDown")),
                ImmutableList.of(I18n.format("tc4tweaks.configgui.tooltip.moveDown.shift")),
                ImmutableList.of(I18n.format("tc4tweaks.configgui.tooltip.moveDown.ctrl"))
        );
        textFieldValue.width -= 44;
    }

    private static int getKeyboardState(int normal, int shift, int ctrl) {
        return GuiScreen.isCtrlKeyDown() ? ctrl : GuiScreen.isShiftKeyDown() ? shift : normal;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (getValue() != null && isValidated)
            mc.fontRenderer.drawString(
                    isValidValue ? TextFormatting.GREEN + VALID : TextFormatting.RED + INVALID,
                    listWidth / 4 - mc.fontRenderer.getStringWidth(VALID) - 2,
                    y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2,
                    16777215);

        int labelIndex = getKeyboardState(0, 1, 2);
        int half = listWidth / 2;
        if (owningEntryList.canAddMoreEntries) {
            btnAddNewEntryAbove.visible = true;
            btnAddNewEntryAbove.x =half + ((half / 2) - 44);
            btnAddNewEntryAbove.y =y;
            btnAddNewEntryAbove.drawButton(mc, mouseX, mouseY, partialTicks);
        } else
            btnAddNewEntryAbove.visible = false;
        if (!configElement.isListLengthFixed() && slotIndex != owningEntryList.listEntries.size() - 1) {
            btnRemoveEntry.visible = true;
            btnRemoveEntry.x =half + ((half / 2) - 22);
            btnRemoveEntry.y =y;
            btnRemoveEntry.drawButton(mc, mouseX, mouseY, partialTicks);
        } else
            btnRemoveEntry.visible = false;
        if (configElement.isListLengthFixed() || slotIndex != owningEntryList.listEntries.size() - 1) {
            textFieldValue.setVisible(true);
            textFieldValue.y =y + 1;
            textFieldValue.drawTextBox();
        } else
            textFieldValue.setVisible(false);
        if (slotIndex > 0) {
            btnMoveUp.visible = true;
            btnMoveUp.x =half + half / 2 - 88;
            btnMoveUp.y =y;
            btnMoveUp.displayString = upLabels[labelIndex];
            btnMoveUp.drawButton(mc, mouseX, mouseY, partialTicks);
        } else
            btnMoveUp.visible = false;
        if (slotIndex < owningEntryList.listEntries.size() - 2) {
            btnMoveDown.visible = true;
            btnMoveDown.x =half + half / 2 - 66;
            btnMoveDown.y =y;
            btnMoveDown.displayString = downLabels[labelIndex];
            btnMoveDown.drawButton(mc, mouseX, mouseY, partialTicks);
        } else
            btnMoveDown.visible = false;
    }

    @Override
    public void drawToolTip(int mouseX, int mouseY) {
        super.drawToolTip(mouseX, mouseY);
        int labelIndex = getKeyboardState(0, 1, 2);
        boolean canHover = mouseY < owningEntryList.bottom && mouseY > owningEntryList.top;
        if (btnMoveUp.visible && moveUpHoverChecker.checkHover(mouseX, mouseY, canHover))
            owningScreen.drawToolTip(moveUpToolTip.get(labelIndex), mouseX, mouseY);
        if (btnMoveDown.visible && moveDownHoverChecker.checkHover(mouseX, mouseY, canHover))
            owningScreen.drawToolTip(moveDownToolTip.get(labelIndex), mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        int delta = getKeyboardState(1, 5, owningEntryList.listEntries.size());
        Minecraft mc = Minecraft.getMinecraft();
        if (btnAddNewEntryAbove.mousePressed(mc, x, y)) {
            btnAddNewEntryAbove.playPressSound(mc.getSoundHandler());
            // forge bug: you cannot just use addEntry as that one has no support for custom IArrayEntry
            owningEntryList.listEntries.add(index, new StringOrderingEntry(owningScreen, owningEntryList, owningEntryList.configElement, ""));
            owningEntryList.canAddMoreEntries = !owningEntryList.configElement.isListLengthFixed()
                    && (owningEntryList.configElement.getMaxListLength() == -1 || owningEntryList.listEntries.size() - 1 < owningEntryList.configElement.getMaxListLength());
            owningEntryList.recalculateState();
            return true;
        } else if (btnRemoveEntry.mousePressed(mc, x, y)) {
            btnRemoveEntry.playPressSound(mc.getSoundHandler());
            owningEntryList.removeEntry(index);
            owningEntryList.recalculateState();
            return true;
        } else if (btnMoveUp.mousePressed(mc, x, y)) {
            btnMoveUp.playPressSound(mc.getSoundHandler());
            GuiEditArrayEntries.IArrayEntry e = owningEntryList.listEntries.remove(index);
            owningEntryList.listEntries.add(Math.max(index - delta, 0), e);
            owningEntryList.recalculateState();
            return true;
        } else if (btnMoveDown.mousePressed(mc, x, y)) {
            btnMoveDown.playPressSound(mc.getSoundHandler());
            GuiEditArrayEntries.IArrayEntry e = owningEntryList.listEntries.remove(index);
            owningEntryList.listEntries.add(Math.min(index + delta, owningEntryList.listEntries.size()), e);
            owningEntryList.recalculateState();
            return true;
        }
        return false;
    }
}
