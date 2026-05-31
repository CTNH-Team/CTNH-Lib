package tech.vixhentx.mcmod.ctnhlib.client.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.registration.PonderLocalization;

public final class CTNHPonderLang {

    private CTNHPonderLang() {
    }

    public static void init(PonderPlugin plugin) {
        PonderIndex.addPlugin(plugin);
        PonderIndex.registerAll();
        if (PonderIndex.getLangAccess() instanceof PonderLocalization localization) {
            localization.generateSceneLang();
        }
    }
}
