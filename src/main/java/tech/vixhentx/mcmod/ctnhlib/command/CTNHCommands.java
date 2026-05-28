package tech.vixhentx.mcmod.ctnhlib.command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import com.mojang.brigadier.CommandDispatcher;
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
 * Brigadier registration entrypoint for the {@code /ctnh} inspection commands.
 * Permission level is 0, so all players can run them.
 */
public final class CTNHCommands {

    /** Suggests known item/block/fluid tag IDs based on the requested inspection type. */
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
                                        .executes(ctx -> executeShowTag(ctx, InspectType.FLUID)))));
        dispatcher.register(root);
    }

    private static InspectType parseInspectType(CommandContext<CommandSourceStack> ctx) {
        String input = ctx.getInput();
        if (input.contains(" item ")) return InspectType.ITEM;
        if (input.contains(" block ")) return InspectType.BLOCK;
        if (input.contains(" fluid ")) return InspectType.FLUID;
        return null;
    }

    // ---- /ctnh hand ----------------------------------------------------------------

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
        // Item header.
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_name"),
                itemName.getString());
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_id"),
                itemId.toString());
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_count"),
                String.valueOf(stack.getCount()));

        CompoundTag tag = stack.getTag();
        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_nbt"),
                CTNHCommandInspector.prettyNbt(tag));

        sendLabeled(player,
                Component.translatable("command.ctnhlib.hand.item_tags"),
                joinIds(CTNHCommandInspector.itemTags(stack)));

        // Block info, if applicable.
        ResourceLocation blockId = CTNHCommandInspector.blockId(stack);
        if (blockId != null) {
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.block_name"),
                    Component.translatable(stack.getItem().getDescriptionId()).getString());
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.block_id"),
                    blockId.toString());
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.block_tags"),
                    joinIds(CTNHCommandInspector.blockTags(stack)));
        }

        // Fluid info, if applicable.
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
                    entry.displayName().getString());
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_id"),
                    entry.id().toString());
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_amount"),
                    String.valueOf(entry.amount()));
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_nbt"),
                    CTNHCommandInspector.prettyNbt(entry.tag()));
            sendLabeled(player,
                    Component.translatable("command.ctnhlib.hand.fluid_tags"),
                    joinIds(entry.tags()));
        }
    }

    // ---- /ctnh showtag -------------------------------------------------------------

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
            String copyText = String.format("- %s (%s)", displayName, idString);
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

    // ---- helpers -------------------------------------------------------------------

    private static String joinIds(List<ResourceLocation> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ids.get(i).toString());
        }
        return sb.toString();
    }

    private static void sendLabeled(ServerPlayer player, Component label, String value) {
        String safeValue = value == null || value.isEmpty() ?
                Component.translatable("command.ctnhlib.value.empty").getString() :
                value;
        for (Component line : CTNHCommandChatHelper.labeledLines(label, safeValue)) {
            player.sendSystemMessage(line);
        }
    }
}
