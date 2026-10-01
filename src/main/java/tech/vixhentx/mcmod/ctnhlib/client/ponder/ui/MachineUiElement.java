package tech.vixhentx.mcmod.ctnhlib.client.ponder.ui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.AnimatedOverlayElementBase;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.systems.RenderSystem;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;

import java.util.ArrayList;
import java.util.List;

/**
 * 把机器真实的 {@link ModularUI} 画进思索场景的叠加层元素：按锚点投影定位、以面板左下角为原点，
 * 并按时间线把物品写进指定槽位。
 *
 * <p>
 * 机器实例在渲染/运行期按坐标解析，不跨重播持有；场景重播（{@code PonderScene#begin()} 会重建
 * BlockEntity）时自动重建界面并重放写入。
 */
public class MachineUiElement extends AnimatedOverlayElementBase {

    /** 叠在场景之上、文本框之下。 */
    private static final float Z = 250f;
    private static final int FLIGHT_TICKS = 10;
    private static final float MIN_FADE = 1 / 16f;
    /** 传给 GUI 的鼠标坐标：远在面板之外，避开所有 hover / 拖拽分支。 */
    private static final int OUTSIDE = -10000;

    private final MachineUI ui;
    private final Vec3 anchor;
    private final BlockPos machinePos;
    private final List<MachineUiPlacement.SlotWrite> writes;
    private final boolean[] written;

    private Resolved resolved;
    private boolean failed;
    private int ticksShown;

    MachineUiElement(MachineUI ui, Vec3 anchor, BlockPos machinePos, List<MachineUiPlacement.SlotWrite> writes) {
        this.ui = ui;
        this.anchor = anchor;
        this.machinePos = machinePos == null ? BlockPos.containing(anchor) : machinePos;
        this.writes = writes;
        this.written = new boolean[writes.size()];
    }

    @Override
    public void tick(PonderScene scene) {
        if (failed) {
            return;
        }
        ticksShown++;
        Resolved current = resolve(scene);
        if (current == null) {
            return;
        }
        current.modularUi().mainGroup.updateScreen();
        applyScheduledWrites(current);
    }

