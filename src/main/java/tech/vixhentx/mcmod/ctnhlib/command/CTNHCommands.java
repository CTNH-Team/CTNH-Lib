package tech.vixhentx.mcmod.ctnhlib.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import tech.vixhentx.mcmod.ctnhlib.command.CTNHCommandInspector.FluidEntry;
import tech.vixhentx.mcmod.ctnhlib.command.CTNHCommandInspector.InspectType;
import tech.vixhentx.mcmod.ctnhlib.command.CTNHCommandInspector.TagMember;

import java.util.List;
import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Brigadier 注册入口，用于 {@code /ctnh} 检查命令。
 * 权限等级为 0，因此所有玩家均可执行。
 */
public final class CTNHCommands {

    private static final ChatFormatting NAME_COLOR = ChatFormatting.AQUA;
    private static final ChatFormatting RESOURCE_ID_COLOR = ChatFormatting.GREEN;
    private static final ChatFormatting COUNT_COLOR = ChatFormatting.YELLOW;
    private static final ChatFormatting NBT_COLOR = ChatFormatting.LIGHT_PURPLE;
    private static final ChatFormatting TAG_COLOR = ChatFormatting.DARK_GREEN;
    private static final ChatFormatting MOD_COLOR = ChatFormatting.DARK_AQUA;

    private static final TagKey<Block> ORE_BLOCKS_TAG = TagKey.create(Registries.BLOCK,
            new ResourceLocation("forge", "ores"));

    /** 根据请求的检查类型，建议已知的物品/方块/流体标签 ID。 */
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TAGS = (ctx, builder) -> {
        InspectType type = parseInspectType(ctx);
        if (type == null) {
            return builder.buildFuture();
        }
        RegistryAccess access = ctx.getSource().registryAccess();
        ResourceKey<? extends Registry<?>> registryKey = CTNHCommandInspector.registryKeyFor(type);
        Optional<? extends Registry<?>> registry = access.registry(registryKey);
        registry.ifPresent(r -> SharedSuggestionProvider.suggestResource(
                r.getTagNames().map(t -> t.location()), builder));
        return builder.buildFuture();
    };

