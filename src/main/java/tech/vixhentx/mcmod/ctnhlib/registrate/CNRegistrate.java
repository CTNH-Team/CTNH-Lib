package tech.vixhentx.mcmod.ctnhlib.registrate;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTBlockBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;
import tech.vixhentx.mcmod.ctnhlib.registrate.builders.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;
import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;
import static tech.vixhentx.mcmod.ctnhlib.utils.EnvUtils.isDataGen;

public class CNRegistrate extends GTRegistrate {
    /**
     * Construct a new Registrate for the given mod ID.
     *
     * @param modid The mod ID for which objects will be registered
     */
    protected CNRegistrate(String modid) {
        super(modid);
        this.langProcessor = new LangProcessor(this);
    }

    public <T extends Item> CTNHItemBuilder<T, GTRegistrate> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return item(self(), name, factory);
    }


    public <T extends Item, P> CTNHItemBuilder<T, P> item(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
        return (CTNHItemBuilder<T, P>) entry(name, callback -> CTNHItemBuilder.create(this, parent, name, callback, factory));
    }

    @Override
    public <T extends Block> CTNHBlockBuilder<T, GTRegistrate> block(String name,
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    @Override
    public <T extends Block, P> CTNHBlockBuilder<T, P> block(P parent, String name,
                                                           NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (CTNHBlockBuilder<T, P>) entry(name,
                callback -> CTNHBlockBuilder.create(this, parent, name, callback, factory));
    }

    public <T extends Entity, P> CTNHEntityBuilder<T, P> entity(P parent, String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return (CTNHEntityBuilder<T, P>)this.entry(name,
                (callback) -> CTNHEntityBuilder.create(this, parent, name, callback, factory, classification));
    }

//    @Override
//    public <T extends Entity> CTNHEntityBuilder<T, GTRegistrate> entity(EntityType.EntityFactory<T> factory, MobCategory classification) {
//        return super.entity(factory, classification);
//    }

    @Override
    public <T extends Entity> CTNHEntityBuilder<T, GTRegistrate> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return this.entity(this.self(), name, factory, classification);
    }

    public <DEFINITION extends MachineDefinition> CTNHMachineBuilder<DEFINITION> machine(String name,
                                                                                         Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                         Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                         BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                         BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                         TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new CTNHMachineBuilder<>(this, name, definitionFactory, metaMachine,
                blockFactory, itemFactory, blockEntityFactory);
    }

    @Override
    public CTNHMachineBuilder<MachineDefinition> machine(String name, Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return new CTNHMachineBuilder<>(this, name, MachineDefinition::new, metaMachine,
                MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::new);
    }

    public <DEFINITION extends MachineDefinition> CTNHMachineBuilder<DEFINITION> machine(String name,
                                                                                         String cnname,
                                                                                         Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                         Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                         BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                         BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                         TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new CTNHMachineBuilder<>(this, name, cnname, definitionFactory, metaMachine,
                blockFactory, itemFactory, blockEntityFactory);
    }

    public CTNHMultiblockMachineBuilder multiblock(String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return multiblock(name, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::new);
    }

    public CTNHMultiblockMachineBuilder multiblock(String name,
                                                   Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                                   BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                                   BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                   TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory){
        return new CTNHMultiblockMachineBuilder(this, name, metaMachine, blockFactory, itemFactory, blockEntityFactory);
    }

    public CTNHMaterial.Builder material(ResourceLocation resourceLocation){
        return new CTNHMaterial.Builder(this, resourceLocation);
    }

    public CTNHMaterial.Builder material(String path){
        return material(ResourceLocation.tryBuild(getModid(), path));
    }

    public CTNHRecipeType recipeType(String name, String group, RecipeType<?>... proxyRecipes){
        return recipeType(GTCEu.id(name), group, proxyRecipes);
    }

    public CTNHRecipeType recipeType(ResourceLocation resourceLocation, String group, RecipeType<?>... proxyRecipes){
        var recipeType = new CTNHRecipeType(this, resourceLocation, group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }

    public CTNHRecipeType recipeType(ResourceLocation resourceLocation, String group, Function<GTRecipeType, GTRecipeTypeUI> uiFactory, RecipeType<?>... proxyRecipes){
        var type = recipeType(resourceLocation, group, proxyRecipes);
        return (CTNHRecipeType) type.setRecipeUI(uiFactory.apply(type));
    }

    public CTNHTagPrefix oreTagPrefix(String name, TagKey<Block> miningToolTag) {
        return (CTNHTagPrefix) new CTNHTagPrefix(this, name)
                .defaultTagPath("ores/%s")
                .prefixOnlyTagPath("ores_in_ground/%s")
                .unformattedTagPath("ores")
                .materialIconType(MaterialIconType.ore)
                .miningToolTag(miningToolTag)
                .unificationEnabled(true)
                .blockConstructor(OreBlock::new)
                .generationCondition(hasOreProperty);
    }
    public CTNHTagPrefix tagPrefix(String name) {
        return new CTNHTagPrefix(this, name);
    }


    private final LangProcessor langProcessor;
    private final ObjectSet<Class<?>> langProcessed = new ObjectOpenHashSet<>();

    //Chinese
    private final NonNullSupplier<List<Pair<String, String>>> extraCNLang = NonNullSupplier.lazy(() -> {
        final List<Pair<String, String>> ret = new ArrayList<>();
        addDataGenerator(CNLANG, prov -> ret.forEach(p -> prov.add(p.getKey(), p.getValue())));
        return ret;
    });

    public MutableComponent addCNLang(String type, ResourceLocation id, String localizedName) {
        return addRawCNLang(Util.makeDescriptionId(type, id), localizedName);
    }

    public MutableComponent addCNLang(String type, ResourceLocation id, String suffix, String localizedName) {
        return addRawCNLang(Util.makeDescriptionId(type, id) + "." + suffix, localizedName);
    }

    public MutableComponent addRawCNLang(String key, String value) {
        if(isDataGen) {
            extraCNLang.get().add(Pair.of(key, value));
        }
        return Component.translatable(key);
    }

    public MutableComponent addRawCNLang(String key, String value, Object... args) {
        if(isDataGen) {
            extraCNLang.get().add(Pair.of(key, value));
        }
        return Component.translatable(key, args);
    }

    //English and Chinese
    public MutableComponent addLang(String type, ResourceLocation id, String en, String cn) {
        addRawLang(Util.makeDescriptionId(type, id), en);
        return addRawCNLang(Util.makeDescriptionId(type, id), cn);
    }

    public HashSet<String> keys = new HashSet<>();

    public MutableComponent genLang(String key, String en, String cn) {
        if(keys.contains(key))
            return Component.translatable(key);
        else {
            keys.add(key);
            addRawLang(key, en);
            return addRawCNLang(key, cn);
        }
    }

    public MutableComponent genLang(String key, String en, String cn, Object... args) {
        if(keys.contains(key))
            return Component.translatable(key, args);
        else {
            keys.add(key);
            addRawLang(key, en);
            return addRawCNLang(key, cn, args);
        }

    }
//    public MutableComponent addLang(String type, ResourceLocation id, String suffix, String en, String cn) {
//        addRawLang(Util.makeDescriptionId(type, id) + "." + suffix, en);
//        return addRawCNLang(Util.makeDescriptionId(type, id) + "." + suffix, cn);
//    }

    public void addRawLang(String key, String en, String cn) {
        if(!en.isEmpty()) addRawLang(key, en);
        if(!cn.isEmpty()) addRawCNLang(key, cn);
    }

    public CNRegistrate addLangProcessor() {
        this.addDataGenerator(ProviderType.LANG, prov -> {
            LangProcessor processor = new LangProcessor(this);
            processor.processAll();
        });
        return this;
    }

    //Lang Processor
    /// @param clazz the class to process lang
//    public CNRegistrate addLang(Class<?> clazz){
//        if(langProcessed.add(clazz))
//            langProcessor.process(clazz);
//        return this;
//    }
}
