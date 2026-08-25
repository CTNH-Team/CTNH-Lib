package tech.vixhentx.mcmod.ctnhlib.utils.mapping;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static tech.vixhentx.mcmod.ctnhlib.utils.mapping.MappingImpl.isDefaultClass;

public class MappingTransformer {
    protected final MappingImpl mappingImpl;
    
    public MappingTransformer(MappingImpl mappingImpl) {
        this.mappingImpl = mappingImpl;
    }

    public ClassNode transform(ClassNode input) {
        return transform(input, mappingImpl);
    }
    public ClassNode transform(ClassNode input, MappingImpl mappingImpl) {
        // 映射类名
        String mappedClassName = mappingImpl.mapClass(input.name);
        if (input.signature != null) {
            String signature = mappingImpl.mapLocalSignature(input.signature);
            input.signature = signature;
        }
        if (!mappedClassName.equals(input.name)) {
            input.name = mappedClassName;
        }
        
        // 映射父类名
        if (input.superName != null) {
            String mappedSuperName = mappingImpl.mapClass(input.superName);
            if (!mappedSuperName.equals(input.superName)) {
                input.superName = mappedSuperName;
            }
        }
        
        // 映射接口名
        for (int i = 0; i < input.interfaces.size(); i++) {
            String interfaceName = input.interfaces.get(i);
            String mappedInterfaceName = mappingImpl.mapClass(interfaceName);
            if (!mappedInterfaceName.equals(interfaceName)) {
                input.interfaces.set(i, mappedInterfaceName);
            }
        }
        
        // 映射字段名
        for (FieldNode field : input.fields) {
            String fullFieldName = input.name + "." + field.name;
            String[] mappedField = mappingImpl.mapField(fullFieldName);
            if (mappedField != null && mappedField.length > 1 && !mappedField[1].equals(field.name)) {
                field.name = mappedField[1];
            }
            if (field.signature != null) {
                String signature = mappingImpl.mapLocalSignature(field.signature);
                field.signature = signature;
            }
            field.desc = mapFieldDescriptor(field.desc);
        }
        
        // 映射方法名和描述符
        for (MethodNode method : input.methods) {
            if (method.signature != null) {
                method.signature = mappingImpl.mapLocalSignature(method.signature);
            }
            //// 跳过构造方法
            //if (method.name.equals("<init>") || method.name.equals("<clinit>")) {
            //    continue;
            //}
            String fullMethodName = input.name + "." + method.name + method.desc;
            String[] mappedMethod1 = mappingImpl.mapMethodNull(fullMethodName);
            if (mappedMethod1 != null && mappedMethod1.length > 2) {
                method.name = mappedMethod1[1];
                method.desc = mappedMethod1[2].equals(method.desc) ? mapMethodDescriptor(method.desc) : mappedMethod1[2];
            }else {
                method.desc = mapMethodDescriptor(method.desc);
            }
            if (method.tryCatchBlocks != null) {
                for (var tc : method.tryCatchBlocks) {
                    if (tc.type != null) tc.type = mappingImpl.mapClass(tc.type);
                }
            }
            if (method.localVariables != null) {
                for (LocalVariableNode localVariable : method.localVariables) {
                    localVariable.desc = mapFieldDescriptor(localVariable.desc);
                    String signature = localVariable.signature;
                    if (signature != null) {
                        localVariable.signature = mappingImpl.mapLocalSignature(signature);
                    }
                }
            }
            
            // 映射方法体中的汇编指令
            if (method.instructions != null) {
                for (AbstractInsnNode insnNode : method.instructions) {
                    // 处理方法调用指令
                    if (insnNode instanceof MethodInsnNode methodInsnNode) {
                        //// 映射类名
                        //String mappedOwner = mappingImpl.mapClass(methodInsnNode.owner);
                        //if (!mappedOwner.equals(methodInsnNode.owner)) {
                        //    methodInsnNode.owner = mappedOwner;
                        //}
                        
                        // 映射方法名和描述符
                        String fullMethodRefName = methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc;
                        String[] mappedMethodRef = mappingImpl.mapMethodNull(fullMethodRefName);
                        if (mappedMethodRef != null && mappedMethodRef.length > 2) {
                            methodInsnNode.owner = mappedMethodRef[0];
                            methodInsnNode.name = mappedMethodRef[1];
                            methodInsnNode.desc = mappedMethodRef[2];
                        }else {
                            methodInsnNode.owner = mappingImpl.mapClass(methodInsnNode.owner);
                            methodInsnNode.desc = mapMethodDescriptor(methodInsnNode.desc);
                        }
                    }
                    
                    // 处理帧
                    else if (insnNode instanceof FrameNode frameNode) {
                        List<Object> stack = frameNode.stack;
                        if (stack != null) {
                            for (int i = 0; i < stack.size(); i++) {
                                Object o = stack.get(i);
                                if (o instanceof String str) {
                                    stack.set(i, mapCommonDescriptor(str));
                                }
                            }
                        }
                        List<Object> local = frameNode.local;
                        if (local != null) {
                            for (int i = 0; i < local.size(); i++) {
                                Object o = local.get(i);
                                if (o instanceof String str) {
                                    local.set(i, mapCommonDescriptor(str));
                                }
                            }
                        }
                    }
                    // 处理字段访问指令
                    else if (insnNode instanceof FieldInsnNode fieldInsnNode) {
                        //// 映射类名
                        //String mappedOwner = mappingImpl.mapClass(fieldInsnNode.owner);
                        //if (!mappedOwner.equals(fieldInsnNode.owner)) {
                        //    fieldInsnNode.owner = mappedOwner;
                        //}
                        
                        // 映射字段名
                        String fullFieldRefName = fieldInsnNode.owner + "." + fieldInsnNode.name;
                        String[] mappedFieldRef = mappingImpl.mapField(fullFieldRefName);
                        if (mappedFieldRef != null && mappedFieldRef.length > 1) {
                            if (fieldInsnNode.owner.equals(mappedFieldRef[0]) && fieldInsnNode.name.equals(mappedFieldRef[1])){
                                fieldInsnNode.owner = mappingImpl.mapClass(fieldInsnNode.owner);
                            }else {
                                fieldInsnNode.owner = mappedFieldRef[0];
                                fieldInsnNode.name = mappedFieldRef[1];
                            }
                        }
                        fieldInsnNode.desc = mapFieldDescriptor(fieldInsnNode.desc);
                    }
                    
                    // 处理类型指令
                    else if (insnNode instanceof TypeInsnNode typeInsnNode) {
                        // 映射类名
                        String mappedType = mappingImpl.mapClass(typeInsnNode.desc);
                        if (!mappedType.equals(typeInsnNode.desc)) {
                            typeInsnNode.desc = mappedType;
                        }
                    }
                    
                    // 处理新建多维数组指令
                    else if (insnNode instanceof MultiANewArrayInsnNode multiANewArrayInsnNode) {
                        // 映射数组元素类型
                        String mappedType = mappingImpl.mapClass(multiANewArrayInsnNode.desc);
                        if (!mappedType.equals(multiANewArrayInsnNode.desc)) {
                            multiANewArrayInsnNode.desc = mappedType;
                        }
                    }
                    
                    // 处理LDC指令
                    else if (insnNode instanceof LdcInsnNode ldcInsnNode) {
                        Object cst = ldcInsnNode.cst;
                        
                        // 处理Type对象
                        if (cst instanceof Type typeCst) {
                            ldcInsnNode.cst = processType(typeCst);
                        }
                        
                        // 处理函数句柄（MethodHandle）
                        else if (cst instanceof Handle) {
                            handleMethodHandleConstant(ldcInsnNode, (Handle) cst);
                        }
                        
                        // 处理动态常量（ConstantDynamic）
                        else if (cst instanceof ConstantDynamic) {
                            handleConstantDynamic(ldcInsnNode, (ConstantDynamic) cst);
                        }
                    }
                    
                    // 处理动态调用指令（InvokeDynamicInsnNode）
                    else if (insnNode instanceof InvokeDynamicInsnNode invokeDynamicInsnNode) {
                        // 处理引导方法句柄
                        Handle bootstrapMethodHandle = invokeDynamicInsnNode.bsm;
                        if (bootstrapMethodHandle != null) {
                            // 使用通用的Handle处理方法，直接替换，不使用equals比较
                            invokeDynamicInsnNode.bsm = processHandle(bootstrapMethodHandle);
                        }
                        bootstrapMethodHandle = invokeDynamicInsnNode.bsm;
                        if (bootstrapMethodHandle.getTag() == Opcodes.H_INVOKESTATIC && bootstrapMethodHandle.getOwner().equals("java/lang/invoke/LambdaMetafactory")){
                            if (bootstrapMethodHandle.getName().equals("metafactory") || bootstrapMethodHandle.getName().equals("altMetafactory")){
                                int endIndex = invokeDynamicInsnNode.desc.indexOf(")");
                                String returnType = invokeDynamicInsnNode.desc.substring(endIndex + 1);
                                //处理数组
                                int numDimensions = 0;
                                while (returnType.charAt(numDimensions) == '[') {
                                    numDimensions++;
                                }
                                if (returnType.charAt(numDimensions) == 'L'){
                                    returnType = returnType.substring(numDimensions+1,returnType.length()-1);
                                    if (invokeDynamicInsnNode.bsmArgs[0] instanceof Type typeCst){
                                        if (typeCst.getSort() == Type.METHOD){
                                            String[] map = mappingImpl.mapMethod(returnType+'.'+invokeDynamicInsnNode.name+typeCst.getDescriptor());
                                            invokeDynamicInsnNode.name = map[1];
                                            invokeDynamicInsnNode.bsmArgs[0] = Type.getMethodType(map[2]);
                                            //invokeDynamicInsnNode.desc = invokeDynamicInsnNode.desc.substring(endIndex + 1) + ("[") .repeat(numDimensions) + "L" + map[0]+ ";";
                                        }
                                    }
                                }
                            }
                        }
                        invokeDynamicInsnNode.desc = mapMethodDescriptor(invokeDynamicInsnNode.desc);

                        // 处理引导方法参数
                        if (invokeDynamicInsnNode.bsmArgs != null) {
                            //boolean needUpdate = false;
                            Object[] newBsmArgs = invokeDynamicInsnNode.bsmArgs;
                            
                            for (int i = 0; i < invokeDynamicInsnNode.bsmArgs.length; i++) {
                                Object arg = invokeDynamicInsnNode.bsmArgs[i];
                                Object processedArg = processConstantArg(arg);
                                
                                if (!processedArg.equals(arg)) {
                                    //if (!needUpdate) {
                                    //    newBsmArgs = invokeDynamicInsnNode.bsmArgs.clone();
                                    //    needUpdate = true;
                                    //}
                                    newBsmArgs[i] = processedArg;
                                }
                            }
                            
                            //if (needUpdate) {
                            //    invokeDynamicInsnNode.bsmArgs = newBsmArgs;
                            //}
                        }
                        //
                    }
                }
            }
        }
        
        // 映射内部类名
        for (InnerClassNode innerClass : input.innerClasses) {
            String mappedInnerClassName = mappingImpl.mapClass(innerClass.name);
            if (!mappedInnerClassName.equals(innerClass.name)) {
                innerClass.name = mappedInnerClassName;
            }
            
            if (innerClass.outerName != null) {
                String mappedOuterName = mappingImpl.mapClass(innerClass.outerName);
                if (!mappedOuterName.equals(innerClass.outerName)) {
                    innerClass.outerName = mappedOuterName;
                }
            }
            
            if (innerClass.innerName != null) {
                // 从映射后的内部类完整名称中提取新的简单名称
                String currentInnerClassName = innerClass.name;
                int lastDollarIndex = currentInnerClassName.lastIndexOf('$');
                if (lastDollarIndex != -1) {
                    // 更新内部类的简单名称
                    String newInnerName = currentInnerClassName.substring(lastDollarIndex + 1);
                    if (!newInnerName.equals(innerClass.innerName)) {
                        innerClass.innerName = newInnerName;
                    }
                }
            }
        }
        
        return input;
    }

    
    /**
     * 处理Type常量
     */
    protected Type handleTypeConstant(Type typeCst) {
        boolean needUpdate = false;
        Type newType = typeCst;
        
        // 处理数组类型
        if (typeCst.getSort() == Type.ARRAY) {
            // 获取元素类型
            Type elementType = typeCst.getElementType();
            // 处理元素类型
            Type newElementType = processType(elementType);
            if (!newElementType.equals(elementType)) {
                // 根据原数组维度创建新的数组Type
                int dimensions = typeCst.getDimensions();
                String arrayDesc = "[" .repeat(dimensions) + newElementType.getDescriptor();
                newType = Type.getType(arrayDesc);
                needUpdate = true;
            }
        }
        // 处理METHOD类型
        else if (typeCst.getSort() == Type.METHOD) {
            // 处理方法类型描述符
            String methodDesc = typeCst.getDescriptor();
            // 映射方法描述符中的类名
            String mappedMethodDesc = mapMethodDescriptor(methodDesc);
            if (!mappedMethodDesc.equals(methodDesc)) {
                newType = Type.getType(mappedMethodDesc);
                needUpdate = true;
            }
        }
        // 处理OBJECT类型
        else if (typeCst.getSort() == Type.OBJECT) {
            // getInternalName() 直接返回斜杠分隔的内部名
            String internalName = typeCst.getInternalName();
            String mappedInternalName = mappingImpl.mapClass(internalName);
            if (!mappedInternalName.equals(internalName)) {
                newType = Type.getObjectType(mappedInternalName);
                needUpdate = true;
            }
        }

        return newType;
    }
    
