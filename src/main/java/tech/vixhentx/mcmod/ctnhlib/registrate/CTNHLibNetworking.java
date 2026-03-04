package tech.vixhentx.mcmod.ctnhlib.registrate;

import tech.vixhentx.mcmod.ctnhlib.network.packets.BlockHighlightPacket;

import static com.lowdragmc.lowdraglib.networking.LDLNetworking.NETWORK;

public class CTNHLibNetworking {

    public static void init() {
        NETWORK.registerS2C(BlockHighlightPacket.class);
    }
}
