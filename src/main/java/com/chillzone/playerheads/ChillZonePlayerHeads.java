package com.chillzone.playerheads;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Chill Zone server-side port/reimplementation of the MIT CustomPlayerHeads death-drop concept.
 * Drops one vanilla player head on PvP deaths; shows killer, dimension and date (never coordinates).
 */
public final class ChillZonePlayerHeads implements ModInitializer {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final ZoneId DATE_ZONE = ZoneId.of("America/Toronto");

    // A death callback runs before the entire death/respawn process has finished.
    // Queue drops on the server thread so fake players cannot grab their own head
    // while their old entity is still being cleaned up.
    private static final List<PendingHead> PENDING = new ArrayList<>();
    private record PendingHead(ServerLevel level, double x, double y, double z, ItemStack head, int ticks) {}

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<PendingHead> iterator = PENDING.iterator();
            List<PendingHead> ready = new ArrayList<>();
            List<PendingHead> later = new ArrayList<>();
            while (iterator.hasNext()) {
                PendingHead pending = iterator.next();
                iterator.remove();
                if (pending.ticks() > 1) {
                    later.add(new PendingHead(pending.level(), pending.x(), pending.y(), pending.z(), pending.head(), pending.ticks() - 1));
                } else {
                    ready.add(pending);
                }
            }
            PENDING.addAll(later);
            for (PendingHead pending : ready) {
                ItemEntity drop = new ItemEntity(pending.level(), pending.x(), pending.y(), pending.z(), pending.head());
                drop.setPickUpDelay(20); // Another 1 second before ANY entity can collect it.
                pending.level().addFreshEntity(drop);
            }
        });
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

            // Compare ResourceKey objects directly rather than showing their debug strings.
            String dimension = level.dimension().equals(net.minecraft.world.level.Level.NETHER) ? "Nether"
                    : level.dimension().equals(net.minecraft.world.level.Level.END) ? "End"
                    : level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD) ? "Overworld" : "Other";
            String timestamp = LocalDateTime.now(DATE_ZONE).format(DATE);
            head.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Killed by: " + killer.getGameProfile().name()).withStyle(style -> style.withColor(ChatFormatting.RED).withItalic(false)),
                Component.literal("Dimension: " + dimension).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)),
                Component.literal("Date: " + timestamp).withStyle(style -> style.withColor(ChatFormatting.YELLOW).withItalic(false))
            )));

            PENDING.add(new PendingHead(level, player.getX(), player.getY() + 0.4D, player.getZ(), head, 15));
        });
    }
}
