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
 * 用于构建 /ctnh 检查命令的点击复制聊天行的辅助工具。
 * 每行输出的文本在点击时会复制完整的 "label: value" 内容，并显示悬浮提示。
 */
public final class CTNHCommandChatHelper {

    /** 单个聊天行值的最大长度，超过该长度将拆分为多个块。 */
    public static final int MAX_VALUE_LINE_LENGTH = 256;

    private CTNHCommandChatHelper() {}

    /**
     * 为带标签的值构建一个或多个可点击的聊天行。长值将被拆分为
     * 额外的可复制续行，以避免聊天窗口静默丢弃内容。
     *
     * @param label 本地化标签
     * @param value 原始值字符串（不能为 null）
     * @return 一个或多个可发送的 {@link Component} 行
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
            // 每个块复制完整的 "label: chunk" 文本，以便用户粘贴完整信息。
            lines.add(buildLine(labelComp, chunk, chunkLabelText + ": " + chunk));
            chunkIndex++;
        }
        // 尾部摘要行（不可复制，仅用于提示信息）。
        lines.add(Component.translatable("command.ctnhlib.value.truncated", total)
                .withStyle(ChatFormatting.GRAY));
        return lines;
    }

    /**
     * 构建单个聊天行，其可见文本为 "label: value"，单击事件将逐字复制提供的 {@code copyText}。
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

    /** 分区标题行（无复制点击事件）。 */
    public static Component heading(Component component) {
        return component.copy().withStyle(ChatFormatting.GOLD);
    }

    /** 简单的信息提示行，例如"未包含流体"，无复制事件。 */
    public static Component info(Component component) {
        return component.copy().withStyle(ChatFormatting.GRAY);
    }

    /** 命令失败时使用的红色错误行。 */
    public static Component error(Component component) {
        return component.copy().withStyle(ChatFormatting.RED);
    }

    /**
     * 构建单个点击复制聊天行，其可见文本为提供的
     * {@code visible} 组件（无自动标签前缀），点击事件将复制提供的 {@code copyText}。
     */
    public static Component clickableLine(MutableComponent visible, String copyText) {
        Style style = Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("command.ctnhlib.copy.hover")));
        return visible.withStyle(style);
    }
}
