package tech.vixhentx.mcmod.ctnhlib.langprovider;

import java.util.ArrayList;
import java.util.List;

public final class LocatedLang extends Lang{

    Located[] locates;

    public LocatedLang(Located... locates) {
        this.locates = locates;
    }

    public static Builder builder(){
        return Builder.start();
    }
    public static Builder en(String value) {
        return Builder.start().en(value);
    }
    public static Builder cn(String value) {
        return Builder.start().cn(value);
    }

    public static class Builder {
        private List<Located> locates = new ArrayList<>();

        public static Builder start() {
            return new Builder();
        }

        public Builder add(String location, String value) {
            locates.add(new Located(location, value));
            return this;
        }

        public Builder en(String value) {
            locates.add(Located.en(value));
            return this;
        }

        public Builder cn(String value) {
            locates.add(Located.cn(value));
            return this;
        }
        //add more if needed

        public LocatedLang build() {
            return new LocatedLang(locates.toArray(new Located[0]));
        }
    }


    Lang erase() {
        return new Lang(key);
    }
}
