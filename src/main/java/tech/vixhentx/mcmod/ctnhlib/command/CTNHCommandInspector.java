package tech.vixhentx.mcmod.ctnhlib.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用于 /ctnh 检查命令的只读工具辅助类。集中处理物品、方块和流体的
 * 资源 ID/名称/NBT/标签提取，以及通过 {@link RegistryAccess} 进行标签成员
 * 枚举，从而在运行时尊重数据包变更。
 */
public final class CTNHCommandInspector {

    private CTNHCommandInspector() {}

    /** 描述手持容器中单个流体的轻量级负载数据结构。 */
    public record FluidEntry(FluidStack stack) {

        public Fluid fluid() {
            return stack.getFluid();
        }

        public Component displayName() {
            return stack.getDisplayName();
        }

        public ResourceLocation id() {
            ResourceLocation key = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
            return key != null ? key : ResourceLocation.tryBuild("minecraft", "empty");
        }

        public int amount() {
            return stack.getAmount();
        }

        @Nullable
        public CompoundTag tag() {
            return stack.getTag();
        }

        public List<ResourceLocation> tags() {
            List<ResourceLocation> out = new ArrayList<>();
            stack.getFluid().builtInRegistryHolder().tags().forEach(t -> out.add(t.location()));
            Collections.sort(out, (a, b) -> a.toString().compareTo(b.toString()));
            return out;
        }
    }

    /** 物品注册 ID，缺失时回退为 {@code minecraft:air}。 */
    public static ResourceLocation itemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null ? id : ResourceLocation.tryBuild("minecraft", "air");
    }

    /** {@link BlockItem} 的方块注册 ID，如果该物品栈不是方块物品则返回 null。 */
    @Nullable
    public static ResourceLocation blockId(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return ForgeRegistries.BLOCKS.getKey(block);
        }
        return null;
    }

    /** 附加到物品栈物品上的已排序物品标签 ID。 */
    public static List<ResourceLocation> itemTags(ItemStack stack) {
        List<ResourceLocation> out = new ArrayList<>();
        stack.getItem().builtInRegistryHolder().tags().forEach(t -> out.add(t.location()));
        Collections.sort(out, (a, b) -> a.toString().compareTo(b.toString()));
        return out;
    }

    /** 附加到 {@link BlockItem} 背后方块上的已排序方块标签 ID。 */
    public static List<ResourceLocation> blockTags(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return Collections.emptyList();
        }
        List<ResourceLocation> out = new ArrayList<>();
        blockItem.getBlock().builtInRegistryHolder().tags().forEach(t -> out.add(t.location()));
        Collections.sort(out, (a, b) -> a.toString().compareTo(b.toString()));
        return out;
    }

    /**
     * 枚举物品栈中包含的所有非空流体。多槽容器逐槽遍历其
     * {@link IFluidHandlerItem}；单流体物品回退到
     * {@link FluidUtil#getFluidContained(ItemStack)} 以保证兼容性。
     */
    public static List<FluidEntry> fluidsIn(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<IFluidHandlerItem> handlerOpt = stack
                .getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM)
                .resolve();
        List<FluidEntry> entries = new ArrayList<>();
        if (handlerOpt.isPresent()) {
            IFluidHandler handler = handlerOpt.get();
            int tanks = handler.getTanks();
            for (int i = 0; i < tanks; i++) {
                FluidStack fluid = handler.getFluidInTank(i);
                if (fluid != null && !fluid.isEmpty()) {
                    entries.add(new FluidEntry(fluid.copy()));
                }
            }
            if (!entries.isEmpty()) {
                return entries;
            }
        }
        FluidUtil.getFluidContained(stack)
                .filter(f -> !f.isEmpty())
                .ifPresent(f -> entries.add(new FluidEntry(f.copy())));
        return entries;
    }

    /** 将给定的检查类型解析为对应的 {@code ResourceKey<Registry<?>>}。 */
    public static ResourceKey<? extends Registry<?>> registryKeyFor(InspectType type) {
        return switch (type) {
            case ITEM -> Registries.ITEM;
            case BLOCK -> Registries.BLOCK;
            case FLUID -> Registries.FLUID;
        };
    }

    /**
     * 根据命令源暴露的运行时注册表解析标签。如果标签存在则返回
     * holder set，否则返回 {@link Optional#empty()}。
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<HolderSet.Named<?>> resolveTag(CommandSourceStack source,
                                                          InspectType type,
                                                          ResourceLocation tagId) {
        ResourceKey registryKey = registryKeyFor(type);
        RegistryAccess access = source.registryAccess();
        Optional<? extends Registry<?>> registryOpt = access.registry(registryKey);
        if (registryOpt.isEmpty()) {
            return Optional.empty();
        }
        Registry registry = registryOpt.get();
        TagKey tagKey = TagKey.create(registryKey, tagId);
        return registry.getTag(tagKey).map(set -> (HolderSet.Named<?>) set);
    }

    /** 按迭代顺序返回标签成员，映射为（显示名称，注册 ID）对。 */
    public static List<TagMember> listTagMembers(InspectType type, HolderSet.Named<?> set) {
        List<TagMember> result = new ArrayList<>();
        for (Holder<?> holder : set) {
            Object value = holder.value();
            ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
            if (id == null) {
                continue;
            }
            Component name = displayNameFor(type, value, id);
            result.add(new TagMember(id, name));
        }
        return result;
    }

    private static Component displayNameFor(InspectType type, Object value, ResourceLocation id) {
        return switch (type) {
            case ITEM -> {
                if (value instanceof net.minecraft.world.item.Item item) {
                    yield Component.translatable(item.getDescriptionId());
                }
                yield Component.literal(id.toString());
            }
            case BLOCK -> {
                if (value instanceof Block block) {
                    yield Component.translatable(block.getDescriptionId());
                }
                yield Component.literal(id.toString());
            }
            case FLUID -> {
                if (value instanceof Fluid fluid) {
                    yield new FluidStack(fluid, 1).getDisplayName();
                }
                yield Component.literal(id.toString());
            }
        };
    }

    /** {@link #listTagMembers(InspectType, HolderSet.Named)} 的结果条目。 */
    public record TagMember(ResourceLocation id, Component displayName) {}

    /** 将 CompoundTag 格式化为适合聊天显示的字符串（单行 SNBT 形式）。 */
    public static String prettyNbt(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        return tag.toString();
    }

    /** 检查目标类型。 */

    public enum InspectType {

        ITEM,
        BLOCK,
        FLUID;

        public String registryDisplay() {
            return switch (this) {
                case ITEM -> "minecraft:item";
                case BLOCK -> "minecraft:block";
                case FLUID -> "minecraft:fluid";
            };
        }
    }
}
