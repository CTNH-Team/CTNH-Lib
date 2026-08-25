package tech.vixhentx.mcmod.ctnhlib.utils.mapping;


import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MapMappingSrgImplForge extends MappingImpl {

    public MapMappingSrgImplForge(String m, BiFunction<String, String, String> function) {

        for (String s : m.split("(\n|\r\n)+")) {
            String[] split = s.split(": ");
            String[] split1 = split[1].split(" ");

            switch (split[0]) {
                case "CL"-> {
                    map.put(split1[0], function.apply(split1[0], split1[1]));
                }
                case "FD"->{

                    String[] split2 = split1[0].split("/(?!(.+)/)");
                    String[] split3 = split1[1].split("/(?!(.+)/)");

                    String key = split2[0] + "." + split2[1];
                    map.put(key, function.apply(key, split3[0] + "." + split3[1]));
                    map.put(split2[1], function.apply(split2[1], split3[1]));
                }
                case "MD"->{

                    String[] split2 = split1[0].split("/(?!(.+)/)");
                    String[] split3 = split1[2].split("/(?!(.+)/)");

                    String key = split2[0] + "." + split2[1] + split1[1];
                    map.put(key, function.apply(key, split3[0] + "." + split3[1] + split1[3]));
                    String key1 = split2[0] + "." + split2[1] + "}{";
                    map.put(key1, function.apply(key1, split3[0] + "." + split3[1]));
                    map.put(split2[1], function.apply(split2[1], split3[1]));
                }
                default -> {
                    throw new RuntimeException();
                }
            }
        }
        if (function instanceof Consumer c)c.accept(this);
    }
}
