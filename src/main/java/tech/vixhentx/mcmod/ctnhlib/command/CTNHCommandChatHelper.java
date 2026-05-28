package tech.vixhentx.mcmod.ctnhlib.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for building click-to-copy chat lines used by /ctnh inspection commands.
 * Each emitted line copies the full "label: value" text on click and shows a hover tooltip.
 */
public final class CTNHCommandChatHelper {

    /** Maximum length of a single chat line value before it is split into chunks. */
    public static final int MAX_VALUE_LINE_LENGTH = 256;

    private CTNHCommandChatHelper() {}

    /**
     * Build one or more clickable chat lines for a labeled value. Long values are split into
     * additional copyable continuation lines so chat does not silently drop content.
     *
     * @param label localized label
     * @param value raw value string (must not be null)
     * @return one or more {@link Component} lines ready to be sent
     */
    public static List<Component> labeledLines(Component label, String value) {
        List<Component> lines = new ArrayList<>();
        String safe = value == null ? "" : value;
        String labelText = label.getString();
        if (safe.length() <= MAX_VALUE_LINE_LENGTH) {
            lines.add(buildLine(label, safe, labelText + ": " + safe));
            return lines;
        }
        int total = safe.length();
        int chunkIndex = 0;
        for (int start = 0; start < total; start += MAX_VALUE_LINE_LENGTH) {
            int end = Math.min(total, start + MAX_VALUE_LINE_LENGTH);
            String chunk = safe.substring(start, end);
            MutableComponent labelComp = chunkIndex == 0 ?
                    label.copy() :
                    label.copy().append(Component.literal(" [" + (chunkIndex + 1) + "]"));
            String chunkLabelText = labelComp.getString();
            // Each chunk copies the full "label: chunk" text, so the user can paste complete information.
            lines.add(buildLine(labelComp, chunk, chunkLabelText + ": " + chunk));
            chunkIndex++;
        }
        // Trailing summary line (not copyable, just informative).
        lines.add(Component.translatable("command.ctnhlib.value.truncated", total)
                .withStyle(ChatFormatting.GRAY));
        return lines;
    }

    /**
     * Build a single chat line whose visible text is "label: value" and whose click event
     * copies the supplied {@code copyText} verbatim.
     */
    public static Component buildLine(Component label, String value, String copyText) {
        MutableComponent labelStyled = label.copy().withStyle(ChatFormatting.AQUA);
        MutableComponent valueStyled = Component.literal(value).withStyle(ChatFormatting.WHITE);
        MutableComponent line = Component.empty()
                .append(labelStyled)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(valueStyled);

        Style style = Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("command.ctnhlib.copy.hover")));
        return line.withStyle(style);
    }

    /** Section heading line (no copy click event). */
    public static Component heading(Component component) {
        return component.copy().withStyle(ChatFormatting.GOLD);
    }

    /** A simple informational line such as "no fluid contained" with no copy event. */
    public static Component info(Component component) {
        return component.copy().withStyle(ChatFormatting.GRAY);
    }

    /** A red error line used by command failure paths. */
    public static Component error(Component component) {
        return component.copy().withStyle(ChatFormatting.RED);
    }

    /**
     * Build a single click-to-copy chat line whose visible text is the supplied
     * {@code visible} component (no automatic label prefix), and whose click event
     * copies the supplied {@code copyText}.
     */
    public static Component clickableLine(MutableComponent visible, String copyText) {
        Style style = Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("command.ctnhlib.copy.hover")));
        return visible.withStyle(style);
    }
}
