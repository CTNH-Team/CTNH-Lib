package tech.vixhentx.mcmod.ctnhlib.client.ponder;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.TagBuilder;
import net.minecraft.resources.ResourceLocation;

import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public final class CTNHPonderTagHelper {

    private CTNHPonderTagHelper() {}

    public static TagBuilder registerTag(CNRegistrate registrate,
                                         PonderTagRegistrationHelper<ResourceLocation> helper,
                                         ResourceLocation id,
                                         String en,
                                         String cn,
                                         String descriptionEn,
                                         String descriptionCn) {
        registrate.genLang(tagKey(id), en, cn);
        registrate.genLang(tagDescriptionKey(id), descriptionEn, descriptionCn);
        return helper.registerTag(id);
    }

    public static String tagKey(ResourceLocation id) {
        return id.getNamespace() + ".ponder.tag." + id.getPath();
    }

    public static String tagDescriptionKey(ResourceLocation id) {
        return tagKey(id) + ".description";
    }
}
