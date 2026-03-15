package com.chedd.dragontweaks;

import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import com.iafenvoy.iceandfire.entity.EntityDragonFireCharge;
import com.iafenvoy.iceandfire.entity.EntityDragonIceCharge;
import com.iafenvoy.iceandfire.entity.EntityDragonLightningCharge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class DragonBreathHelper {

    public static final double BEAM_RADIUS = 3.0;
    public static final int RIDER_BEAM_ACTIVE_TICKS = 25;
    public static final int RIDER_TAKEOFF_CHARGE_TICKS = 1;
    private static final int RIDER_BEAM_FULL_PROGRESS = 40;
    private static final float RIDER_FIRE_BREATH_FULL_PROGRESS = 10.0F;
    private static final int RIDER_FIRE_STOP_TICKS = 10;
    private static final double RIDER_BEAM_MIN_VISIBLE_DISTANCE = 2.0;

    private static SoundEvent fireBreathSound;
    private static SoundEvent iceBreathSound;
    private static SoundEvent lightningBreathSound;
    private static SoundEvent lightningCrackleSound;

    public static SoundEvent getFireBreathSound() {
        if (fireBreathSound == null)
            fireBreathSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("iceandfire", "firedragon_breath"));
        return fireBreathSound;
    }

    public static SoundEvent getIceBreathSound() {
        if (iceBreathSound == null)
            iceBreathSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("iceandfire", "icedragon_breath"));
        return iceBreathSound;
    }

    public static SoundEvent getLightningBreathSound() {
        if (lightningBreathSound == null)
            lightningBreathSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("iceandfire", "lightningdragon_breath"));
        return lightningBreathSound;
    }

    public static SoundEvent getLightningCrackleSound() {
        if (lightningCrackleSound == null)
            lightningCrackleSound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("iceandfire", "lightningdragon_breath_crackle"));
        return lightningCrackleSound;
    }

    public static void primeRiderBeam(EntityDragonBase dragon) {
        if (!dragon.isBreathingFire()) {
            dragon.setBreathingFire(true);
        }

        // Rider breath visuals key off vanilla warmup/progress fields, not just the
        // breathing flag. Keep them in the active range so the beam and mouth stay synced.
        if (dragon.fireStopTicks < RIDER_FIRE_STOP_TICKS) {
            dragon.fireStopTicks = RIDER_FIRE_STOP_TICKS;
        }
        if (dragon.fireBreathTicks < RIDER_BEAM_ACTIVE_TICKS) {
            dragon.fireBreathTicks = RIDER_BEAM_ACTIVE_TICKS;
        }
        if (dragon.burnProgress < RIDER_BEAM_FULL_PROGRESS) {
            dragon.burnProgress = RIDER_BEAM_FULL_PROGRESS;
        }
        if (dragon.fireBreathProgress < RIDER_FIRE_BREATH_FULL_PROGRESS) {
            dragon.fireBreathProgress = RIDER_FIRE_BREATH_FULL_PROGRESS;
        }
        if (dragon.prevFireBreathProgress < RIDER_FIRE_BREATH_FULL_PROGRESS) {
            dragon.prevFireBreathProgress = RIDER_FIRE_BREATH_FULL_PROGRESS;
        }
    }

    public static Vec3 resolveRiderBeamTarget(EntityDragonBase dragon, Entity controller, Vec3 headPos, double maxDistance) {
        Vec3 fallback = headPos.add(controller.getLookAngle().scale(maxDistance));
        HitResult hit = dragon.rayTraceRider(controller, maxDistance, 1.0F);
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return fallback;
        }

        Vec3 hitPos = hit.getLocation();
        if (hitPos.distanceToSqr(headPos) < RIDER_BEAM_MIN_VISIBLE_DISTANCE * RIDER_BEAM_MIN_VISIBLE_DISTANCE) {
            return fallback;
        }
        return hitPos;
    }

    @SuppressWarnings("unchecked")
    public static void shootFireball(EntityDragonBase dragon, Entity controller, Vec3 headVec) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("iceandfire", "fire_dragon_charge"));
        Vec3 look = controller.getLookAngle();
        double d2 = look.x + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        double d3 = look.y + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        double d4 = look.z + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        EntityDragonFireCharge charge = new EntityDragonFireCharge(
                (EntityType<? extends net.minecraft.world.entity.projectile.Fireball>) type,
                (Level) dragon.level(), dragon, d2, d3, d4);
        charge.setPos(headVec.x, headVec.y, headVec.z);
        Level level = (Level) dragon.level();
        if (!level.isClientSide) {
            level.addFreshEntity(charge);
        }
    }

    @SuppressWarnings("unchecked")
    public static void shootIceball(EntityDragonBase dragon, Entity controller, Vec3 headVec) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("iceandfire", "ice_dragon_charge"));
        Vec3 look = controller.getLookAngle();
        double d2 = look.x + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        double d3 = look.y + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        double d4 = look.z + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        EntityDragonIceCharge charge = new EntityDragonIceCharge(
                (EntityType<? extends net.minecraft.world.entity.projectile.Fireball>) type,
                (Level) dragon.level(), dragon, d2, d3, d4);
        charge.setPos(headVec.x, headVec.y, headVec.z);
        Level level = (Level) dragon.level();
        if (!level.isClientSide) {
            level.addFreshEntity(charge);
        }
    }

    @SuppressWarnings("unchecked")
    public static void shootLightningball(EntityDragonBase dragon, Entity controller, Vec3 headVec) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("iceandfire", "lightning_dragon_charge"));
        Vec3 look = controller.getLookAngle();
        double d2 = look.x + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        double d3 = look.y + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        double d4 = look.z + dragon.getRandom().nextGaussian() * 0.007499999832361937D;
        EntityDragonLightningCharge charge = new EntityDragonLightningCharge(
                (EntityType<? extends net.minecraft.world.entity.projectile.Fireball>) type,
                (Level) dragon.level(), dragon, d2, d3, d4);
        charge.setPos(headVec.x, headVec.y, headVec.z);
        Level level = (Level) dragon.level();
        if (!level.isClientSide) {
            level.addFreshEntity(charge);
        }
    }

    public static void damageEntitiesAlongBeam(EntityDragonBase dragon, Vec3 start, Vec3 end, float damage) {
        Level level = (Level) dragon.level();
        if (level.isClientSide) return;

        double minX = Math.min(start.x, end.x) - BEAM_RADIUS;
        double minY = Math.min(start.y, end.y) - BEAM_RADIUS;
        double minZ = Math.min(start.z, end.z) - BEAM_RADIUS;
        double maxX = Math.max(start.x, end.x) + BEAM_RADIUS;
        double maxY = Math.max(start.y, end.y) + BEAM_RADIUS;
        double maxZ = Math.max(start.z, end.z) + BEAM_RADIUS;
        AABB searchBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        List<LivingEntity> entities = level.getEntitiesOfClass(
            LivingEntity.class, searchBox,
            e -> e != dragon && !e.is(dragon.getControllingPassenger()) && e.isAlive()
        );

        for (LivingEntity entity : entities) {
            AABB expandedBox = entity.getBoundingBox().inflate(BEAM_RADIUS);
            if (expandedBox.contains(start) || expandedBox.contains(end) || expandedBox.clip(start, end).isPresent()) {
                entity.hurt(dragon.damageSources().mobAttack(dragon), damage);
            }
        }
    }
}