    /**
     * 处理MethodHandle常量
     */
    private void handleMethodHandleConstant(LdcInsnNode ldcInsnNode, Handle handle) {
        // 使用通用的Handle处理方法，直接替换，不使用equals比较
        ldcInsnNode.cst = processHandle(handle);
    }
    
    /**
     * 处理ConstantDynamic常量
     */
    private void handleConstantDynamic(LdcInsnNode ldcInsnNode, ConstantDynamic constantDynamic) {
        boolean needUpdate = false;
        String mappedName = constantDynamic.getName();
        String mappedDescriptor = constantDynamic.getDescriptor();
        Handle mappedBootstrapMethod = constantDynamic.getBootstrapMethod();
        
        // 处理引导方法句柄
        if (mappedBootstrapMethod != null) {
            Handle newBootstrapMethod = processHandle(mappedBootstrapMethod);
            if (newBootstrapMethod != mappedBootstrapMethod) {
                mappedBootstrapMethod = newBootstrapMethod;
                needUpdate = true;
            }
        }
        
        // 处理引导方法参数
        int argCount = constantDynamic.getBootstrapMethodArgumentCount();
        Object[] mappedBootstrapMethodArguments = new Object[argCount];
        boolean argsUpdated = false;
        
        for (int i = 0; i < argCount; i++) {
            Object arg = constantDynamic.getBootstrapMethodArgument(i);
            mappedBootstrapMethodArguments[i] = arg;
            Object processedArg = processConstantArg(arg);
            if (processedArg != arg) {
                mappedBootstrapMethodArguments[i] = processedArg;
                argsUpdated = true;
            }
        }
        
        if (argsUpdated) {
            needUpdate = true;
        }
        
        // 处理描述符（如果是对象类型，需要映射）
        String mappedDesc = mapTypeDescriptor(mappedDescriptor);
        if (!mappedDesc.equals(mappedDescriptor)) {
            mappedDescriptor = mappedDesc;
            needUpdate = true;
        }
        
        if (needUpdate) {
            ldcInsnNode.cst = new ConstantDynamic(
                    mappedName,
                    mappedDescriptor,
                    mappedBootstrapMethod,
                    mappedBootstrapMethodArguments
            );
        }
    }
    
