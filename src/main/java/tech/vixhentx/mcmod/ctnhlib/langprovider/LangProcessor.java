package tech.vixhentx.mcmod.ctnhlib.langprovider;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;
import org.objectweb.asm.Type;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.langprovider.annotation.*;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static tech.vixhentx.mcmod.ctnhlib.utils.EnvUtils.isDataGen;

public class


LangProcessor {
    final String modid;
    private final Consumer<TranslatedLang> genDataMethod;

    private static final Type EN_ANNOTATION = Type.getType(EN.class);
    private static final Type CN_ANNOTATION = Type.getType(CN.class);

    private static class AnnotationPair {
        ModFileScanData.AnnotationData enData;
        ModFileScanData.AnnotationData cnData;
    }

    public LangProcessor(String modid, Consumer<TranslatedLang> dataGenerator){
        this.modid = modid;
        genDataMethod = isDataGen ? dataGenerator : __ -> {};
    }
    public LangProcessor(CNRegistrate registrate){
        this(registrate.getModid(),
                (lang)->registrate.addRawLang(lang.key, lang.en_translation, lang.cn_translation));
    }

    public @Nullable ModFileScanData getScanDataForModId(String modId) {
        return ModList.get()
                .getModContainerById(modId) // 获取指定modid的ModContainer
                .map(ModContainer::getModInfo) // 获取ModInfo
                .map(IModInfo::getOwningFile) // 获取所属的IModFileInfo
                .map(IModFileInfo::getFile) // 获取IModFile
                .map(IModFile::getScanResult)
                .orElse(null); // 获取扫描结果
    }

    /** 全局扫描所有 @EN/@CN 字段 */
    public void processAll() {
        ModFileScanData scanData = getScanDataForModId(modid);
        if(scanData != null)
        {
            Map<String, AnnotationPair> annotationMap = new HashMap<>();

            // 第一阶段：收集所有注解
            scanData.getAnnotations().forEach(ann -> {
                if (ann.targetType() != ElementType.FIELD) return;

                String key = ann.clazz().getClassName() + "#" + ann.memberName();
                AnnotationPair pair = annotationMap.computeIfAbsent(key, k -> new AnnotationPair());

                if (ann.annotationType().equals(EN_ANNOTATION)) {
                    pair.enData = ann;
                } else if (ann.annotationType().equals(CN_ANNOTATION)) {
                    pair.cnData = ann;
                }
            });

            // 第二阶段：处理每个字段
            annotationMap.forEach((key, pair) -> {
                try {
                    processField(pair.enData, pair.cnData);
                } catch (Exception e) {
                    CTNHLib.LOGGER.error("Failed to process Lang field {}", key, e);
                }
            });
        }
    }


    private void processField(ModFileScanData.AnnotationData enData,
                              ModFileScanData.AnnotationData cnData) throws Exception {
        // 确定主注解数据（优先使用EN，其次CN）
        ModFileScanData.AnnotationData primaryData = enData != null ? enData : cnData;
        if (primaryData == null) return; // 理论上不会发生，因为annotationMap只存有注解的字段

        String className = primaryData.clazz().getClassName();
        String fieldName = primaryData.memberName();

        Class<?> clazz = Class.forName(className);
        Field field = clazz.getDeclaredField(fieldName);

        String itemKey = LangProcessUtils.getItemKey(field);
        String builtKey = buildKeyFromAnnotations(clazz, field, itemKey);

        // 处理翻译内容
        TranslatedLang[] langs = extractTranslations(enData, cnData, className, fieldName, builtKey);
        if (langs.length == 0) return;

        // 数据生成回调
        Arrays.stream(langs).filter(Objects::nonNull).forEach(genDataMethod);

        // 运行时注入（非datagen环境）
        if (true || !isDataGen) {
            injectFieldValue(field, langs);
        }
    }

    private TranslatedLang[] extractTranslations(ModFileScanData.AnnotationData enData,
                                                 ModFileScanData.AnnotationData cnData,
                                                 String className,
                                                 String fieldName,
                                                 String baseKey) {
        // 处理数组情况
        if (enData != null && LangProcessUtils.extractStringArray(enData).length > 1) {
            TranslatedLang[] langs = LangProcessUtils.getLocatedInfos(enData, cnData, className, fieldName);
            for (int i = 0; i < langs.length; i++) {
                if (langs[i] != null) {
                    langs[i].key = LangProcessUtils.buildKeyWithIndex(baseKey, i);
                }
            }
            return langs;
        }

        // 处理单值情况
        TranslatedLang lang = LangProcessUtils.getLocatedInfo(enData, cnData, className, fieldName);

        lang.key = baseKey;
        return new TranslatedLang[]{lang};
    }

    private void injectFieldValue(Field field, TranslatedLang[] langs) throws IllegalAccessException {
        field.setAccessible(true);
        try {
            if (field.getType().isArray()) {
                Lang[] erased = new Lang[langs.length];
                for (int i = 0; i < langs.length; i++) {
                    erased[i] = langs[i] != null ? langs[i].erase() : null;
                }
                field.set(null, erased);
            } else {
                field.set(null, langs[0] != null ? langs[0].erase() : null);
            }
        } finally {
            field.setAccessible(false);
        }
    }


    /** 构建键名（暂时只处理类级别注解，可扩展递归） */
    private String buildKeyFromAnnotations(Class<?> clazz, Field field, String itemKey) {
        LinkedList<String> prefixes = new LinkedList<>();
        LinkedList<String> suffixes = new LinkedList<>();

        Domain domainAnn = clazz.getAnnotation(Domain.class);
        Prefix prefixAnn = clazz.getAnnotation(Prefix.class);
        Suffix suffixAnn = clazz.getAnnotation(Suffix.class);

        String domain = (domainAnn != null) ? domainAnn.value() : modid;
        String root = (domainAnn != null) ? LangProcessUtils.getRoot(domainAnn, "") : "";
        String category = (domainAnn != null) ? LangProcessUtils.getCategory(domainAnn, clazz.getSimpleName()) : "";

        if (prefixAnn != null) prefixes.add(LangProcessUtils.getPrefix(prefixAnn, clazz::getSimpleName));
        if (suffixAnn != null) suffixes.add(LangProcessUtils.getSuffix(suffixAnn, clazz::getSimpleName));

        return LangProcessUtils.buildKey(prefixes, suffixes, domain, root, category, itemKey);
    }
}
