package com.enderdevelopment.umbral;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.LightType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UmbralMod implements ModInitializer {
    public static final String MOD_ID = "umbral";
    public static final Item FLASHLIGHT = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "flashlight"),
            new FlashlightItem(new Item.Settings().maxCount(1).maxDamage(256))
    );
    private static final Map<UUID, TensionState> TENSION = new HashMap<>();

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(FLASHLIGHT));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayer(player);
            }
        });
    }

    private static void updatePlayer(ServerPlayerEntity player) {
        if (player.isSpectator() || player.isCreative()) {
            return;
        }
        TensionState state = TENSION.computeIfAbsent(player.getUuid(), ignored -> new TensionState());
        int blockLight = player.getWorld().getLightLevel(LightType.BLOCK, player.getBlockPos());
        boolean isolated = player.getWorld().getPlayers(p -> p != player && p.squaredDistanceTo(player) < 24 * 24).isEmpty();
        if (blockLight <= 3 && isolated) {
            state.value = Math.min(100, state.value + 1);
        } else {
            state.value = Math.max(0, state.value - 2);
        }
        if (state.value < 55 || player.age - state.lastEvent < 100) {
            return;
        }
        state.lastEvent = player.age;
        int event = player.getRandom().nextInt(100);
        if (event < 38) {
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.9f, 0.65f + player.getRandom().nextFloat() * 0.35f);
        } else if (event < 65) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 25, 0, false, false, true));
            player.sendMessage(Text.translatable("message.umbral.darkness"), true);
        } else if (event < 86) {
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.AMBIENT, 0.7f, 0.45f);
            player.sendMessage(Text.translatable("message.umbral.behind"), true);
        } else {
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.AMBIENT, 0.3f, 0.7f);
            player.sendMessage(Text.translatable("message.umbral.watching"), true);
        }
        state.value = Math.max(20, state.value - 25);
    }

    private static final class TensionState {
        private int value;
        private int lastEvent = -1000;
    }
}
