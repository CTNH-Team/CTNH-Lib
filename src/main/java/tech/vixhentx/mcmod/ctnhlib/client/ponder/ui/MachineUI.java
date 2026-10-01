package tech.vixhentx.mcmod.ctnhlib.client.ponder.ui;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.minecraft.world.level.block.Block;

/**
 * 一个可复用的机器界面描述对象：在模块里定义一次，在任意思索场景里重复摆放。
 *
 * <p>
 * 定义侧：
 *
 * <pre>{@code
 * 
 * private static final MachineUI LV_INPUT_BUS_UI = MachineUI.of(GTMachines.ITEM_IMPORT_BUS[GTValues.LV])
 *         .hidePlayerInventory()
 *         .scale(1.25f);
 * }</pre>
 *
 * <p>
 * 使用侧：
 *
 * <pre>{@code
 * scene.showUI(LV_INPUT_BUS_UI).at(anchor).forMachine(pos)
 *         .slot(0).withItem(new ItemStack(Items.GRASS_BLOCK, 64), 20)
 *         .show(200);
 * }</pre>
 *
 * <p>
 * 本对象只描述“画什么”，不持有机器实例：机器在场景运行时按坐标解析，所以同一个常量可以跨场景、跨重播
 * 复用。面板左下角对齐 {@link MachineUiPlacement#at(net.minecraft.world.phys.Vec3)} 传入的坐标。
 */
public final class MachineUI {

    private final MachineDefinition definition;
    private final boolean playerInventory;
    private final boolean titleBar;
    private final boolean sideTabs;
    private final boolean configurators;
    private final float scale;

    private MachineUI(MachineDefinition definition, boolean playerInventory, boolean titleBar, boolean sideTabs,
                      boolean configurators, float scale) {
        this.definition = definition;
        this.playerInventory = playerInventory;
        this.titleBar = titleBar;
        this.sideTabs = sideTabs;
        this.configurators = configurators;
        this.scale = scale;
    }

    /** 以 GT 机器定义创建界面描述。 */
    public static MachineUI of(MachineDefinition definition) {
        return new MachineUI(definition, true, true, true, true, 1.0f);
    }

    /** 以机器方块创建界面描述。 */
    public static MachineUI of(Block block) {
        if (block instanceof IMachineBlock machineBlock) {
            return of(machineBlock.getDefinition());
        }
        throw new IllegalArgumentException("Not a GT machine block: " + block);
    }

    /** 不画玩家背包，等价于 {@code IFancyUIProvider#hasPlayerInventory() == false} 的布局。 */
    public MachineUI hidePlayerInventory() {
        return new MachineUI(definition, false, titleBar, sideTabs, configurators, scale);
    }

    /** 不画 fancy UI 的标题栏。 */
    public MachineUI hideTitleBar() {
        return new MachineUI(definition, playerInventory, false, sideTabs, configurators, scale);
    }

    /** 不画 fancy UI 左侧的页签栏。 */
    public MachineUI hideSideTabs() {
        return new MachineUI(definition, playerInventory, titleBar, false, configurators, scale);
    }

    /** 不画 fancy UI 的配置器面板。 */
    public MachineUI hideConfigurators() {
        return new MachineUI(definition, playerInventory, titleBar, sideTabs, false, scale);
    }

    /** 面板缩放，1.0 即 GUI 原始像素。 */
    public MachineUI scale(float scale) {
        return new MachineUI(definition, playerInventory, titleBar, sideTabs, configurators, scale);
    }

    /** 开始一次摆放。 */
    public MachineUiPlacement in(SceneBuilder builder) {
        return new MachineUiPlacement(builder, this);
    }

    MachineDefinition definition() {
        return definition;
    }

    boolean playerInventory() {
        return playerInventory;
    }

    boolean titleBar() {
        return titleBar;
    }

    boolean sideTabs() {
        return sideTabs;
    }

    boolean configurators() {
        return configurators;
    }

    float scale() {
        return scale;
    }
}
