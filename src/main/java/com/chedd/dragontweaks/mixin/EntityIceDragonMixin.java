package com.chedd.dragontweaks.mixin;

import com.chedd.dragontweaks.DragonBreathHelper;
import com.iafenvoy.iceandfire.entity.EntityIceDragon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityIceDragon.class)
public class EntityIceDragonMixin {

    @Inject(method = "riderShootFire", at = @At("HEAD"), cancellable = true, remap = false)
    private void overrideRiderShootFire(Entity controller, CallbackInfo ci) {
        EntityIceDragon self = (EntityIceDragon) (Object) this;
        ci.cancel();

        DragonBreathHelper.primeRiderBeam(self);
        self.setYRot(self.yBodyRot);
        if (self.tickCount % 5 == 0) {
            self.playSound(DragonBreathHelper.getIceBreathSound(), 4, 1);
        }

        Vec3 headPos = self.getHeadPosition();
        double maxDistance = 10.0 * self.getDragonStage();
        Vec3 hitPos = DragonBreathHelper.resolveRiderBeamTarget(self, controller, headPos, maxDistance);

        self.stimulateFire(hitPos.x, hitPos.y, hitPos.z, 1);

        float damage = self.getDragonStage() * 2.0F;
        DragonBreathHelper.damageEntitiesAlongBeam(self, headPos, hitPos, damage);
    }
}
