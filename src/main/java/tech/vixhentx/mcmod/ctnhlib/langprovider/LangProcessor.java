package tech.vixhentx.mcmod.ctnhlib.langprovider;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;

import com.ctnhlang.CN;
import com.ctnhlang.EN;
import com.ctnhlang.IgnoreLang;
import com.ctnhlang.langprovider.LangKeyBuilder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Map;

public class LangProcessor {

    private static final Type EN_ANNOTATION = Type.getType(EN.class);
    private static final Type CN_ANNOTATION = Type.getType(CN.class);
    private static final Type IGNORE_ANNOTATION = Type.getType(IgnoreLang.class);

    private final String modid;
    private final CNRegistrate registrate;

    private static final class AnnotationPair {

        ModFileScanData.AnnotationData enData;
        ModFileScanData.AnnotationData cnData;
        ModFileScanData.AnnotationData ignoreData;
    }

    public LangProcessor(CNRegistrate registrate) {
        this.modid = registrate.getModid();
        this.registrate = registrate;
    }

    public void processAll() {
        ModFileScanData scanData = getScanDataForModId(modid);
        if (scanData == null) {
            return;
        }

        Map<String, AnnotationPair> annotationMap = new HashMap<>();
        scanData.getAnnotations().forEach(annotation -> {
            if (annotation.targetType() != ElementType.FIELD) {
                return;
            }

            String key = annotation.clazz().getClassName() + "#" + annotation.memberName();
            AnnotationPair pair = annotationMap.computeIfAbsent(key, ignored -> new AnnotationPair());
            if (annotation.annotationType().equals(EN_ANNOTATION)) {
                pair.enData = annotation;
            } else if (annotation.annotationType().equals(CN_ANNOTATION)) {
                pair.cnData = annotation;
            } else if (annotation.annotationType().equals(IGNORE_ANNOTATION)) {
                pair.ignoreData = annotation;
            }
        });

        annotationMap.forEach((key, pair) -> {
            if (pair.ignoreData != null) {
                return;
            }
            try {
                processField(pair.enData, pair.cnData);
            } catch (Exception exception) {
                CTNHLib.LOGGER.error("Failed to process Lang field {}", key, exception);
            }
        });
    }

    public ModFileScanData getScanDataForModId(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(ModContainer::getModInfo)
                .map(IModInfo::getOwningFile)
                .map(IModFileInfo::getFile)
                .map(IModFile::getScanResult)
                .orElse(null);
    }

    private void processField(ModFileScanData.AnnotationData enData,
                              ModFileScanData.AnnotationData cnData) throws Exception {
        ModFileScanData.AnnotationData primary = enData != null ? enData : cnData;
        if (primary == null) {
            return;
        }

        ClassNode classNode = readClassNode(primary.clazz().getClassName());
        if (classNode == null) {
            throw new IllegalStateException("Unable to read class bytes for " + primary.clazz().getClassName());
        }
        FieldNode field = findField(classNode, primary.memberName());
        if (field == null) {
            throw new NoSuchFieldException(primary.clazz().getClassName() + "#" + primary.memberName());
        }
        String[] enValues = extractStringArray(enData);
        String[] cnValues = extractStringArray(cnData);

        if (field.desc.startsWith("[")) {
            int count = Math.max(enValues.length, cnValues.length);
            String[] keys = LangKeyBuilder.buildIndexedKeys(classNode, field, count, modid);
            for (int i = 0; i < count; i++) {
                String en = i < enValues.length ? enValues[i] : "";
                String cn = i < cnValues.length ? cnValues[i] : "";
                if (!en.isEmpty() || !cn.isEmpty()) {
                    registrate.addRawLang(keys[i], en, cn);
                }
            }
            if (enValues.length != cnValues.length && enValues.length != 0 && cnValues.length != 0) {
                CTNHLib.LOGGER.warn("Mismatched @EN/@CN array lengths on {}#{}", primary.clazz().getClassName(),
                        field.name);
            }
            return;
        }

        String key = LangKeyBuilder.buildKey(classNode, field, modid);
        String en = enValues.length > 0 ? enValues[0] : "";
        String cn = cnValues.length > 0 ? cnValues[0] : "";
        if (!en.isEmpty() || !cn.isEmpty()) {
            registrate.addRawLang(key, en, cn);
        }
    }

    private ClassNode readClassNode(String className) throws IOException {
        String resourceName = className.replace('.', '/') + ".class";
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                return null;
            }
            ClassNode classNode = new ClassNode();
            new ClassReader(stream).accept(classNode,
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return classNode;
        }
    }

    private FieldNode findField(ClassNode classNode, String fieldName) {
        for (FieldNode field : classNode.fields) {
            if (field.name.equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    private String[] extractStringArray(ModFileScanData.AnnotationData data) {
        if (data == null) {
            return new String[0];
        }
        Object value = data.annotationData().get("value");
        if (value == null) {
            return new String[0];
        }
        if (value instanceof String stringValue) {
            return new String[] { stringValue };
        }
        if (value instanceof String[] arrayValue) {
            return arrayValue;
        }
        if (value instanceof java.util.List<?> listValue) {
            return listValue.stream().map(Object::toString).toArray(String[]::new);
        }
        return new String[0];
    }
}
