// SPDX-License-Identifier: GPL-3.0
// Copyright (C) 2026 mmyddd

package tech.vixhentx.mcmod.ctnhlib.client.ponder.ui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ProgrammableCircuitSlotTrait;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * 取机器上的编程电路槽（fork 版把这一路做成了 trait：{@link ProgrammableCircuitSlotTrait}）。
 *
 * <p>上游 GT 是 {@code IHasCircuitSlot#getCircuitInventory()}，fork 里 trait 自己拿着一个
 * {@code CustomItemStackHandler}，字段是私有的，这里反射读一次并缓存。
 */
final class CircuitSlots {

    @Nullable
    private static Field storageField;
    private static boolean storageFieldMissing;

    private CircuitSlots() {}

    /** 机器的电路槽；机器没有这一路（没有 trait 或反射拿不到）时返回 null。 */
    static @Nullable ItemStackHandler storage(@Nullable MetaMachine machine) {
        if (machine == null) {
            return null;
        }
        ProgrammableCircuitSlotTrait trait = machine.getTrait(ProgrammableCircuitSlotTrait.class);
        Field field = storageField();
        if (trait == null || field == null) {
            return null;
        }
        try {
            return (ItemStackHandler) field.get(trait);
        } catch (Throwable t) {
            return null;
        }
    }

    /** 机器认不认编程电路。 */
    static boolean hasCircuit(@Nullable MetaMachine machine) {
        return storage(machine) != null;
    }

    /** 当前电路（没有电路就是空栈）。 */
    static ItemStack circuit(@Nullable MetaMachine machine) {
        ItemStackHandler handler = storage(machine);
        return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(0);
    }

    private static @Nullable Field storageField() {
        if (storageField == null && !storageFieldMissing) {
            try {
                Field field = ProgrammableCircuitSlotTrait.class.getDeclaredField("storage");
                field.setAccessible(true);
                storageField = field;
            } catch (Throwable t) {
                storageFieldMissing = true;
                tech.vixhentx.mcmod.ctnhlib.CTNHLib.LOGGER.error(
                        "CTNHLib: cannot read the circuit storage of the fork trait; circuit UI will be skipped", t);
            }
        }
        return storageField;
    }
}