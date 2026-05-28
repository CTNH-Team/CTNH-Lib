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
 * Read-only utility helpers for the /ctnh inspection commands. Centralizes
 * resource-id/name/NBT/tag extraction for items, blocks, and fluids, and tag-member
 * enumeration via {@link RegistryAccess} so datapack changes are honored at runtime.
 */
public final class CTNHCommandInspector {

    private CTNHCommandInspector() {}

    /** Lightweight payload describing a single fluid inside a held container. */
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

    /** Item registry id, falling back to {@code minecraft:air} when missing. */
    public static ResourceLocation itemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null ? id : ResourceLocation.tryBuild("minecraft", "air");
    }

    /** Block registry id for a {@link BlockItem}, or null when the stack is not a block item. */
    @Nullable
    public static ResourceLocation blockId(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return ForgeRegistries.BLOCKS.getKey(block);
        }
        return null;
    }

    /** Sorted item tag ids attached to the stack's item. */
    public static List<ResourceLocation> itemTags(ItemStack stack) {
        List<ResourceLocation> out = new ArrayList<>();
        stack.getItem().builtInRegistryHolder().tags().forEach(t -> out.add(t.location()));
        Collections.sort(out, (a, b) -> a.toString().compareTo(b.toString()));
        return out;
    }

    /** Sorted block tag ids attached to the block backing a {@link BlockItem}. */
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
     * Enumerate every non-empty fluid contained in the stack. Multi-tank containers
     * walk their {@link IFluidHandlerItem} tank-by-tank; single-fluid items fall back
     * to {@link FluidUtil#getFluidContained(ItemStack)} for compatibility.
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

    /** Resolve a string to {@code ResourceKey<Registry<?>>} for the given inspection type. */
    public static ResourceKey<? extends Registry<?>> registryKeyFor(InspectType type) {
        return switch (type) {
            case ITEM -> Registries.ITEM;
            case BLOCK -> Registries.BLOCK;
            case FLUID -> Registries.FLUID;
        };
    }

    /**
     * Resolve a tag against the runtime registry exposed by the command source. Returns the
     * holder set if the tag exists, otherwise {@link Optional#empty()}.
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

    /** Members of a tag in iteration order, mapped to (display name, registry id) pairs. */
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

    /** Result entry for {@link #listTagMembers(InspectType, HolderSet.Named)}. */
    public record TagMember(ResourceLocation id, Component displayName) {}

    /** Pretty-print a CompoundTag for chat display (single-line SNBT form). */
    public static String prettyNbt(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return "";
        }
        return tag.toString();
    }

    /** Inspection target type. */

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
