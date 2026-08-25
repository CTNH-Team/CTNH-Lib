package tech.vixhentx.mcmod.ctnhlib.utils;


/**
 * 他会动态变化，根据不同的环境而变化，他会在运行一段时间后加载对应代码如果一开始就加载会找不到对应的类
 */
public class AsmApi2 {
    public static final BootType bootType;
    static {
        BootType bootType_ = BootType.FORGE;
        try{//if(System.getProperty("legacyClassPath") == null) {
            AsmApi2.class.getClassLoader().loadClass("cpw.mods.modlauncher.api.TargetType");//确定是neoforge
            bootType_ = BootType.NEO_FORGE;
        }catch (Exception e){
        }
        bootType = bootType_;
    }

    public static enum BootType{
        FORGE, NEO_FORGE
    }
}
