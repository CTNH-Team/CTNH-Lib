package tech.vixhentx.mcmod.ctnhlib.api;

import net.minecraft.world.item.DyeColor;

import java.util.Map;

public class CTNHValues {

    public static final Map<DyeColor, String> DYE_COLOR_CN = Map.ofEntries(
            Map.entry(DyeColor.WHITE, "白色"),
            Map.entry(DyeColor.ORANGE, "橙色"),
            Map.entry(DyeColor.MAGENTA, "品红色"),
            Map.entry(DyeColor.LIGHT_BLUE, "淡蓝色"),
            Map.entry(DyeColor.YELLOW, "黄色"),
            Map.entry(DyeColor.LIME, "黄绿色"),
            Map.entry(DyeColor.PINK, "粉红色"),
            Map.entry(DyeColor.GRAY, "灰色"),
            Map.entry(DyeColor.LIGHT_GRAY, "淡灰色"),
            Map.entry(DyeColor.CYAN, "青色"),
            Map.entry(DyeColor.PURPLE, "紫色"),
            Map.entry(DyeColor.BLUE, "蓝色"),
            Map.entry(DyeColor.BROWN, "棕色"),
            Map.entry(DyeColor.GREEN, "绿色"),
            Map.entry(DyeColor.RED, "红色"),
            Map.entry(DyeColor.BLACK, "黑色"));

    public static final String[] VNC = new String[] {
            "超低压",
            "低压",
            "中压",
            "高压",
            "超高压",
            "强导压",
            "剧差压",
            "零点压",
            "极限压",
            "极高压",
            "极超压",
            "极巨压",
            "极顶压",
            "过载压",
            "上限压",
    };

    public static final String[] VNCF = new String[] {
            "§8超低压",
            "§7低压",
            "§b中压",
            "§6高压",
            "§5超高压",
            "§9强导压",
            "§d剧差压",
            "§c零点压",
            "§3极限压",
            "§4极高压",
            "§a极超压",
            "§2极巨压",
            "§e极顶压",
            "§9§l过载压",
            "§c§l上限压",
    };
}
