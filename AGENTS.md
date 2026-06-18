# CTNH-Lib KNOWLEDGE BASE

## OVERVIEW
CTNH-Lib is the shared support module for CTNH code. It contains common proxies, registrate builder wrappers, dynamic datapack helpers, language annotations, shared Ponder support, right-side configurator UI, Jade priority infrastructure, and client highlight networking.

## WHERE TO LOOK
- Mod entry: `src/main/java/tech/vixhentx/mcmod/ctnhlib/CTNHLib.java`. Library mod initialization.
- Registrate helpers: `src/main/java/tech/vixhentx/mcmod/ctnhlib/registrate/`. `CNRegistrate`, networking, builder wrappers.
- Builder APIs: `src/main/java/tech/vixhentx/mcmod/ctnhlib/registrate/builders/`. Shared block/item/machine/material/recipe builders.
- Dynamic data: `src/main/java/tech/vixhentx/mcmod/ctnhlib/data/`. `CTNHDynamicDataPack` and `DataFilterPack` provide runtime datapack/filter support.
- Shared GUI: `src/main/java/tech/vixhentx/mcmod/ctnhlib/client/gui/`. `RightConfiguratorPanel`, `RCUIWidget`, and provider interfaces used by Core/Energy machine UIs.
- Highlight rendering: `src/main/java/tech/vixhentx/mcmod/ctnhlib/client/render/highlight/`, `network/packets/BlockHighlightPacket.java`. Client block highlight renderer and packet path.
- Jade priority: `src/main/java/tech/vixhentx/mcmod/ctnhlib/jade/`. Ordered GT provider registration through `JadePriorityManager` and `GTProvidersRegistrar`.
- Ponder framework: `src/main/java/tech/vixhentx/mcmod/ctnhlib/client/ponder/CTNHPonderSceneBuilder.java` and `CTNHPonderLang.java`. Provides shared baseplate/camera helpers, `title/showText` bilingual text registration via module-supplied `LangRegistrar`, and shared Ponder scene lang extraction for datagen.
- Lang annotations: `src/main/java/com/ctnhlang/`. `@CN`, `@EN`, category/domain annotations.
- Mixins: `src/main/java/tech/vixhentx/mcmod/ctnhlib/mixin/`. GT Jade provider ordering, GT recipe/machine builder adjustments, TMRV compatibility, and Forge datagen shutdown behavior.

## REGISTRATION ENTRYPOINTS
- Registrate API: `registrate/CNRegistrate.java` extends GTCEu `GTRegistrate` and provides CTNH item/block/entity/recipe helpers.
- Builders: `registrate/builders/CTNHItemBuilder.java`, `CTNHBlockBuilder.java`, `CTNHEntityBuilder.java`, `CTNHMachineBuilder.java`, `CTNHMultiblockMachineBuilder.java`, `CTNHRecipeType.java`, `CTNHMaterial.java`, `CTNHTagPrefix.java`.
- Runtime helper item: `common/CommonProxy.java` registers `MultiblockHelper` through GTCEu's registrate.
- Common proxy: `common/CommonProxy.java` initializes `GTProvidersRegistrar`, `CTNHLibNetworking`, registers the runtime helper item, and adds the `ctnhlib:filter_data` server data pack source.
- Networking/datapack: `registrate/CTNHLibNetworking.java`, `data/CTNHDynamicDataPack.java`, `data/DataFilterPack.java`.
- Jade infrastructure: `jade/GTProvidersRegistrar.java` loads ordered GT providers; `jade/JadePriorityManager.java` lets feature modules register/unregister block data and component providers with explicit priority.
- Right configurator UI: `client/gui/RightConfiguratorPanel.java`, `IRCFancyUIProvider.java`, `RCUIWidget.java`; Core/Energy part machines attach right-side widgets through this shared surface.
- Highlight system: `client/ClientProxy.java` hooks render-level events into `client/render/highlight/HighlightRender.java`; packets use `network/packets/BlockHighlightPacket.java`.
- Lang/datagen support: `registrate/lang/RegistrateCNLangProvider.java` and `com.ctnhlang.*` annotations.
- Ponder support: `client/ponder/CTNHPonderSceneBuilder.java` is reusable scene infrastructure and `CTNHPonderLang.java` is reusable datagen lang extraction; modules pass their mod id and registrate lang callback from adapter builders and pass their own `PonderPlugin` to `CTNHPonderLang.init(...)`.
- Do not add normal gameplay items/blocks here; add shared registration helpers only.

## CONVENTIONS
- Main library namespace is `tech.vixhentx.mcmod.ctnhlib`; lang annotation namespace is `com.ctnhlang`.
- Resource count is intentionally tiny compared with gameplay modules.
- Changes here can affect all CTNH modules through shared builders, annotations, Jade provider ordering, right configurator UI, highlight packets, and shared Ponder support.

## COMMANDS
```bash
./gradlew :modules:CTNH-Lib:build
./gradlew :modules:CTNH-Lib:runData
./gradlew :modules:CTNH-Lib:spotlessCheck
```

## ANTI-PATTERNS
- Do not add gameplay-specific logic to CTNH-Lib unless it is genuinely shared.
- Do not move module-specific Ponder scene/tag/plugin code or Energy's AE2 cable helper into CTNH-Lib.
- Do not rename lang annotations without checking the custom `com.ctnhlang.langprovider` plugin usage.
- Do not bypass `JadePriorityManager` for GT provider ordering; CTNH modules rely on predictable Jade block data/component priority.
