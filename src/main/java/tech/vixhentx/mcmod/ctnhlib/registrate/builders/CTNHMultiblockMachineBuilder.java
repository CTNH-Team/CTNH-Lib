package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Generated;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class CTNHMultiblockMachineBuilder extends
                                          MultiblockMachineBuilder<MultiblockMachineDefinition, CTNHMultiblockMachineBuilder>
                                          implements ICNBuilder {

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

    public CTNHMultiblockMachineBuilder recipeTypes(GTRecipeType... recipeTypes) {
        if (recipeTypes.length == 0) {
            return super.recipeTypes(recipeTypes);
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
        return super.recipeTypes(recipeTypes);
    }

    public CTNHMultiblockMachineBuilder recipeType(GTRecipeType recipeType) {
        if (recipeType == GTRecipeTypes.DUMMY_RECIPES) {
            return super.recipeType(recipeType);
        }
        return recipeTypes(recipeType);
    }

    public CTNHMultiblockMachineBuilder tooltips(Lang[] tooltip) {
        List<Component> tooltips = new ArrayList<>();
        for (var t : tooltip) {
            tooltips.add(t.translate());
        }
        return super.tooltips(tooltips);
    }

    public CTNHMultiblockMachineBuilder conditionalTooltip(Component component, boolean condition) {
        if (condition) {
            this.tooltips(component);
        }

        return this;
    }

    @Generated
    public CTNHMultiblockMachineBuilder addUnderTooltip(String tooltip) {
        this.under_component = tooltip;
        return this;
    }

    @Override
    public MultiblockMachineDefinition register() {
        var definition = super.register();
        definition.setTooltipBuilder(definition.getTooltipBuilder().andThen(
                (is, c) -> {
                    c.add(Component.literal("————————————————————————"));
                    c.add(Component.translatable(under_component));
                }));
        return definition;
    }
}
