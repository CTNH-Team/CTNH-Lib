package tech.vixhentx.mcmod.ctnhlib.network.packets;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.PacketIntLocation;

import net.minecraft.core.BlockPos;

import lombok.NoArgsConstructor;
import tech.vixhentx.mcmod.ctnhlib.client.render.ColorData;
import tech.vixhentx.mcmod.ctnhlib.client.render.highlight.HighlightHandler;

@NoArgsConstructor
public class BlockHighlightPacket extends PacketIntLocation {

    public BlockHighlightPacket(BlockPos blockPos) {
        super(blockPos);
    }

    @Override
    public void execute(IHandlerContext handler) {
        super.execute(handler);
        var level = handler.getLevel();
        if (level == null || pos == null || !level.isLoaded(pos)) {
            return;
        }
        HighlightHandler.highlight(
                pos,
                level.dimension(),
                System.currentTimeMillis() + 10000,
                ColorData.RED);
    }
}
