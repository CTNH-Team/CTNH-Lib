package tech.vixhentx.mcmod.ctnhlib.jade;

import com.gregtechceu.gtceu.integration.jade.provider.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 基于 GTJadePlugin 原始注册顺序的优先级注册器。
 * 每个 provider 的优先级使用固定数字（1100, 1200...），便于直观看出插入点。
 */
public final class GTProvidersRegistrar {

    private GTProvidersRegistrar() {}

    public static void init() {
        registerBlockDataProviders();
        registerBlockComponentProviders();
    }

    private static void registerBlockDataProviders() {
        JadePriorityManager.registerBlockData(new ElectricContainerBlockProvider(), BlockEntity.class, 1100,
                "electric_container_data");
        JadePriorityManager.registerBlockData(new WorkLogicMachineProvider(), BlockEntity.class, 1200, "workable_data");
        JadePriorityManager.registerBlockData(new ControllableBlockProvider(), BlockEntity.class, 1300,
                "controllable_data");
        JadePriorityManager.registerBlockData(new RecipeLogicProvider(), BlockEntity.class, 1400,
                "recipe_logic_data");
        JadePriorityManager.registerBlockData(new ParallelProvider(), BlockEntity.class, 1500, "parallel_data");
        JadePriorityManager.registerBlockData(new RecipeOutputProvider(), BlockEntity.class, 1600,
                "recipe_output_data");
        JadePriorityManager.registerBlockData(new MultiblockStructureProvider(), BlockEntity.class, 1700,
                "multiblock_structure_data");
        JadePriorityManager.registerBlockData(new MaintenanceBlockProvider(), BlockEntity.class, 1800,
                "maintenance_data");
        JadePriorityManager.registerBlockData(new ExhaustVentBlockProvider(), BlockEntity.class, 1900,
                "exhaust_vent_data");
        JadePriorityManager.registerBlockData(new SteamBoilerBlockProvider(), BlockEntity.class, 2000,
                "steam_boiler_data");
        JadePriorityManager.registerBlockData(new AutoOutputBlockProvider(), BlockEntity.class, 2100,
                "auto_output_data");
        JadePriorityManager.registerBlockData(new CableBlockProvider(), BlockEntity.class, 2200, "cable_data");
        JadePriorityManager.registerBlockData(new MachineModeProvider(), BlockEntity.class, 2300, "machine_mode_data");
        JadePriorityManager.registerBlockData(new StainedColorProvider(), BlockEntity.class, 2400,
                "stained_color_data");
        JadePriorityManager.registerBlockData(new HazardCleanerBlockProvider(), BlockEntity.class, 2500,
                "hazard_cleaner_data");
        JadePriorityManager.registerBlockData(new TransformerBlockProvider(), BlockEntity.class, 2600,
                "transformer_data");
        JadePriorityManager.registerBlockData(new PrimitivePumpBlockProvider(), BlockEntity.class, 2700,
                "primitive_pump_data");
        JadePriorityManager.registerBlockData(new DataBankBlockProvider(), BlockEntity.class, 2750, "databank_data");
        JadePriorityManager.registerBlockData(new EnergyConverterModeProvider(), BlockEntity.class, 2800,
                "energy_converter_mode_data");
    }

    private static void registerBlockComponentProviders() {
        JadePriorityManager.registerBlockComponent(new ElectricContainerBlockProvider(), Block.class, 1100,
                "electric_container_component");
        JadePriorityManager.registerBlockComponent(new WorkLogicMachineProvider(), Block.class, 1200,
                "workable_component");
        JadePriorityManager.registerBlockComponent(new ControllableBlockProvider(), Block.class, 1300,
                "controllable_component");
        JadePriorityManager.registerBlockComponent(new RecipeLogicProvider(), Block.class, 1400,
                "recipe_logic_component");
        JadePriorityManager.registerBlockComponent(new ParallelProvider(), Block.class, 1500, "parallel_component");
        JadePriorityManager.registerBlockComponent(new RecipeOutputProvider(), Block.class, 1600,
                "recipe_output_component");
        JadePriorityManager.registerBlockComponent(new MultiblockStructureProvider(), Block.class, 1700,
                "multiblock_structure_component");
        JadePriorityManager.registerBlockComponent(new MaintenanceBlockProvider(), Block.class, 1800,
                "maintenance_component");
        JadePriorityManager.registerBlockComponent(new ExhaustVentBlockProvider(), Block.class, 1900,
                "exhaust_vent_component");
        JadePriorityManager.registerBlockComponent(new SteamBoilerBlockProvider(), Block.class, 2000,
                "steam_boiler_component");
        JadePriorityManager.registerBlockComponent(new AutoOutputBlockProvider(), Block.class, 2100,
                "auto_output_component");
        JadePriorityManager.registerBlockComponent(new CableBlockProvider(), Block.class, 2200, "cable_component");
        JadePriorityManager.registerBlockComponent(new MachineModeProvider(), Block.class, 2300,
                "machine_mode_component");
        JadePriorityManager.registerBlockComponent(new StainedColorProvider(), Block.class, 2400,
                "stained_color_component");
        JadePriorityManager.registerBlockComponent(new HazardCleanerBlockProvider(), Block.class, 2500,
                "hazard_cleaner_component");
        JadePriorityManager.registerBlockComponent(new TransformerBlockProvider(), Block.class, 2600,
                "transformer_component");
        JadePriorityManager.registerBlockComponent(new PrimitivePumpBlockProvider(), Block.class, 2700,
                "primitive_pump_component");
        JadePriorityManager.registerBlockComponent(new DataBankBlockProvider(), Block.class, 2750, "databank_data");
        JadePriorityManager.registerBlockComponent(new EnergyConverterModeProvider(), Block.class, 2800,
                "energy_converter_mode_component");
    }
}