    private CTNHCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                @SuppressWarnings("unused") CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> root = literal("ctnh")
                .requires(src -> src.hasPermission(0))
                .then(literal("hand")
                        .executes(CTNHCommands::executeHand))
                .then(literal("showtag")
                        .then(literal("item")
                                .then(argument("tag", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_TAGS)
                                        .executes(ctx -> executeShowTag(ctx, InspectType.ITEM))))
                        .then(literal("block")
                                .then(argument("tag", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_TAGS)
                                        .executes(ctx -> executeShowTag(ctx, InspectType.BLOCK))))
                        .then(literal("fluid")
                                .then(argument("tag", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_TAGS)
                                        .executes(ctx -> executeShowTag(ctx, InspectType.FLUID)))))
                .then(literal("showores")
                        .requires(src -> src.hasPermission(2))
                        .then(argument("radius", IntegerArgumentType.integer(1, 4))
                                .executes(CTNHCommands::executeShowOres)));
        dispatcher.register(root);
    }

    private static InspectType parseInspectType(CommandContext<CommandSourceStack> ctx) {
        String input = ctx.getInput();
        if (input.contains(" item ")) return InspectType.ITEM;
        if (input.contains(" block ")) return InspectType.BLOCK;
        if (input.contains(" fluid ")) return InspectType.FLUID;
        return null;
    }

    // ---- /ctnh hand 手持物品检查 ----------------------------------------------------

    private static int executeHand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(CTNHCommandChatHelper.error(
                    Component.translatable("command.ctnhlib.error.player_only")));
            return 0;
        }
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.isEmpty()) {
            source.sendFailure(CTNHCommandChatHelper.error(
                    Component.translatable("command.ctnhlib.error.empty_hand")));
            return 0;
        }
        sendHandReport(player, stack);
        return 1;
    }

    private static void sendHandReport(ServerPlayer player, ItemStack stack) {
        ResourceLocation itemId = CTNHCommandInspector.itemId(stack);
        Component itemName = stack.getHoverName();
        // 物品信息
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_name"),
                itemName.getString(),
                NAME_COLOR);
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_id"),
                itemId.toString(),
                RESOURCE_ID_COLOR);
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_mod"),
                modDisplay(itemId),
                itemId.getNamespace(),
                MOD_COLOR);
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_count"),
                String.valueOf(stack.getCount()),
                COUNT_COLOR);

        CompoundTag tag = stack.getTag();
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_nbt"),
                CTNHCommandInspector.prettyNbt(tag),
                NBT_COLOR);

        CommandSourceStack source = player.createCommandSourceStack();
        sendTagLines(player,
                source,
                Component.translatable("command.ctnhlib.hand.item_tags"),
                CTNHCommandInspector.itemTags(stack),
                InspectType.ITEM);

        // 方块信息（如适用）
        ResourceLocation blockId = CTNHCommandInspector.blockId(stack);
        if (blockId != null) {
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.block_name"),
                    Component.translatable(stack.getItem().getDescriptionId()).getString(),
                    NAME_COLOR);
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.block_id"),
                    blockId.toString(),
                    RESOURCE_ID_COLOR);
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.block_mod"),
                    modDisplay(blockId),
                    blockId.getNamespace(),
                    MOD_COLOR);
            sendTagLines(player,
                    source,
                    Component.translatable("command.ctnhlib.hand.block_tags"),
                    CTNHCommandInspector.blockTags(stack),
                    InspectType.BLOCK);
        }

        // 流体信息（如适用）
        List<FluidEntry> fluids = CTNHCommandInspector.fluidsIn(stack);
        if (fluids.isEmpty()) {
            player.sendSystemMessage(CTNHCommandChatHelper.info(
                    Component.translatable("command.ctnhlib.hand.no_fluid")));
            return;
        }
        for (int i = 0; i < fluids.size(); i++) {
            FluidEntry entry = fluids.get(i);
            player.sendSystemMessage(CTNHCommandChatHelper.heading(
                    Component.translatable("command.ctnhlib.hand.fluid_header", i + 1)));
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_name"),
                    entry.displayName().getString(),
                    NAME_COLOR);
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_id"),
                    entry.id().toString(),
                    RESOURCE_ID_COLOR);
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_mod"),
                    modDisplay(entry.id()),
                    entry.id().getNamespace(),
                    MOD_COLOR);
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_amount"),
                    String.valueOf(entry.amount()),
                    COUNT_COLOR);
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_nbt"),
                    CTNHCommandInspector.prettyNbt(entry.tag()),
                    NBT_COLOR);
            sendTagLines(player,
                    source,
                    Component.translatable("command.ctnhlib.hand.fluid_tags"),
                    entry.tags(),
                    InspectType.FLUID);
        }
    }

    // ---- /ctnh showtag 标签展示 ----------------------------------------------------

    private static int executeShowTag(CommandContext<CommandSourceStack> ctx, InspectType type) {
        CommandSourceStack source = ctx.getSource();
        ResourceLocation tagId = ResourceLocationArgument.getId(ctx, "tag");
        Optional<HolderSet.Named<?>> tagSet = CTNHCommandInspector.resolveTag(source, type, tagId);
        if (tagSet.isEmpty()) {
            source.sendFailure(CTNHCommandChatHelper.error(
                    Component.translatable("command.ctnhlib.error.unknown_tag",
                            tagId.toString(), type.registryDisplay())));
            return 0;
        }
        List<TagMember> members = CTNHCommandInspector.listTagMembers(type, tagSet.get());
        if (members.isEmpty()) {
            source.sendFailure(CTNHCommandChatHelper.error(
                    Component.translatable("command.ctnhlib.error.empty_tag",
                            tagId.toString(), type.registryDisplay())));
            return 0;
        }
        Component header = Component.translatable("command.ctnhlib.showtag.header",
                tagId.toString(), type.registryDisplay()).withStyle(ChatFormatting.GOLD);
        source.sendSuccess(() -> header, false);
        Component count = Component.translatable("command.ctnhlib.showtag.count", members.size())
                .withStyle(ChatFormatting.GRAY);
        source.sendSuccess(() -> count, false);
        for (TagMember member : members) {
            String displayName = member.displayName().getString();
            String idString = member.id().toString();
            String copyText = idString;
            String langKey = type == InspectType.FLUID ?
                    "command.ctnhlib.showtag.fluid_member" :
                    "command.ctnhlib.showtag.member";
            net.minecraft.network.chat.MutableComponent visible = Component
                    .translatable(langKey, displayName, idString)
                    .withStyle(ChatFormatting.WHITE);
            Component line = CTNHCommandChatHelper.clickableLine(visible, copyText);
            source.sendSuccess(() -> line, false);
        }
        return members.size();
    }

    // ---- /ctnh showores 矿脉观察（清除区块柱内除矿石外的方块） ----------------------

    private static int executeShowOres(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(CTNHCommandChatHelper.error(
                    Component.translatable("command.ctnhlib.error.player_only")));
            return 0;
        }
        int radius = IntegerArgumentType.getInteger(ctx, "radius");
        ServerLevel level = source.getLevel();
        ChunkPos center = new ChunkPos(player.blockPosition());
        long startNanos = System.nanoTime();
        int cleared = 0;
        int kept = 0;
        int skippedChunks = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = center.x + dx;
                int chunkZ = center.z + dz;
                if (!level.hasChunk(chunkX, chunkZ)) {
                    skippedChunks++;
                    continue;
                }
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                LevelChunkSection[] sections = chunk.getSections();
                for (int s = 0; s < sections.length; s++) {
                    LevelChunkSection section = sections[s];
                    if (section.hasOnlyAir()) {
                        continue;
                    }
                    int baseY = (level.getMinSection() + s) * 16;
                    for (int lx = 0; lx < 16; lx++) {
                        for (int lz = 0; lz < 16; lz++) {
                            for (int ly = 0; ly < 16; ly++) {
                                BlockState state = section.getBlockState(lx, ly, lz);
                                if (state.isAir()) {
                                    continue;
                                }
                                if (state.is(ORE_BLOCKS_TAG)) {
                                    kept++;
                                    continue;
                                }
                                cursor.set((chunkX << 4) + lx, baseY + ly, (chunkZ << 4) + lz);
                                if (level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                                    cleared++;
                                }
                            }
                        }
                    }
                }
            }
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        int clearedCount = cleared;
        int keptCount = kept;
        int skippedCount = skippedChunks;
        source.sendSuccess(() -> Component.translatable("command.ctnhlib.showores.done",
                clearedCount, keptCount, skippedCount, elapsedMs), false);
        return 1;
    }

    // ---- 辅助方法 -------------------------------------------------------------------

    private static void sendLabeled(ServerPlayer player, Component label, String value) {
        sendLabeled(player, label, value, ChatFormatting.WHITE);
    }

    private static void sendLabeled(ServerPlayer player, Component label, String value, ChatFormatting valueColor) {
        sendLabeled(player, label, value, value, valueColor);
    }

    private static void sendLabeled(ServerPlayer player,
                                    Component label,
                                    String value,
                                    String copyText,
                                    ChatFormatting valueColor) {
        String safeValue = value == null || value.isEmpty() ?
                Component.translatable("command.ctnhlib.value.empty").getString() :
                value;
        for (Component line : CTNHCommandChatHelper.labeledLines(label, safeValue, copyText, valueColor)) {
            player.sendSystemMessage(line);
        }
    }

    private static void sendTagLines(ServerPlayer player,
                                     CommandSourceStack source,
                                     Component label,
                                     List<ResourceLocation> tagIds,
                                     InspectType type) {
        List<String> tagIdStrings = tagIds == null ?
                List.of() :
                tagIds.stream().map(ResourceLocation::toString).toList();
        for (Component line : CTNHCommandChatHelper.labeledTagLines(label, tagIdStrings, TAG_COLOR,
                tagId -> tagHover(type, tagMemberCount(source, type, ResourceLocation.tryParse(tagId))))) {
            player.sendSystemMessage(line);
        }
    }

    private static String modDisplay(ResourceLocation id) {
        return "@" + id.getNamespace();
    }

    private static int tagMemberCount(CommandSourceStack source, InspectType type, ResourceLocation tagId) {
        if (tagId == null) {
            return 0;
        }
        return CTNHCommandInspector.resolveTag(source, type, tagId)
                .map(set -> CTNHCommandInspector.listTagMembers(type, set).size())
                .orElse(0);
    }

    private static Component tagHover(InspectType type, int count) {
        String key = switch (type) {
            case ITEM -> "command.ctnhlib.copy.hover.item_tag";
            case BLOCK -> "command.ctnhlib.copy.hover.block_tag";
            case FLUID -> "command.ctnhlib.copy.hover.fluid_tag";
        };
        return Component.translatable(key, count);
    }
}
