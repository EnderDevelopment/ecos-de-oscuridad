package com.enderdevelopment.umbral;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class FlashlightItem extends Item {
    public FlashlightItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 240, 0, false, false, true));
            EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.damage(1, user, slot);
            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.6f, 1.8f);
        }
        user.getItemCooldownManager().set(this, 10);
        return TypedActionResult.success(stack, world.isClient());
    }
}
