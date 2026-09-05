package tech.vixhentx.mcmod.ctnhlib.registrate.data;

import com.tterrag.registrate.providers.ProviderType;
import tech.vixhentx.mcmod.ctnhlib.registrate.lang.RegistrateCNLangProvider;

public class ProviderTypes {

    /**
     * 中文 lang provider 的 Registrate {@code ProviderType}。
     * <p>
     * 注册 id 必须是 CTNH-Lib 独占的：{@code ProviderType.register} 内部是
     * {@code RegistrateDataProvider.TYPES.put(name, type)}，同名后注册者会直接顶掉先注册者。第三方 mod
     * <b>ae2pw</b>（AE2 Pattern Workstation）内置了一份本类的拷贝，也用朴素的 {@code "cnlang"} 注册；
     * 一旦它赢下这个 id，{@code RegistrateDataProvider} 建出来的 zh_cn 子 provider 就属于 ae2pw 那条链，
     * 只会排空挂在它自己 {@code ProviderType} 下的生成器，{@code @CN} 注解经 {@code LangProcessor}
     * 写入本类 {@code CNLANG} 的条目一条都读不到。此时 provider 的 {@code data} 为空，Forge 的
     * {@code LanguageProvider.run()} 直接 return —— zh_cn.json 不写盘，且不报错、不警告。
     * <p>
     * 因此这里带 {@code ctnhlib_} 前缀独占命名，避免与任何第三方拷贝撞车。
     */
    public static ProviderType<RegistrateCNLangProvider> CNLANG = ProviderType.register("ctnhlib_cnlang",
            (p, e) -> new RegistrateCNLangProvider(p, e.getGenerator().getPackOutput()));
}
