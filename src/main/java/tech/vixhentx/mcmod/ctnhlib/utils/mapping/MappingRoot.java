package tech.vixhentx.mcmod.ctnhlib.utils.mapping;

import lombok.extern.slf4j.Slf4j;
import tech.vixhentx.mcmod.ctnhlib.utils.AsmApi2;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class MappingRoot {
    public static final MappingImpl minecraft_map;
    public static final MappingImpl srg$Forge$_map;
    static {
        InputStream resourceAsStream = MappingTsrgImpl.class.getResourceAsStream("/ctnh.mapping/map.tsrg");
        InputStream resourceAsStream2 = MappingSrgImpl.class.getResourceAsStream("/ctnh.mapping/map_srg.srg");
        boolean isNeoForge = AsmApi2.bootType == AsmApi2.BootType.NEO_FORGE;
        try {

            if (resourceAsStream == null)throw new IOException("找不到映射表[/ctnh.mapping/map.tsrg]，可以尝试检查是否正确编译");
            if (resourceAsStream2 == null)throw new IOException("找不到映射表[/ctnh.mapping/map_srg.srg]，可以尝试检查是否正确编译");
            minecraft_map = isNeoForge ? new MappingMCP(new MapMappingSrgImplForge(new String(resourceAsStream2.readAllBytes()), new Neo21MapppingMap())) : new MappingTsrgImplForge(new String(resourceAsStream.readAllBytes()));
            srg$Forge$_map = isNeoForge ? new MappingImpl(){} : new MappingSrgImplForge(new String(resourceAsStream2.readAllBytes()));
            //minecraft_map.map.forEach((k,v)->{
            //    log.info("{} -> {}",k,v);
            //});

            log.info("加载映射表成功 {}", AsmApi2.bootType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
