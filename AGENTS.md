# CTNH-Lib KNOWLEDGE BASE

## OVERVIEW
CTNH-Lib is the shared support module for CTNH code. It contains common proxies, registrate builder wrappers, dynamic datapack helpers, language annotations, and the in-repo lang provider support code.

## WHERE TO LOOK
- Mod entry: `src/main/java/tech/vixhentx/mcmod/ctnhlib/CTNHLib.java`. Library mod initialization.
- Registrate helpers: `src/main/java/tech/vixhentx/mcmod/ctnhlib/registrate/`. `CNRegistrate`, networking, builder wrappers.
- Builder APIs: `src/main/java/tech/vixhentx/mcmod/ctnhlib/registrate/builders/`. Shared block/item/machine/material/recipe builders.
- Dynamic data: `src/main/java/tech/vixhentx/mcmod/ctnhlib/data/CTNHDynamicDataPack.java`. Runtime datapack support.
- Lang annotations: `src/main/java/com/ctnhlang/`. `@CN`, `@EN`, category/domain annotations.
- Datagen bugfix mixin: `src/main/java/tech/vixhentx/mcmod/ctnhlib/mixin/MiscForgeHelperMixin.java`. Fixes Forge datagen shutdown behavior.

## REGISTRATION ENTRYPOINTS
- Registrate API: `registrate/CNRegistrate.java` extends GTCEu `GTRegistrate` and provides CTNH item/block/entity/recipe helpers.
- Builders: `registrate/builders/CTNHItemBuilder.java`, `CTNHBlockBuilder.java`, `CTNHEntityBuilder.java`, `CTNHMachineBuilder.java`, `CTNHMultiblockMachineBuilder.java`, `CTNHRecipeType.java`, `CTNHMaterial.java`, `CTNHTagPrefix.java`.
- Runtime helper item: `common/CommonProxy.java` registers `MultiblockHelper` through GTCEu's registrate.
- Networking/datapack: `registrate/CTNHLibNetworking.java`, `data/CTNHDynamicDataPack.java`.
- Lang/datagen support: `registrate/lang/RegistrateCNLangProvider.java` and `com.ctnhlang.*` annotations.
- Do not add normal gameplay items/blocks here; add shared registration helpers only.

## CONVENTIONS
- Main library namespace is `tech.vixhentx.mcmod.ctnhlib`; lang annotation namespace is `com.ctnhlang`.
- Resource count is intentionally tiny compared with gameplay modules.
- Changes here can affect all CTNH modules through shared builders and annotations.

## COMMANDS
```bash
./gradlew :modules:CTNH-Lib:build
./gradlew :modules:CTNH-Lib:runData
./gradlew :modules:CTNH-Lib:spotlessCheck
```

## ANTI-PATTERNS
- Do not add gameplay-specific logic to CTNH-Lib unless it is genuinely shared.
- Do not rename lang annotations without checking the custom `com.ctnhlang.langprovider` plugin usage.
