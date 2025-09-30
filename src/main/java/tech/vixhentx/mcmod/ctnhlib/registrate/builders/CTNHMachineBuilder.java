package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;


import java.util.function.BiFunction;
import java.util.function.Function;

@Accessors(chain = true, fluent = true)
public class CTNHMachineBuilder <DEFINITION extends MachineDefinition> extends MachineBuilder<DEFINITION> implements ICNBuilder{
    // getter for KJS
    @Setter
    @Nullable
    private String cnLangValue = null;


    public CTNHMachineBuilder(GTRegistrate registrate, String name, Function<ResourceLocation, DEFINITION> definition, Function<IMachineBlockEntity, MetaMachine> machine, BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory, BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory, TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, definition, machine, blockFactory, itemFactory, blockEntityFactory);
    }

    public CTNHMachineBuilder(GTRegistrate registrate, String name, String cnname, Function<ResourceLocation, DEFINITION> definition, Function<IMachineBlockEntity, MetaMachine> machine, BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory, BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory, TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(registrate, name, definition, machine, blockFactory, itemFactory, blockEntityFactory);
        cnLangValue = cnname;
    }

    @Override
    public String getCNLangValue() {
        return cnLangValue;
    }
}
