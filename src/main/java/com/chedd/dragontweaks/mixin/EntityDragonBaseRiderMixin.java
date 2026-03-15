package com.chedd.dragontweaks.mixin;

import com.chedd.dragontweaks.DragonBreathHelper;
import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityDragonBase.class)
public abstract class EntityDragonBaseRiderMixin {

    @Shadow(remap = false) public abstract boolean isFlying();
    @Shadow(remap = false) public abstract boolean isHovering();
    @Shadow(remap = false) public abstract void setFlying(boolean flying);
    @Shadow(remap = false) public abstract void setHovering(boolean hovering);
    @Shadow(remap = false) public abstract int getCommand();
    @Shadow(remap = false) public abstract void setCommand(int command);

    @Unique
    private Player dragontweaks$lastRider = null;

    @ModifyConstant(method = "updateRider", constant = @Constant(intValue = 20, ordinal = 0), remap = false)
    private int reduceTakeoffChargeTime(int original) {
        return DragonBreathHelper.RIDER_TAKEOFF_CHARGE_TICKS;
    }

    @Inject(method = "updateRider", at = @At("HEAD"), remap = false)
    private void onUpdateRiderHead(CallbackInfo ci) {
        EntityDragonBase self = (EntityDragonBase)(Object)this;
        LivingEntity passenger = self.getControllingPassenger();
        Player currentRider = passenger instanceof Player p ? p : null;

        // Rider was just removed (force dismount or normal dismount)
        if (dragontweaks$lastRider != null && currentRider == null) {
            // Fix 1: Clear crouch so other players don't see rider stuck crouching
            dragontweaks$lastRider.setShiftKeyDown(false);

            // Fix 2: Stop flying/hovering so dragon doesn't freeze in the air
            if (isFlying() || isHovering()) {
                setFlying(false);
                setHovering(false);
            }
        }

        dragontweaks$lastRider = currentRider;
    }

    // Skip wander (0) - only allow sit (1) and escort (2)
    @ModifyVariable(method = "setCommand", at = @At("HEAD"), argsOnly = true, remap = false)
    private int skipWanderCommand(int command) {
        EntityDragonBase self = (EntityDragonBase)(Object)this;
        return self.isTame() && command == 0 ? 1 : command;
    }
}
