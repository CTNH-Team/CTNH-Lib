package tech.vixhentx.mcmod.ctnhlib.client.ponder.ui;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link MachineUI} 的一次摆放：锚点、机器坐标与槽位写入计划。由
 * {@code CTNHPonderSceneBuilder#showUI(MachineUI)} 创建。
 */
public final class MachineUiPlacement {

    private final SceneBuilder builder;
    private final MachineUI ui;
    private final List<SlotWrite> writes = new ArrayList<>();
    private Vec3 anchor = Vec3.ZERO;
    private BlockPos machinePos;

    MachineUiPlacement(SceneBuilder builder, MachineUI ui) {
        this.builder = builder;
        this.ui = ui;
    }

    /** 锚点 = 面板左下角在场景中的位置。 */
    public MachineUiPlacement at(Vec3 anchor) {
        this.anchor = anchor;
        return this;
    }

    /** 面板展示的机器所在方块；缺省取锚点所在方块。 */
    public MachineUiPlacement forMachine(BlockPos machinePos) {
        this.machinePos = machinePos;
        return this;
    }

    /** 第 index 个机器槽位：顺序与 UI 内槽位顺序一致，也就是玩家的真实索引。 */
    public SlotTarget slot(int index) {
        return new SlotTarget(index);
    }

    /** 按给定 tick 数展示面板；此前登记的槽位写入按各自延迟执行。 */
    public void show(int ticks) {
        MachineUiElement element = new MachineUiElement(ui, anchor, machinePos, List.copyOf(writes));
        builder.addInstruction(new ShowMachineUiInstruction(element, ticks));
    }

    public final class SlotTarget {

        private final int index;

        private SlotTarget(int index) {
            this.index = index;
        }

        /** 面板出现的同一 tick 就把物品写进该槽位。 */
        public MachineUiPlacement withItem(ItemStack stack) {
            return withItem(stack, 0);
        }

        /** 面板出现 delayTicks 个 tick 后，物品飞入该槽位并写入机器。 */
        public MachineUiPlacement withItem(ItemStack stack, int delayTicks) {
            writes.add(new SlotWrite(index, stack.copy(), Math.max(0, delayTicks)));
            return MachineUiPlacement.this;
        }
    }

    record SlotWrite(int index, ItemStack stack, int delayTicks) {}
}
