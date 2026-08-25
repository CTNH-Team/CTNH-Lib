package tech.vixhentx.mcmod.ctnhlib.utils.mapping;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Slf4j
public class Neo21MapppingMap implements BiFunction<String, String, String>, Consumer<MappingImpl> {
    public static final MappingImpl map = new MappingImpl(){
        {
            map.put("net/minecraft/world/level/chunk/ChunkStatus", "net/minecraft/world/level/chunk/status/ChunkStatus");
            map.put("net/minecraftforge/fluids/FluidStack", "net/neoforged/neoforge/fluids/FluidStack");
        }
    };
    @Override
    public String apply(String old, String data) {
        String[] split = data.split("\\.");
        split[0] = map.mapClass(split[0]);
        if (split.length == 2){
            String[] split1 = split[1].split("\\(");
            if (split1.length == 1)return split[0] + "." + split[1];
            return split[0] + "." + split1[0] + map.mapMethodDesc(split1[1]);
        }else if (split.length == 1){
            return split[0];
        }else  {
            throw new RuntimeException();
        }

    }

    @Override
    public void accept(MappingImpl mapping) {
        mapping.map.put("net/minecraft/world/level/chunk/ChunkStatus", "net/minecraft/world/level/chunk/status/ChunkStatus");
        mapping.map.put("net/minecraftforge/fluids/FluidStack", "net/neoforged/neoforge/fluids/FluidStack");
    }
}
