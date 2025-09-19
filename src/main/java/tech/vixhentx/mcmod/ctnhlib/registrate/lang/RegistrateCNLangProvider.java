package tech.vixhentx.mcmod.ctnhlib.registrate.lang;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;

public class RegistrateCNLangProvider extends LanguageProvider implements RegistrateProvider {
    private final AbstractRegistrate<?> owner;

    public RegistrateCNLangProvider(AbstractRegistrate<?> owner, PackOutput packOutput) {
        super(packOutput, owner.getModid(), "zh_cn");
        this.owner = owner;
    }

    @Override @NotNull
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    protected void addTranslations() {
        owner.genData(CNLANG, this);
    }
}
