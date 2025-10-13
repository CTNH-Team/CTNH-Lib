package tech.vixhentx.mcmod.ctnhlib.mixin;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.integration.jade.GTJadePlugin;
import com.gregtechceu.gtceu.integration.jade.provider.FluidPipeStorageProvider;
import com.gregtechceu.gtceu.integration.jade.provider.GTFluidStorageProvider;
import com.gregtechceu.gtceu.integration.jade.provider.GTItemStorageProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;

import tech.vixhentx.mcmod.ctnhlib.jade.JadePriorityManager;

@Mixin(value = GTJadePlugin.class, remap = false)
public class GTJadePluginMixin {
    /**
     * @author luckyblock
     * @reason add priority
     */
    @Overwrite
    public void register(IWailaCommonRegistration registration) {
        for (JadePriorityManager.JadeBlockDataRegistration reg : JadePriorityManager.orderedBlockData()) {
            reg.registerCommon(registration);
        }
        registration.registerItemStorage(GTItemStorageProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerFluidStorage(GTFluidStorageProvider.INSTANCE, MetaMachineBlockEntity.class);
        registration.registerFluidStorage(FluidPipeStorageProvider.INSTANCE, FluidPipeBlockEntity.class);
    }

    /**
     * @author luckyblock
     * @reason add priority
     */
    @Overwrite
    public void registerClient(IWailaClientRegistration registration) {
        // 使用排序后的列表注册 BlockComponentProviders（动态）
        for (JadePriorityManager.JadeBlockComponentRegistration reg : JadePriorityManager.orderedBlockComponent()) {
            reg.registerClient(registration);
        }
        registration.registerItemStorageClient(GTItemStorageProvider.INSTANCE);
        registration.registerFluidStorageClient(GTFluidStorageProvider.INSTANCE);
        registration.registerFluidStorageClient(FluidPipeStorageProvider.INSTANCE);
    }
}
