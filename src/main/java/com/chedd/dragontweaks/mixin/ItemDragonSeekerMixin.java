package com.chedd.dragontweaks.mixin;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import com.iafenvoy.iceandfire.item.ItemDragonSeeker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDragonSeeker.class)
public class ItemDragonSeekerMixin {

    private static final int SEARCH_RANGE = 500;
    private static final int MIN_DRAGON_STAGE = 4;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUse(Level level, Player player, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.isClientSide) return;

        ItemStack stack = player.getItemInHand(hand);
        Vec3 pos = player.position();
        AABB searchBox = new AABB(
            pos.x - SEARCH_RANGE, pos.y - SEARCH_RANGE, pos.z - SEARCH_RANGE,
            pos.x + SEARCH_RANGE, pos.y + SEARCH_RANGE, pos.z + SEARCH_RANGE
        );

        EntityDragonBase dragon = level.getNearestEntity(
            EntityDragonBase.class,
            TargetingConditions.forCombat().selector(entity -> {
                if (entity instanceof EntityDragonBase d) {
                    return d.getDragonStage() >= MIN_DRAGON_STAGE
                        && !d.isMobDead()
                        && !d.isTame();
                }
                return false;
            }),
            player,
            player.getX(), player.getY(), player.getZ(),
            searchBox
        );

        if (dragon == null) {
            player.sendSystemMessage(Component.translatable("item.iceandfire.dragon_seeker.not_found"));
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        int distance = (int) Math.round(player.position().distanceTo(dragon.position()));
        player.sendSystemMessage(
            Component.literal("Mother dragon detected ")
                .append(Component.literal(distance + " blocks away")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)))
        );

        cir.setReturnValue(InteractionResultHolder.success(stack));
    }
}
