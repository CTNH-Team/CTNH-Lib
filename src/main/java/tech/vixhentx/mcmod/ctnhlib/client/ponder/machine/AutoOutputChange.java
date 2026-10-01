// SPDX-License-Identifier: GPL-3.0
// Copyright (C) 2026 mmyddd

package tech.vixhentx.mcmod.ctnhlib.client.ponder.machine;

import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

/**
 * 设置机器的物品/流体自动输出口朝向——只是改机器状态，和界面无关。
 *
 * <p>顺带把这一路自动输出打开：GT 给输出面画一个箭头，自动输出开着时再多一个标记，场景里才看得出来
 * 东西从哪一面出去。朝向与开关在回退时都还原。
 */
public final class AutoOutputChange implements MachineEdit {

    private final Direction side;
    private final boolean items;
    private final boolean fluids;
    @Nullable
    private Direction previousItems;
    @Nullable
    private Direction previousFluids;
    private boolean previousItemsAuto;
    private boolean previousFluidsAuto;
    private boolean itemsApplied;
    private boolean fluidsApplied;

    public AutoOutputChange(Direction side, boolean items, boolean fluids) {
        this.side = side;
        this.items = items;
        this.fluids = fluids;
    }

    @Override
    public void apply(MetaMachine machine, BlockPos machinePos) {
        AutoOutputTrait output = machine.getTrait(AutoOutputTrait.class);
        if (items) {
            if (output != null && output.hasAutoOutputItem()) {
                try {
                    previousItems = output.getOutputFacingItems();
                    previousItemsAuto = output.isAutoOutputItems();
                    output.setOutputFacingItems(side);
                    output.setAutoOutputItems(true);
                    itemsApplied = true;
                } catch (Throwable t) {
                    CTNHLib.LOGGER.error("CTNHLib: setting the item auto-output side of the machine at {} to {} " +
                            "threw", machinePos, side, t);
                }
            } else {
                report(machinePos, true, "this machine cannot auto-output items");
            }
        }
        if (fluids) {
            if (output != null && output.hasAutoOutputFluid()) {
                try {
                    previousFluids = output.getOutputFacingFluids();
                    previousFluidsAuto = output.isAutoOutputFluids();
                    output.setOutputFacingFluids(side);
                    output.setAutoOutputFluids(true);
                    fluidsApplied = true;
                } catch (Throwable t) {
                    CTNHLib.LOGGER.error("CTNHLib: setting the fluid auto-output side of the machine at {} to {} " +
                            "threw", machinePos, side, t);
                }
            } else {
                report(machinePos, false, "this machine cannot auto-output fluids");
            }
        }
    }

    @Override
    public void revert(MetaMachine machine, BlockPos machinePos) {
        AutoOutputTrait output = machine.getTrait(AutoOutputTrait.class);
        if (itemsApplied) {
            itemsApplied = false;
            if (output != null && output.hasAutoOutputItem()) {
                output.setOutputFacingItems(previousItems);
                output.setAutoOutputItems(previousItemsAuto);
            }
            previousItems = null;
        }
        if (fluidsApplied) {
            fluidsApplied = false;
            if (output != null && output.hasAutoOutputFluid()) {
                output.setOutputFacingFluids(previousFluids);
                output.setAutoOutputFluids(previousFluidsAuto);
            }
            previousFluids = null;
        }
    }

    private void report(BlockPos machinePos, boolean itemSide, String reason) {
        CTNHLib.LOGGER.error("CTNHLib: cannot set the {} auto-output side of the machine at {} to {}: {}",
                itemSide ? "item" : "fluid", machinePos, side, reason);
    }
}
