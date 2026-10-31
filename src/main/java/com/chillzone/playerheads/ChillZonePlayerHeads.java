package com.chillzone.playerheads;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Chill Zone server-side port/reimplementation of the MIT CustomPlayerHeads death-drop concept.
 * Drops one vanilla player head on PvP deaths; shows killer, dimension and date (never coordinates).
 */
public final class ChillZonePlayerHeads implements ModInitializer {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final ZoneId DATE_ZONE = ZoneId.of("America/Toronto");

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, damage) -> {
            if (!(victim instanceof ServerPlayer player)) return;
            Entity attacker = damage.getEntity();
            if (!(attacker instanceof ServerPlayer killer) || killer == player) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            // Static profile keeps the victim skin from their server game profile.
            head.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
            head.set(DataComponents.CUSTOM_NAME, Component.literal(player.getGameProfile().name() + "'s Head")
                    .withStyle(ChatFormatting.GOLD).withStyle(style -> style.withItalic(false)));

            String dimensionKey = level.dimension().toString();
            String dimension = dimensionKey.contains("the_nether") ? "Nether"
                    : dimensionKey.contains("the_end") ? "End"
                    : dimensionKey.contains("overworld") ? "Overworld" : "Other";
            String timestamp = LocalDateTime.now(DATE_ZONE).format(DATE);
            head.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Killed by: " + killer.getGameProfile().name()).withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)),
                Component.literal("Dimension: " + dimension).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)),
                Component.literal("Date: " + timestamp).withStyle(style -> style.withColor(ChatFormatting.YELLOW).withItalic(false))
            )));

            ItemEntity drop = new ItemEntity(level, player.getX(), player.getY() + 0.4D, player.getZ(), head);
            level.addFreshEntity(drop);
        });
    }
}
