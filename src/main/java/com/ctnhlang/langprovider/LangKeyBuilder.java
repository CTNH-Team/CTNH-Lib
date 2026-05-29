package com.ctnhlang.langprovider;

import com.ctnhlang.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class LangKeyBuilder {

    private static final String CATEGORY_DESC = Type.getDescriptor(Category.class);
    private static final String DOMAIN_DESC = Type.getDescriptor(Domain.class);
    private static final String PREFIX_DESC = Type.getDescriptor(Prefix.class);
    private static final String SUFFIX_DESC = Type.getDescriptor(Suffix.class);
    private static final String KEY_DESC = Type.getDescriptor(Key.class);

    private LangKeyBuilder() {}

    public static String buildKey(Class<?> ownerClass, Field field, String fallbackModId) {
        String explicitKey = getExplicitKey(field);
        if (explicitKey != null) {
            return explicitKey;
        }
        return buildKey(fromClass(ownerClass), fallbackModId, field.getName().toLowerCase(Locale.ROOT));
    }

    public static String[] buildIndexedKeys(Class<?> ownerClass, Field field, int count, String fallbackModId) {
        return buildIndexedKeys(buildKey(ownerClass, field, fallbackModId), count);
    }

    public static String buildKey(ClassNode classNode, FieldNode field, String fallbackModId) {
        String explicitKey = getExplicitKey(field);
        if (explicitKey != null) {
            return explicitKey;
        }
        return buildKey(fromClassNode(classNode), fallbackModId, field.name.toLowerCase(Locale.ROOT));
    }

    public static String[] buildIndexedKeys(ClassNode classNode, FieldNode field, int count, String fallbackModId) {
        return buildIndexedKeys(buildKey(classNode, field, fallbackModId), count);
    }

    public static String[] buildIndexedKeys(String baseKey, int count) {
        if (count <= 0) {
            return new String[0];
        }
        if (count == 1) {
            return new String[] { baseKey };
        }
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = baseKey + "." + i;
        }
        return keys;
    }

    private static String buildKey(ClassMetadata metadata, String fallbackModId, String itemKey) {
        StringJoiner joiner = new StringJoiner(".");

        if (!metadata.prefix.isEmpty()) {
            joiner.add(metadata.prefix);
        }

        if (!metadata.domain.isEmpty()) {
            joiner.add(metadata.domain);
        }

        String root = metadata.root.isEmpty() ? fallbackModId : metadata.root;
        if (!root.isEmpty()) {
            joiner.add(root);
        }
        if (!metadata.category.isEmpty()) {
            joiner.add(metadata.category);
        }

        joiner.add(itemKey);
        if (!metadata.suffix.isEmpty()) {
            joiner.add(metadata.suffix);
        }
        return joiner.toString();
    }

    private static ClassMetadata fromClass(Class<?> ownerClass) {
        Category category = ownerClass.getAnnotation(Category.class);
        Domain domain = ownerClass.getAnnotation(Domain.class);
        Prefix prefix = ownerClass.getAnnotation(Prefix.class);
        Suffix suffix = ownerClass.getAnnotation(Suffix.class);
        return new ClassMetadata(
                domain != null ? domain.value() : "",
                "",
                resolveCategory(domain != null ? domain.value() : "", category != null ? category.value() : "",
                        ownerClass.getSimpleName()),
                prefix != null ? resolveAffix(prefix.value(), ownerClass.getSimpleName()) : "",
                suffix != null ? resolveAffix(suffix.value(), ownerClass.getSimpleName()) : "");
    }

    private static ClassMetadata fromClassNode(ClassNode classNode) {
        String domain = "";
        String root = "";
        String category = "";
        String prefix = "";
        String suffix = "";

        if (classNode.visibleAnnotations != null) {
            for (AnnotationNode annotation : classNode.visibleAnnotations) {
                if (DOMAIN_DESC.equals(annotation.desc)) {
                    domain = getAnnotationValue(annotation, "value");
                } else if (CATEGORY_DESC.equals(annotation.desc)) {
                    category = getAnnotationValue(annotation, "value");
                } else if (PREFIX_DESC.equals(annotation.desc)) {
                    prefix = resolveAffix(getAnnotationValue(annotation, "value"), simpleName(classNode.name));
                } else if (SUFFIX_DESC.equals(annotation.desc)) {
                    suffix = resolveAffix(getAnnotationValue(annotation, "value"), simpleName(classNode.name));
                }
            }
        }

        return new ClassMetadata(domain, root, resolveCategory(domain, category, simpleName(classNode.name)), prefix,
                suffix);
    }

    private static String getExplicitKey(Field field) {
        Key key = field.getAnnotation(Key.class);
        return key != null ? key.value() : null;
    }

    private static String getExplicitKey(FieldNode field) {
        if (field.visibleAnnotations == null) {
            return null;
        }
        for (AnnotationNode annotation : field.visibleAnnotations) {
            if (KEY_DESC.equals(annotation.desc)) {
                return getAnnotationValue(annotation, "value");
            }
        }
        return null;
    }

    private static String resolveCategory(String domain, String explicitCategory, String className) {
        if (explicitCategory != null && !explicitCategory.isEmpty()) {
            return explicitCategory;
        }

        String rawName = className.toLowerCase(Locale.ROOT);
        if (!domain.isEmpty()) {
            String domainName = domain.toLowerCase(Locale.ROOT);
            if (rawName.endsWith(domainName)) {
                return rawName.substring(0, rawName.length() - domainName.length());
            }
            if (rawName.startsWith(domainName + "_")) {
                return rawName.substring(domainName.length() + 1);
            }
        }
        return rawName;
    }

    private static String resolveAffix(String explicitValue, String className) {
        return explicitValue == null || explicitValue.isEmpty() ? className.toLowerCase(Locale.ROOT) : explicitValue;
    }

    private static String simpleName(String internalName) {
        int slash = internalName.lastIndexOf('/');
        return slash >= 0 ? internalName.substring(slash + 1) : internalName;
    }

    static String getAnnotationValue(AnnotationNode annotation, String key) {
        if (annotation.values == null) {
            return "";
        }
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (key.equals(annotation.values.get(i))) {
                Object value = annotation.values.get(i + 1);
                return value instanceof String ? (String) value : "";
            }
        }
        return "";
    }

    static String[] getStringArray(AnnotationNode annotation) {
        if (annotation == null || annotation.values == null) {
            return new String[0];
        }
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (!"value".equals(annotation.values.get(i))) {
                continue;
            }
            Object value = annotation.values.get(i + 1);
            if (value instanceof String stringValue) {
                return new String[] { stringValue };
            }
            if (value instanceof List<?> list) {
                String[] result = new String[list.size()];
                for (int j = 0; j < list.size(); j++) {
                    result[j] = String.valueOf(list.get(j));
                }
                return result;
            }
        }
        return new String[0];
    }

    private record ClassMetadata(String domain, String root, String category, String prefix, String suffix) {}
}
