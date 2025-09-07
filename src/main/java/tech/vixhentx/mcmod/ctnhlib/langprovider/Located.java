package tech.vixhentx.mcmod.ctnhlib.langprovider;

public record Located(String location,String value){
    public static Located en(String value) {
        return new Located("en_us", value);
    }
    public static Located cn(String value) {
        return new Located("zh_cn", value);
    }
    //add more if used
}