    /**
     * 处理常量参数，用于处理套娃情况，不使用递归
     */
    private Object processConstantArg(Object arg) {
        if (arg instanceof Type) {
            Type type = (Type) arg;
            return processType(type);
        }
        else if (arg instanceof Handle) {
            Handle handle = (Handle) arg;
            return processHandle(handle);
        }
        else if (arg instanceof ConstantDynamic) {
            ConstantDynamic constantDynamic = (ConstantDynamic) arg;
            return processConstantDynamic(constantDynamic);
        }
        return arg;
    }
    
    /**
     * 处理ConstantDynamic对象，返回处理后的新对象
     */
    private ConstantDynamic processConstantDynamic(ConstantDynamic constantDynamic) {
        boolean needUpdate = false;
        String mappedName = constantDynamic.getName();
        String mappedDescriptor = constantDynamic.getDescriptor();
        Handle mappedBootstrapMethod = constantDynamic.getBootstrapMethod();

        // 处理引导方法句柄
        if (mappedBootstrapMethod != null) {
            Handle newBootstrapMethod = processHandle(mappedBootstrapMethod);
            if (newBootstrapMethod != mappedBootstrapMethod) {
                mappedBootstrapMethod = newBootstrapMethod;
                needUpdate = true;
            }
        }

        // 处理引导方法参数
        int argCount = constantDynamic.getBootstrapMethodArgumentCount();
        Object[] mappedBootstrapMethodArguments = new Object[argCount];
        boolean argsUpdated = false;

        for (int i = 0; i < argCount; i++) {
            Object arg = constantDynamic.getBootstrapMethodArgument(i);
            mappedBootstrapMethodArguments[i] = arg;
        }
        if (mappedBootstrapMethod.getTag() == Opcodes.H_INVOKESTATIC && mappedBootstrapMethod.getOwner().equals("java/lang/invoke/LambdaMetafactory")){
            if (mappedBootstrapMethod.getName().equals("metafactory") || mappedBootstrapMethod.getName().equals("altMetafactory")){
                int endIndex = constantDynamic.getDescriptor().indexOf(")");
                String returnType = constantDynamic.getDescriptor().substring(endIndex + 1);
                //处理数组
                int numDimensions = 0;
                while (returnType.charAt(numDimensions) == '[') {
                    numDimensions++;
                }
                if (returnType.charAt(numDimensions) == 'L'){
                    returnType = returnType.substring(numDimensions+1,returnType.length()-1);
                    if (constantDynamic.getBootstrapMethodArgument(0) instanceof Type typeCst){
                        if (typeCst.getSort() == Type.METHOD){
                            needUpdate = true;
                            String[] map = mappingImpl.mapMethod(returnType+'.'+mappedBootstrapMethod.getName()+typeCst.getDescriptor());
                            mappedName = map[1];
                            mappedDescriptor = constantDynamic.getDescriptor().substring(endIndex + 1) + ("[") .repeat(numDimensions) + "L" + map[0]+ ";";
                            mappedBootstrapMethodArguments[0] = Type.getObjectType(map[2]);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < argCount; i++) {
            Object mappedBootstrapMethodArgument = mappedBootstrapMethodArguments[i];
            Object processedArg = processConstantArg(mappedBootstrapMethodArgument);
            if (processedArg != mappedBootstrapMethodArgument) {
                mappedBootstrapMethodArguments[i] = processedArg;
                argsUpdated = true;
            }
        }
        
        if (argsUpdated) {
            needUpdate = true;
        }
        
        // 处理描述符
        String mappedDesc = mapMethodDescriptor(mappedDescriptor);
        if (!mappedDesc.equals(mappedDescriptor)) {
            mappedDescriptor = mappedDesc;
            needUpdate = true;
        }
        
        if (needUpdate) {
            return new ConstantDynamic(
                    mappedName,
                    mappedDescriptor,
                    mappedBootstrapMethod,
                    mappedBootstrapMethodArguments
            );
        }
        return constantDynamic;
    }
    
    /**
     * 处理Type对象，不使用递归
     */
    protected Type processType(Type type) {
        if (type.getSort() == Type.ARRAY) {
            // 获取元素类型并递归处理
            Type elementType = type.getElementType();
            Type newElementType = processType(elementType);

            // 根据原数组维度创建新的数组Type
            int dimensions = type.getDimensions();
            String arrayDesc = "[".repeat(dimensions) + newElementType.getDescriptor();
            return Type.getType(arrayDesc);
        }
        else if (type.getSort() == Type.OBJECT) {
            // getInternalName() 直接返回斜杠分隔的内部名
            String internalName = type.getInternalName();
            String mappedInternalName = mappingImpl.mapClass(internalName);
            if (!mappedInternalName.equals(internalName)) {
                return Type.getObjectType(mappedInternalName);
            }
        }
        else if (type.getSort() == Type.METHOD) {
            String methodDesc = type.getDescriptor();
            String mappedMethodDesc = mapMethodDescriptor(methodDesc);
            if (!mappedMethodDesc.equals(methodDesc)) {
                return Type.getType(mappedMethodDesc);
            }
        }
        return type;
    }
    
    /**
     * 处理Handle对象，支持变量和函数类型
     */
    protected Handle processHandle(Handle handle) {
        boolean needUpdate = false;
        String mappedOwner = handle.getOwner();
        String mappedName = handle.getName();
        String mappedDesc = handle.getDesc();

        // 根据Handle类型处理
        switch (handle.getTag()) {
            // 静态方法
            case Opcodes.H_GETSTATIC:
            case Opcodes.H_PUTSTATIC:
            case Opcodes.H_INVOKESTATIC:
            // 实例方法
            case Opcodes.H_GETFIELD:
            case Opcodes.H_PUTFIELD:
            case Opcodes.H_INVOKEVIRTUAL:
            case Opcodes.H_INVOKESPECIAL:
            case Opcodes.H_INVOKEINTERFACE:
            case Opcodes.H_NEWINVOKESPECIAL:
                // 处理字段访问或方法调用
                if (handle.getTag() == Opcodes.H_GETFIELD || handle.getTag() == Opcodes.H_PUTFIELD || 
                    handle.getTag() == Opcodes.H_GETSTATIC || handle.getTag() == Opcodes.H_PUTSTATIC) {
                    // 字段访问
                    String fullFieldName = handle.getOwner() + "." + handle.getName();
                    String[] mappedField = mappingImpl.mapField(fullFieldName);
                    if (mappedField != null && mappedField.length > 1) {
                        if (!mappedField[1].equals(handle.getName())) {
                            mappedName = mappedField[1];
                            needUpdate = true;
                        }
                    }
                } else {
                    // 方法调用
                    String fullMethodName = handle.getOwner() + "." + handle.getName() + handle.getDesc();
                    String[] mappedMethod = mappingImpl.mapMethod(fullMethodName);

                    if (mappedMethod != null && mappedMethod.length > 2) {
                        if (!mappedMethod[1].equals(handle.getName())) {
                            mappedName = mappedMethod[1];
                            needUpdate = true;
                        }
                        if (!mappedMethod[2].equals(handle.getDesc())) {
                            mappedDesc = mappedMethod[2];
                            needUpdate = true;
                        }else {
                            var mappedDesc2 = mapMethodDescriptor(mappedMethod[2]);
                            if (!mappedDesc2.equals(mappedMethod[2])) {
                                mappedDesc = mappedDesc2;
                                needUpdate = true;
                            }
                        }
                    }else {
                        var mappedDesc2 = mapMethodDescriptor(mappedMethod[2]);
                        if (!mappedDesc2.equals(mappedMethod[2])) {
                            mappedDesc = mappedDesc2;
                            needUpdate = true;
                        }
                    }
                }
                break;
            // 其他类型
            default:
                // 不处理
                break;
        }

        // 映射Handle中的类名
        String tempMappedOwner = mappingImpl.mapClass(handle.getOwner());
        if (!tempMappedOwner.equals(handle.getOwner())) {
            mappedOwner = tempMappedOwner;
            needUpdate = true;
        }

        if (needUpdate) {
            return new Handle(
                    handle.getTag(),
                    mappedOwner,
                    mappedName,
                    mappedDesc,
                    handle.isInterface()
            );
        }
        return handle;
    }
    
    // 暂时移除ConstantDynamic相关方法，等待获取正确的API信息
    
    /**
     * 映射方法描述符，替换其中的类名
     */
    protected String mapMethodDescriptor(String methodDesc) {
        return mappingImpl.mapMethodDesc(methodDesc);
//        // 解析方法描述符，提取参数类型和返回类型
//        // 简单实现，假设描述符格式正确
//        int paramStart = methodDesc.indexOf('(');
//        int paramEnd = methodDesc.indexOf(')');
//
//        if (paramStart == -1 || paramEnd == -1) {
//            return methodDesc;
//        }
//
//        String paramDesc = methodDesc.substring(paramStart + 1, paramEnd);
//        String returnDesc = methodDesc.substring(paramEnd + 1);
//
//        // 映射参数类型
//        StringBuilder mappedParamDesc = new StringBuilder();
//        int i = 0;
//        while (i < paramDesc.length()) {
//            int endIndex = findTypeEnd(paramDesc, i);
//            if (endIndex == -1) {
//                break;
//            }
//            String typeDesc = paramDesc.substring(i, endIndex);
//            mappedParamDesc.append(mapTypeDescriptor(typeDesc));
//            i = endIndex;
//        }
//
//        // 映射返回类型
//        String mappedReturnDesc = mapTypeDescriptor(returnDesc);
//
//        return "(" + mappedParamDesc + ")" + mappedReturnDesc;
    }
    
    /**
     * 查找类型描述符的结束位置
     */
    private int findTypeEnd(String desc, int startIndex) {
        if (startIndex >= desc.length()) {
            return -1;
        }
        
        char c = desc.charAt(startIndex);
        
        // 基本类型
        if ("ZBCSIFJD".indexOf(c) != -1) {
            return startIndex + 1;
        }
        // 数组类型
        else if (c == '[') {
            return findTypeEnd(desc, startIndex + 1);
        }
        // 对象类型
        else if (c == 'L') {
            int endIndex = desc.indexOf(';', startIndex);
            return endIndex != -1 ? endIndex + 1 : -1;
        }
        return -1;
    }
    
    /**
     * 映射类型描述符
     */
    private String mapTypeDescriptor(String typeDesc) {
        if (typeDesc.startsWith("L")) {
            // 对象类型
            String className = typeDesc.substring(1, typeDesc.length() - 1);
            String mappedClassName = mappingImpl.mapClass(className);
            if (!mappedClassName.equals(className)) {
                return "L" + mappedClassName + ";";
            }
        }
        return typeDesc;
    }
    //帧的
    protected String mapCommonDescriptor(String desc) {
        //快速粗略处理
        switch (desc){
            case "B":
            case "J":
            case "C":
            case "Z":
            case "F":
            case "I":
            case "S":
            case "D":
            case "V":
            case "[B":
            case "[J":
            case "[C":
            case "[Z":
            case "[F":
            case "[I":
            case "[S":
            case "[D":
            case "[V":
                return desc;
        }
        if (desc.startsWith("java")) {
            return desc;
        }
        if (desc.startsWith("[")) {
            return mapFieldDescriptor(desc);
        }
        if (desc.contains("/")){
            if (desc.endsWith(";"))
                return mapFieldDescriptor(desc);
            return mappingImpl.mapClass(desc);
        }else if (desc.length() > 1){
            return mappingImpl.mapClass(desc);
        }else if (!isDefaultClass(desc)){
            return mappingImpl.mapClass(desc);
        }else return desc;
    }
    protected String mapFieldDescriptor(String fieldDesc) {
        // 映射类型
        int array = 0;
        int max = fieldDesc.length();
        int pos = 0;
        while (pos < max) {
            if (fieldDesc.charAt(pos) == '[') {
                array++;
                pos++;
            } else {
                break;
            }
        }
        if (pos < max){
            if (fieldDesc.charAt(pos) == 'L') {
                var sp = pos += 1;
                while (pos < max) {
                    if (fieldDesc.charAt(pos) == ';') {
                        break;
                    }
                    pos++;
                }
                return "[".repeat(array) + "L" + mappingImpl.mapClass(fieldDesc.substring(sp, pos)) + ";";
            }
        }
        return fieldDesc;
    }
}