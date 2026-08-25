package tech.vixhentx.mcmod.ctnhlib.utils.mapping;



import java.util.HashMap;
import java.util.Map;

public class MappingTsrgImpl extends MappingImpl {
    public MappingTsrgImpl(String m) {
        Map<String,String> map2 = new HashMap<>();
        String n1 = null;//类名
        String m1 = null;//类名

        for (String s : m.split("(\n|\r\n)+")) {
            int i = countStr(s, "\t");
            String[] split = s.replace("\t","").split(" ");

            switch (i) {
                case 0-> {//类型，名字
                    if (split.length == 3)continue;
                    map2.put(n1 = split[0], (m1=split[1]));
                }
                case 1->{
                    switch (split.length){
                        case 2->{
                            map2.put(n1 + "." + split[0], m1 + "." + split[1]);
                        }
                        case 3->{
                            map2.put(n1 + "." + split[0] + split[1], m1 + "." + split[2]);
                            map2.put(n1 + "." + split[0] + "}{", m1 + "." + split[2]);
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, String> stringStringEntry : map2.entrySet()) {
            if (stringStringEntry.getKey().contains("(")) {

                String[] split1 = stringStringEntry.getKey().split("\\.");
                String[] split = split1[1].split("\\(");

                map.put(stringStringEntry.getKey(), stringStringEntry.getValue() + mapMethodDesc(split[1]));
            }else {
                map.put(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }

    }
}
