package tech.vixhentx.mcmod.ctnhlib.utils.mapping;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MappingImpl implements Function<String,String>{
    public final Map<String,String> map;
    public MappingImpl(int size) {
        map = new HashMap<>(size);
    }
    public MappingImpl() {
        map = new HashMap<>();
    }

    public static String mapMethodDesc(String desc, Function<String,String> mapper){

        StringBuilder buffer = new StringBuilder("(");

        String[] strings = toDescList(desc);
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.startsWith("[")) {

                String replace = string.replace("[", "");
                if (!isDefaultClass(replace)) {
                    String substring = replace.substring(1, replace.length() - 1);
                    strings[i] = "[".repeat(countStr(string, "[")) + "L" + mapper.apply(substring) + ";";
                }
            }else if (!isDefaultClass(string)) {
                strings[i] = "L"+mapper.apply(string.substring(1, string.length() - 1))+";";
            }
        }

        for (int i = 0; i < strings.length; i++) {
            if (i == strings.length-1){
                buffer.append(")");
                buffer.append(strings[i]);
                continue;
            }
            buffer.append(strings[i]);
        }
        return buffer.toString();
    }

    public static int mapLocalSignatureEnd(String signature, int pos, int max){
        int i = pos;
        while (i++<max) {
            var c = signature.charAt(i);
            switch (c) {
                case '<':
                case ';':
                case '+':
                case '-':
                case '*':
                case '(':
                case ')':
                case ',':
                    return i;
            }
        }
        throw new IllegalArgumentException("signature not end with ';' or '<' or '+' or '-' or '*'");
    }
    public static int mapLocalSignatureName(String signature, int pos, int max){

        int i = pos;
        while ((i+=1)<max) {
            var c = signature.charAt(i);
            switch (c) {
                case '<':
                case '+':
                case '-':
                case '*':
                case '(':
                case ')':
                case '/':
                case ';':
                    return -1;
                case ',':
                case ':':
                    return i;
            }
        }
        return -1;
    }
    public static String mapLocalSignature(String signature, Function<String,String> mapper){
        boolean contains = signature.contains("<");
        if (signature.endsWith(";") || contains){
            if (contains){
                StringBuilder buffer = new StringBuilder();
                int max = signature.length();
                int i = 0;
                while (i<max){
                    var c = signature.charAt(i);
                    switch (c){
                        case '(':
                        case ')':
                        case '+':
                        case '-':
                        case '*':
                            buffer.append(c);
                            i++;
                            continue;
                        case '<':
                        case ',':
                        case ';': {
                            buffer.append(c);
                            i++;
                            int e = mapLocalSignatureName(signature, i, max);
                            if (e > 0){
                                buffer.append(signature, i, e);
                                i = e;

                            }

                            continue;
                        }
                        case 'L': {
                            buffer.append(c);
                            int e = mapLocalSignatureEnd(signature, i+=1, max);
                            buffer.append(mapper.apply(signature.substring(i, e)));
                            i = e;
                            continue;
                        }
                        default:
                            buffer.append(c);
                            i++;
                    }
                }
                return buffer.toString();
            }else {
                if (signature.startsWith("L")){

                    var s = signature.substring(1, signature.length() - 1);
                    if (s.length() > 1){
                        if (isDefaultClass(s)){
                            return signature;
                        }
                    }
                    return "L"+mapper.apply(s)+";";
                }else {
                    return signature;
                }
            }
        }else {
            return signature;
        }
    }
    public String mapLocalSignature(String signature){
        return mapLocalSignature(signature, this);
    }

    public String mapMethodDesc(String desc){
        StringBuilder buffer = new StringBuilder("(");
        _mapMethodDesc(desc, buffer);
        return buffer.toString();
    }
    public String _mapMethodDesc(String desc){
        StringBuilder buffer = new StringBuilder(desc.charAt(0) == '(' ? "(" : "");
        _mapMethodDesc(desc, buffer);
        return buffer.toString();
    }
    public void _mapMethodDesc(String desc, StringBuilder buffer){

        String[] strings = toDescList(desc);
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string.startsWith("[")) {

                String replace = string.replace("[", "");
                if (!isDefaultClass(replace)) {
                    String substring = replace.substring(1, replace.length() - 1);
                    strings[i] = "[".repeat(countStr(string, "[")) + "L" + mapClass(substring) + ";";
                }
            }else if (!isDefaultClass(string)) {
                strings[i] = "L"+mapClass(string.substring(1, string.length() - 1))+";";
            }
        }

        for (int i = 0; i < strings.length; i++) {
            if (i == strings.length-1){
                buffer.append(")");
                buffer.append(strings[i]);
                continue;
            }
            buffer.append(strings[i]);
        }
        //log.info(buffer.toString());
    }
    public String[] mapMethod(String name){
        String orDefault = map_(name);
        String[] split = orDefault.split("\\.");
        String[] split1 = split[1].split("\\(");
        return new String[]{split[0],split1[0],"("+split1[1]};
    }
    // 有的没实现这个
    public String[] mapMethodNull(String name){
        String orDefault = mapNull_(name);
        if (orDefault == null) return null;
        String[] split = orDefault.split("\\.");
        String[] split1 = split[1].split("\\(");
        return new String[]{split[0],split1[0],"("+split1[1]};
    }

    public String[] mapField(String name) {
        String orDefault = map_(name);
        String[] split = orDefault.split("\\.");
        return new String[]{split[0], split[1]};
    }
    public String map_(String name) {
        return map.getOrDefault(name, name);
    }
    // 有的没实现这个
    public String mapNull_(String name) {
        return map.get(name);
    }
    /**
     * 映射类名
     * @param name 类名，格式为全限定名，例如：java/lang/Object
     * @return 映射后的类名
     */
    public String mapClass(String name){
        return map_(name);
    }

    @Override
    public String apply(String s) {
        return mapClass(s);
    }



    public static final Gson GSON = new Gson();

    public static record MethodInfo(String name, String desc, int nameHash, int descHash) {
        public MethodInfo(String name, String desc) {
            this(name, desc, name.hashCode(), desc.hashCode());
        }
    }
    public static record FieldInfo(String name, int nameHash) {
        public FieldInfo(String name) {
            this(name, name.hashCode());
        }
    }
    /**
     * 获取容许mixin的class哈希校验数据
     * @param classData 修改的数据
     */
    public static byte[] toMixinClassHashCheckDataByte(ClassNode classData){
        toMixinClassHashCheckData(classData);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classData.accept(classWriter);
        return classWriter.toByteArray();
    }
    /**
     * 获取容许mixin的class哈希校验数据
     * @param classFile 类数据
     */
    public static byte[] toMixinClassHashCheckDataByte(byte[] classFile){
        ClassNode classData = new ClassNode();
        new ClassReader(classFile).accept(classData, 0);
        toMixinClassHashCheckData(classData);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classData.accept(classWriter);
        return classWriter.toByteArray();
    }
    /**
     * 获取容许mixin的class哈希校验数据
     * @param classData 修改的数据
     */
    public static void toMixinClassHashCheckData(ClassNode classData){
        List<MethodInfo> methodInfos = new ArrayList<>();
        List<FieldInfo> fieldInfos = new ArrayList<>();
        List<FieldNode> fieldNodes = new ArrayList<>();
        for (MethodNode method : classData.methods) {
            if (method.visibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode visibleAnnotation : method.visibleAnnotations) {
                if (visibleAnnotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;")) {
                    methodInfos.add(new MethodInfo(method.name, method.desc));
                    method.name = "mixinFix";
                    //移除这个注释
                    method.visibleAnnotations.remove(visibleAnnotation);
                    break;
                }
            }
        }
        for (FieldNode field : classData.fields) {
            if (field.visibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode visibleAnnotation : field.visibleAnnotations) {
                if (visibleAnnotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;")) {
                    //不知道会怎么样
                    fieldInfos.add(new FieldInfo(field.name));
                    fieldNodes.add(field);
                    //移除这个注释
                    field.visibleAnnotations.remove(visibleAnnotation);
                    break;
                }
            }
        }
        classData.fields = fieldNodes;
        for (MethodNode method : classData.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode methodInsnNode) {
                    for (MethodInfo methodInfo : methodInfos) {
                        if (methodInfo.nameHash == methodInsnNode.name.hashCode() && methodInfo.descHash == methodInsnNode.desc.hashCode()) {
                            if (methodInsnNode.name.equals(methodInfo.name) && methodInsnNode.desc.equals(methodInfo.desc)) {
                                methodInsnNode.name = "";
                            }
                        }
                    }
                }else if (instruction instanceof FieldInsnNode fieldInsnNode) {
                    for (FieldInfo fieldInfo : fieldInfos) {
                        if (fieldInfo.nameHash == fieldInsnNode.name.hashCode()) {
                            if (fieldInsnNode.name.equals(fieldInfo.name)) {
                                fieldInsnNode.name = "";
                            }
                        }
                    }
                }else if (instruction instanceof InvokeDynamicInsnNode invokeDynamicInsnNode) {
                    Handle bsm = invokeDynamicInsnNode.bsm;
                    if (bsm.getOwner().equals(classData.name)) {
                        for (MethodInfo methodInfo : methodInfos) {
                            if (methodInfo.nameHash == bsm.getName().hashCode() && methodInfo.descHash == bsm.getDesc().hashCode()) {
                                if (bsm.getName().equals(methodInfo.name) && bsm.getDesc().equals(methodInfo.desc)) {
                                    invokeDynamicInsnNode.bsm = new Handle(
                                            bsm.getTag(),
                                            bsm.getOwner(),
                                            "",
                                            bsm.getDesc(),
                                            bsm.isInterface()
                                    );
                                }
                            }
                        }
                    }
                    if (invokeDynamicInsnNode.bsmArgs != null) {
                        for (int i = 0; i < invokeDynamicInsnNode.bsmArgs.length; i++) {
                            Object bsmArg = invokeDynamicInsnNode.bsmArgs[i];
                            if (bsmArg instanceof Handle handle) {
                                switch (handle.getTag()) {
                                    case Opcodes.H_INVOKESTATIC, Opcodes.H_INVOKESPECIAL, Opcodes.H_INVOKEVIRTUAL, Opcodes.H_NEWINVOKESPECIAL, Opcodes.H_INVOKEINTERFACE -> {
                                        for (MethodInfo methodInfo : methodInfos) {
                                            if (methodInfo.nameHash == handle.getName().hashCode() && methodInfo.descHash == handle.getDesc().hashCode()) {
                                                if (handle.getName().equals(methodInfo.name) && handle.getDesc().equals(methodInfo.desc)) {
                                                    invokeDynamicInsnNode.bsmArgs[i] = new Handle(
                                                            handle.getTag(),
                                                            handle.getOwner(),
                                                            "",
                                                            handle.getDesc(),
                                                            handle.isInterface()
                                                    );
                                                }
                                            }
                                        }
                                    }
                                    case Opcodes.H_GETFIELD, Opcodes.H_GETSTATIC, Opcodes.H_PUTFIELD, Opcodes.H_PUTSTATIC -> {
                                        for (FieldInfo fieldInfo : fieldInfos) {
                                            if (fieldInfo.nameHash == handle.getName().hashCode()) {
                                                if (handle.getName().equals(fieldInfo.name)) {
                                                    invokeDynamicInsnNode.bsmArgs[i] = new Handle(
                                                            handle.getTag(),
                                                            handle.getOwner(),
                                                            "",
                                                            handle.getDesc(),
                                                            handle.isInterface()
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     *
     * @param longStr 长字符串
     * @param mixStr 子字符串
     * @return 包含个数
     */
    public static int countStr(String longStr, String mixStr) {
        //如果确定传入的字符串不为空，可以把下面这个判断去掉，提高执行效率
//        if(longStr == null || mixStr == null || "".equals(longStr.trim()) || "".equals(mixStr.trim()) ){
//             return 0;
//        }
        int count = 0;
        int index = 0;
        while((index = longStr.indexOf(mixStr,index))!= -1){
            index = index + mixStr.length();
            count++;
        }
        return count;
    }

    public static final Pattern descIs = Pattern.compile("(|\\[+)(L(.+);|[VBZCSIFDJ])",Pattern.UNIX_LINES);
    public static String[] toDescList(String methodDescriptor){

        Matcher matcher = descIs.matcher(methodDescriptor.replaceAll("[()]", "").replace(";",";\n"));
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches.toArray(new String[0]);/*
        CharList bytes = new CharArrayList();
        List<String> ret = new ArrayList<>();
        boolean isClass = false;
        int a = 0;
        for (char aByte : methodDescriptor.toCharArray()) {
            if (aByte == '[') a++;
            else  {

                if (!isClass && aByte == 'L') {
                    isClass = true;
                }else if (isClass) {
                    if (aByte != ';')
                        bytes.add(aByte);
                    else {
                        ret.add("[".repeat(a)+new String(bytes.toCharArray()));
                        a=0;
                        isClass = false;
                    }
                } else {
                    ret.add("[".repeat(a)+String.valueOf(aByte));
                }
            }
        }
        return ret.toArray(new String[0]);*/
    }

    public static boolean isDefaultClass(String text) {
        return switch (text.replace("[","")){
            case "B", "J", "C", "Z", "F", "I", "S", "D", "V" -> true;
            default ->  false;
        };
    }
}
