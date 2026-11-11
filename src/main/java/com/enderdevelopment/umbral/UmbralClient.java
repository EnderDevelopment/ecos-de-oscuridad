package com.enderdevelopment.umbral;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class UmbralClient implements ClientModInitializer {
    private int quietTicks;
    private int manifestationCooldown;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || client.world == null || player.isCreative() || player.isSpectator()) {
            quietTicks = 0;
            manifestationCooldown = 0;
            return;
        }
        quietTicks++;
        manifestationCooldown--;
        if (quietTicks > 1200 && player.getBlockPos().getY() < 45 && client.world.getLightLevel(player.getBlockPos()) <= 3) {
            quietTicks = 0;
            if (client.world.random.nextInt(5) == 0) {
                double angle = player.getYaw() * Math.PI / 180.0;
                double x = player.getX() - Math.sin(angle) * 4.0;
                double z = player.getZ() + Math.cos(angle) * 4.0;
                client.world.addParticle(ParticleTypes.ASH, x, player.getY() + 1.2, z, 0, 0.015, 0);
                client.world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.AMBIENT, 0.18f, 0.45f);
            }
        }
        if (manifestationCooldown <= 0 && client.world.random.nextInt(900) == 0 && client.world.getLightLevel(player.getBlockPos()) <= 2) {
            manifestationCooldown = 500;
            for (int i = 0; i < 8; i++) {
                client.world.addParticle(ParticleTypes.SMOKE, player.getX() + (client.world.random.nextDouble() - 0.5) * 3, player.getY() + 1.0, player.getZ() + (client.world.random.nextDouble() - 0.5) * 3, 0, 0.02, 0);
            }
        }
    }
}
