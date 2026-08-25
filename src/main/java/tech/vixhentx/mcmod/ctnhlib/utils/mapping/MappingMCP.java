package tech.vixhentx.mcmod.ctnhlib.utils.mapping;

import lombok.AllArgsConstructor;

import java.util.regex.Pattern;

@AllArgsConstructor
public class MappingMCP extends MappingImpl {
    public static final Pattern FIELD_PATTERN = Pattern.compile("^f_\\d+_");
    public static final Pattern METHOD_PATTERN = Pattern.compile("^m_\\d+_");
    public final MappingImpl mappingImpl;
    @Override
    public String[] mapMethod(String name) {
        String orDefault = mappingImpl.map_(name);
        String[] split = orDefault.split("\\.");
        String[] split1 = split[1].split("\\(");
        if (METHOD_PATTERN.matcher(split1[0]).matches()) {
            split1[0] = mappingImpl.map_(split1[0]);
        }
        return new String[]{split[0], split1[0], "(" + split1[1]};
    }

    @Override
    public String[] mapMethodNull(String name){
        String orDefault = mapNull_(name);
        if (orDefault == null) {
            String[] split = name.split("\\.");
            String[] split1 = split[1].split("\\(");

            if (METHOD_PATTERN.matcher(split1[0]).matches()) {
                split1[0] = mappingImpl.map_(split1[0]);
                return new String[]{split[0],split1[0],"("+split1[1]};
            }
            return null;
        }
        String[] split = orDefault.split("\\.");
        String[] split1 = split[1].split("\\(");
        return new String[]{split[0],split1[0],"("+split1[1]};
    }

    @Override
    public String[] mapField(String name) {
        String orDefault = mappingImpl.map_(name);
        String[] split = orDefault.split("\\.");
        if (FIELD_PATTERN.matcher(split[1]).matches()) {
            split[1] = mappingImpl.map_(split[1]);
        }
        return new String[]{split[0], split[1]};
    }

    @Override
    public String mapNull_(String name) {
        return mappingImpl.mapNull_(name);
    }

    @Override
    public String map_(String name) {
        return mappingImpl.map_(name);
    }

    @Override
    public String mapClass(String name) {
        return mappingImpl.mapClass(name);
    }
}
