package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.Generated;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class CTNHMultiblockMachineBuilder extends MultiblockMachineBuilder implements ICNBuilder{

    @Setter
    @Nullable
    private String cnLangValue = null;

    private final CNRegistrate cnRegistrate;

    public CTNHMultiblockMachineBuilder(CNRegistrate registrate,
                                        String name,
                                        Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                        BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                        BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                        TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, metaMachine, blockFactory, itemFactory, blockEntityFactory);
        cnRegistrate = registrate;
    }

    @Override
    public String getCNLangValue() {
        return cnLangValue;
    }

    private String under_component = this.registrate.getModid() + ".copyright.info";

    public CTNHMultiblockMachineBuilder shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        super.shapeInfo(shape);
        return this;
    }

    public CTNHMultiblockMachineBuilder shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        super.shapeInfos(shapes);
        return this;
    }

    public CTNHMultiblockMachineBuilder recoveryItems(Supplier<ItemLike[]> items) {
        super.recoveryItems(items);
        return this;
    }

    public CTNHMultiblockMachineBuilder recoveryStacks(Supplier<ItemStack[]> stacks) {
        super.recoveryStacks(stacks);
        return this;
    }

    public CTNHMultiblockMachineBuilder machine(Function<IMachineBlockEntity, MetaMachine> machine) {
        return (CTNHMultiblockMachineBuilder)super.machine(machine);
    }

    public CTNHMultiblockMachineBuilder shape(VoxelShape shape) {
        return (CTNHMultiblockMachineBuilder)super.shape(shape);
    }

    public CTNHMultiblockMachineBuilder multiblockPreviewRenderer(boolean multiBlockWorldPreview, boolean multiBlockXEIPreview) {
        return (CTNHMultiblockMachineBuilder)super.multiblockPreviewRenderer(multiBlockWorldPreview, multiBlockXEIPreview);
    }

    public CTNHMultiblockMachineBuilder rotationState(RotationState rotationState) {
        return (CTNHMultiblockMachineBuilder)super.rotationState(rotationState);
    }


    public CTNHMultiblockMachineBuilder blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        return (CTNHMultiblockMachineBuilder)super.blockProp(blockProp);
    }

    public CTNHMultiblockMachineBuilder itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        return (CTNHMultiblockMachineBuilder)super.itemProp(itemProp);
    }

    public CTNHMultiblockMachineBuilder blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        return (CTNHMultiblockMachineBuilder)super.blockBuilder(blockBuilder);
    }

    public CTNHMultiblockMachineBuilder itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        return (CTNHMultiblockMachineBuilder)super.itemBuilder(itemBuilder);
    }

    public CTNHMultiblockMachineBuilder recipeTypes(GTRecipeType... recipeTypes) {
        if (recipeTypes.length == 0) {
            return (CTNHMultiblockMachineBuilder) super.recipeTypes(recipeTypes);
        }
        MutableComponent typeNameComponent = Component.empty();

        for (int i = 0; i < recipeTypes.length; i++) {
            if (recipeTypes[i] == null) {
                continue;
            }
            Component typeComponent = Component.translatable(recipeTypes[i].registryName.toLanguageKey());

            if (i > 0) {
                typeNameComponent.append(Component.literal(", ")).append(typeComponent);
            } else {
                typeNameComponent.append(typeComponent);
            }
        }

        // 使用正确的翻译键和参数
        this.tooltips(Component.translatable("ctnh.recipe_type.info", typeNameComponent));
        return (CTNHMultiblockMachineBuilder) super.recipeTypes(recipeTypes);
    }

    public CTNHMultiblockMachineBuilder recipeType(GTRecipeType recipeTypes) {
        var translationKey = recipeTypes.registryName.toLanguageKey();
        this.tooltips(cnRegistrate.genLang(
                "ctnh.recipe_type.info",
                "Recipe Type: %s",
                "配方类型：%s",
                Component.translatable(translationKey)
        ));
        return (CTNHMultiblockMachineBuilder)super.recipeType(recipeTypes);
    }

    public CTNHMultiblockMachineBuilder tier(int tier) {
        return (CTNHMultiblockMachineBuilder)super.tier(tier);
    }

    public CTNHMultiblockMachineBuilder recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> map) {
        return (CTNHMultiblockMachineBuilder)super.recipeOutputLimits(map);
    }

    public CTNHMultiblockMachineBuilder addOutputLimit(RecipeCapability<?> capability, int limit) {
        return (CTNHMultiblockMachineBuilder)super.addOutputLimit(capability, limit);
    }

    public CTNHMultiblockMachineBuilder itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        return (CTNHMultiblockMachineBuilder)super.itemColor(itemColor);
    }

    public CTNHMultiblockMachineBuilder simpleModel(ResourceLocation model) {
        return (CTNHMultiblockMachineBuilder)super.simpleModel(model);
    }

    public CTNHMultiblockMachineBuilder defaultModel() {
        return (CTNHMultiblockMachineBuilder)super.defaultModel();
    }

    public CTNHMultiblockMachineBuilder tieredHullModel(ResourceLocation model) {
        return (CTNHMultiblockMachineBuilder)super.tieredHullModel(model);
    }

    public CTNHMultiblockMachineBuilder overlayTieredHullModel(String name) {
        return (CTNHMultiblockMachineBuilder)super.overlayTieredHullModel(name);
    }

    public CTNHMultiblockMachineBuilder overlayTieredHullModel(ResourceLocation overlayModel) {
        return (CTNHMultiblockMachineBuilder)super.overlayTieredHullModel(overlayModel);
    }

    public CTNHMultiblockMachineBuilder colorOverlayTieredHullModel(String overlay) {
        return (CTNHMultiblockMachineBuilder)super.colorOverlayTieredHullModel(overlay);
    }

    public CTNHMultiblockMachineBuilder colorOverlayTieredHullModel(String overlay, @Nullable String pipeOverlay, @Nullable String emissiveOverlay) {
        return (CTNHMultiblockMachineBuilder)super.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
    }

    public CTNHMultiblockMachineBuilder colorOverlayTieredHullModel(ResourceLocation overlay) {
        return (CTNHMultiblockMachineBuilder)super.colorOverlayTieredHullModel(overlay);
    }

    public CTNHMultiblockMachineBuilder colorOverlayTieredHullModel(ResourceLocation overlay, @Nullable ResourceLocation pipeOverlay, @Nullable ResourceLocation emissiveOverlay) {
        return (CTNHMultiblockMachineBuilder)super.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay);
    }

    public CTNHMultiblockMachineBuilder workableTieredHullModel(ResourceLocation workableModel) {
        return (CTNHMultiblockMachineBuilder)super.workableTieredHullModel(workableModel);
    }

    public CTNHMultiblockMachineBuilder simpleGeneratorModel(ResourceLocation workableModel) {
        return (CTNHMultiblockMachineBuilder)super.simpleGeneratorModel(workableModel);
    }

    public CTNHMultiblockMachineBuilder workableCasingModel(ResourceLocation baseCasing, ResourceLocation overlayModel) {
        return (CTNHMultiblockMachineBuilder)super.workableCasingModel(baseCasing, overlayModel);
    }

    public CTNHMultiblockMachineBuilder sidedOverlayCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        return (CTNHMultiblockMachineBuilder)super.sidedOverlayCasingModel(baseCasing, workableModel);
    }

    public CTNHMultiblockMachineBuilder sidedWorkableCasingModel(ResourceLocation baseCasing, ResourceLocation workableModel) {
        return (CTNHMultiblockMachineBuilder)super.sidedWorkableCasingModel(baseCasing, workableModel);
    }

    public CTNHMultiblockMachineBuilder tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        return (CTNHMultiblockMachineBuilder)super.tooltipBuilder(tooltipBuilder);
    }

    public CTNHMultiblockMachineBuilder appearance(Supplier<BlockState> state) {
        return (CTNHMultiblockMachineBuilder)super.appearance(state);
    }

    public CTNHMultiblockMachineBuilder appearanceBlock(Supplier<? extends Block> block) {
        return (CTNHMultiblockMachineBuilder)super.appearanceBlock(block);
    }

    public CTNHMultiblockMachineBuilder langValue(String langValue) {
        return (CTNHMultiblockMachineBuilder)super.langValue(langValue);
    }

    public CTNHMultiblockMachineBuilder tooltips(Component... components) {
        return (CTNHMultiblockMachineBuilder)super.tooltips(components);
    }

    public CTNHMultiblockMachineBuilder tooltips(Lang[] tooltip) {
        List<Component> tooltips = new ArrayList<>();
        for(var t: tooltip){
            tooltips.add(t.translate());
        }
        return (CTNHMultiblockMachineBuilder)super.tooltips(tooltips);
    }

    public CTNHMultiblockMachineBuilder conditionalTooltip(Component component, Supplier<Boolean> condition) {
        return (CTNHMultiblockMachineBuilder) super.conditionalTooltip(component, (Boolean)condition.get());
    }

    public CTNHMultiblockMachineBuilder conditionalTooltip(Component component, boolean condition) {
        if (condition) {
            this.tooltips(component);
        }

        return this;
    }

    public CTNHMultiblockMachineBuilder abilities(PartAbility... abilities) {
        return (CTNHMultiblockMachineBuilder)super.abilities(abilities);
    }

    public CTNHMultiblockMachineBuilder paintingColor(int paintingColor) {
        return (CTNHMultiblockMachineBuilder)super.paintingColor(paintingColor);
    }

    public CTNHMultiblockMachineBuilder recipeModifier(RecipeModifier recipeModifier) {
        return (CTNHMultiblockMachineBuilder)super.recipeModifier(recipeModifier);
    }

    public CTNHMultiblockMachineBuilder recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        return (CTNHMultiblockMachineBuilder)super.recipeModifier(recipeModifier, alwaysTryModifyRecipe);
    }

    public CTNHMultiblockMachineBuilder recipeModifiers(RecipeModifier... recipeModifiers) {
        return (CTNHMultiblockMachineBuilder)super.recipeModifiers(recipeModifiers);
    }

    public CTNHMultiblockMachineBuilder recipeModifiers(boolean alwaysTryModifyRecipe, RecipeModifier... recipeModifiers) {
        return (CTNHMultiblockMachineBuilder)super.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers);
    }

    public CTNHMultiblockMachineBuilder noRecipeModifier() {
        return (CTNHMultiblockMachineBuilder)super.noRecipeModifier();
    }

    public CTNHMultiblockMachineBuilder alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
        return (CTNHMultiblockMachineBuilder)super.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
    }

    public CTNHMultiblockMachineBuilder beforeWorking(BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking) {
        return (CTNHMultiblockMachineBuilder)super.beforeWorking(beforeWorking);
    }

    public CTNHMultiblockMachineBuilder onWorking(Predicate<IRecipeLogicMachine> onWorking) {
        return (CTNHMultiblockMachineBuilder)super.onWorking(onWorking);
    }

    public CTNHMultiblockMachineBuilder onWaiting(Consumer<IRecipeLogicMachine> onWaiting) {
        return (CTNHMultiblockMachineBuilder)super.onWaiting(onWaiting);
    }

    public CTNHMultiblockMachineBuilder afterWorking(Consumer<IRecipeLogicMachine> afterWorking) {
        return (CTNHMultiblockMachineBuilder)super.afterWorking(afterWorking);
    }

    public CTNHMultiblockMachineBuilder regressWhenWaiting(boolean dampingWhenWaiting) {
        return (CTNHMultiblockMachineBuilder)super.regressWhenWaiting(dampingWhenWaiting);
    }

    public CTNHMultiblockMachineBuilder editableUI(@Nullable EditableMachineUI editableUI) {
        return (CTNHMultiblockMachineBuilder)super.editableUI(editableUI);
    }

    public CTNHMultiblockMachineBuilder onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        return (CTNHMultiblockMachineBuilder)super.onBlockEntityRegister(onBlockEntityRegister);
    }
    @Generated
    public CTNHMultiblockMachineBuilder generator(boolean generator) {
        super.generator(generator);
        return this;
    }

    @Generated
    public CTNHMultiblockMachineBuilder pattern(Function<MultiblockMachineDefinition, BlockPattern> pattern) {
        super.pattern(pattern);
        return this;
    }

    @Generated
    public CTNHMultiblockMachineBuilder allowExtendedFacing(boolean allowExtendedFacing) {
        super.allowExtendedFacing(allowExtendedFacing);
        return this;
    }
    @Generated
    public CTNHMultiblockMachineBuilder addUnderTooltip(String tooltip) {
        this.under_component=tooltip;
        return this;
    }

    @Generated
    public CTNHMultiblockMachineBuilder allowFlip(boolean allowFlip) {
        super.allowFlip(allowFlip);
        return this;
    }

    @Generated
    public CTNHMultiblockMachineBuilder partSorter(Comparator<IMultiPart> partSorter) {
        super.partSorter(partSorter);
        return this;
    }

    @Generated
    public CTNHMultiblockMachineBuilder partAppearance(TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance) {
        super.partAppearance(partAppearance);
        return this;
    }

    @Generated
    public BiConsumer<IMultiController, List<Component>> additionalDisplay() {
        return super.additionalDisplay();
    }

    @Generated
    public CTNHMultiblockMachineBuilder additionalDisplay(BiConsumer<IMultiController, List<Component>> additionalDisplay) {
        super.additionalDisplay(additionalDisplay);
        return this;
    }

    @Override
    public MultiblockMachineDefinition register() {
        this.tooltips(Component.literal("————————————————————————"),
                Component.translatable(under_component));
        return super.register();
    }
}