    @Override
    public void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
        if (failed || fade < MIN_FADE) {
            return;
        }
        Resolved current = resolve(scene);
        if (current == null) {
            return;
        }
        try {
            Vec2 projected = scene.getTransform().sceneToScreen(anchor, partialTicks);
            float scale = ui.scale();
            float width = current.width() * scale;
            float height = current.height() * scale;
            float x = Mth.clamp(projected.x, 8, Math.max(8, screen.width - width - 8));
            float y = Mth.clamp(projected.y - height, 8, Math.max(8, screen.height - height - 8));

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, Z);
            graphics.pose().scale(scale, scale, 1);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, fade);
            current.modularUi().mainGroup.drawInBackground(graphics, OUTSIDE, OUTSIDE, partialTicks);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            renderFlyingItems(graphics, current);
            graphics.pose().popPose();
        } catch (Throwable t) {
            fail("rendering the machine UI", t);
        }
    }

    private void applyScheduledWrites(Resolved current) {
        for (int i = 0; i < writes.size(); i++) {
            if (written[i]) {
                continue;
            }
            MachineUiPlacement.SlotWrite write = writes.get(i);
            if (ticksShown < write.delayTicks() + FLIGHT_TICKS) {
                continue;
            }
            SlotWidget slot = slotAt(current, write.index());
            if (slot == null) {
                continue;
            }
            slot.setItem(write.stack().copy());
            written[i] = true;
        }
    }

    private void renderFlyingItems(GuiGraphics graphics, Resolved current) {
        for (int i = 0; i < writes.size(); i++) {
            if (written[i]) {
                continue;
            }
            MachineUiPlacement.SlotWrite write = writes.get(i);
            float elapsed = ticksShown - write.delayTicks();
            if (elapsed < 0 || elapsed >= FLIGHT_TICKS) {
                continue;
            }
            SlotWidget slot = slotAt(current, write.index());
            if (slot == null) {
                continue;
            }
            float progress = Mth.clamp(elapsed / (float) FLIGHT_TICKS, 0f, 1f);
            float eased = progress * progress * (3 - 2 * progress);
            float startX = -6f;
            float startY = current.height() + 14f;
            float endX = slot.getPositionX() - current.originX() + 1f;
            float endY = slot.getPositionY() - current.originY() + 1f;
            GuiGameElement
                    .of(write.stack()).<GuiGameElement.GuiRenderBuilder>at(Mth.lerp(eased, startX, endX),
                            Mth.lerp(eased, startY, endY))
                    .scale(1)
                    .render(graphics);
        }
    }

    private static SlotWidget slotAt(Resolved current, int index) {
        if (index < 0 || index >= current.machineSlots().size()) {
            return null;
        }
        return current.machineSlots().get(index);
    }

    private Resolved resolve(PonderScene scene) {
        BlockEntity blockEntity = scene.getWorld().getBlockEntity(machinePos);
        if (blockEntity == null) {
            return null;
        }
        if (resolved != null && resolved.blockEntity() == blockEntity) {
            return resolved;
        }
        try {
            Resolved built = build(blockEntity);
            if (built == null) {
                return null;
            }
            // BlockEntity 被重建（场景重播 / 跳步）后，槽位写入需要重放。
            for (int i = 0; i < written.length; i++) {
                written[i] = false;
            }
            resolved = built;
            return built;
        } catch (Throwable t) {
            fail("building the machine UI", t);
            return null;
        }
    }

    private Resolved build(BlockEntity blockEntity) {
        if (!(blockEntity instanceof IMachineBlockEntity holder)) {
            return null;
        }
        MetaMachine machine = holder.getMetaMachine();
        if (!(machine instanceof IUIMachine uiMachine)) {
            return null;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        if (ui.definition() != null && machine.getDefinition() != ui.definition()) {
            CTNHLib.LOGGER.warn("MachineUI was defined for {} but {} sits at {}", ui.definition(),
                    machine.getDefinition(), machinePos);
        }

        ModularUI modularUi = uiMachine.createUI(player);
        if (modularUi == null) {
            return null;
        }
        modularUi.initWidgets();

        Widget root = pickRoot(modularUi);
        if (root instanceof FancyMachineUIWidget fancy) {
            applyFancyChrome(fancy);
        }
        List<SlotWidget> slots = collectMachineSlots(modularUi);
        CTNHLib.LOGGER.debug("MachineUI at {}: panel {}x{} at ({}, {}), {} machine slot(s)", machinePos,
                root.getSizeWidth(), root.getSizeHeight(), root.getPositionX(), root.getPositionY(), slots.size());
        return new Resolved(blockEntity, modularUi, root.getPositionX(), root.getPositionY(), root.getSizeWidth(),
                root.getSizeHeight(), slots);
    }

    private void applyFancyChrome(FancyMachineUIWidget fancy) {
        if (!ui.playerInventory()) {
            PlayerInventoryWidget inventory = fancy.getPlayerInventory();
            if (inventory != null && inventory.isVisible()) {
                inventory.setVisible(false);
                fancy.setSize(fancy.getSizeWidth(), Math.max(0, fancy.getSizeHeight() - inventory.getSizeHeight()));
            }
        }
        if (!ui.titleBar() && fancy.getTitleBar() != null) {
            fancy.getTitleBar()
                    .setVisible(false);
        }
        if (!ui.sideTabs() && fancy.getSideTabsWidget() != null) {
            fancy.getSideTabsWidget()
                    .setVisible(false);
        }
        if (!ui.configurators() && fancy.getConfiguratorPanel() != null) {
            fancy.getConfiguratorPanel()
                    .setVisible(false);
        }
    }

    private static Widget pickRoot(ModularUI modularUi) {
        if (modularUi.mainGroup.widgets.size() == 1 &&
                modularUi.mainGroup.widgets.get(0) instanceof FancyMachineUIWidget fancy) {
            return fancy;
        }
        return modularUi.mainGroup;
    }

    private static List<SlotWidget> collectMachineSlots(ModularUI modularUi) {
        List<SlotWidget> slots = new ArrayList<>();
        for (Widget widget : modularUi.mainGroup.getContainedWidgets(true)) {
            if (widget instanceof SlotWidget slot && !slot.isPlayerContainer && slot.isVisible()) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private void fail(String phase, Throwable throwable) {
        if (failed) {
            return;
        }
        failed = true;
        CTNHLib.LOGGER.error("MachineUI element failed while {} (machine at {})", phase, machinePos, throwable);
    }

    private record Resolved(BlockEntity blockEntity, ModularUI modularUi, int originX, int originY, int width,
                            int height, List<SlotWidget> machineSlots) {}
}
