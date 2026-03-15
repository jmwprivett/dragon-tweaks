package com.chedd.dragontweaks.mixin;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityDragonBase.class)
public abstract class EntityDragonBaseRoarMixin {

    @Redirect(
        method = "roar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            ordinal = 1
        ),
        remap = false,
        require = 0
    )
    private boolean skipWeaknessOnPlayersEpicRoarNamed(LivingEntity target, MobEffectInstance effect) {
        return this.applyRoarEffect(target, effect);
    }

    @Redirect(
        method = "roar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            ordinal = 3
        ),
        remap = false,
        require = 0
    )
    private boolean skipWeaknessOnPlayersNormalRoarNamed(LivingEntity target, MobEffectInstance effect) {
        return this.applyRoarEffect(target, effect);
    }

    @Redirect(
        method = "roar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;m_7292_(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            ordinal = 1
        ),
        remap = false,
        require = 0
    )
    private boolean skipWeaknessOnPlayersEpicRoarSrg(LivingEntity target, MobEffectInstance effect) {
        return this.applyRoarEffect(target, effect);
    }

    @Redirect(
        method = "roar",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;m_7292_(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            ordinal = 3
        ),
        remap = false,
        require = 0
    )
    private boolean skipWeaknessOnPlayersNormalRoarSrg(LivingEntity target, MobEffectInstance effect) {
        return this.applyRoarEffect(target, effect);
    }

    private boolean applyRoarEffect(LivingEntity target, MobEffectInstance effect) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this;
        if (dragon.isTame()) {
            if (target instanceof Player) {
                return false;
            }
            if (target instanceof EntityDragonBase targetDragon && targetDragon.isTame()) {
                return false;
            }
        }
        return target.addEffect(effect);
    }
}
