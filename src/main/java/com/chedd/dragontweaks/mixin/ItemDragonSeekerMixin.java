package com.chedd.dragontweaks.mixin;

import com.iafenvoy.iceandfire.item.ItemDragonSeeker;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ItemDragonSeeker.class)
public class ItemDragonSeekerMixin {

    @Unique
    private static final int MAX_RANGE = 1000;

    @Unique
    private static final long COOLDOWN_MS = 15_000L;

    @Unique
    private static final List<ResourceKey<Structure>> DRAGON_STRUCTURES = List.of(
        ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("iceandfire", "fire_dragon_cave")),
        ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("iceandfire", "ice_dragon_cave")),
        ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("iceandfire", "lightning_dragon_cave")),
        ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("iceandfire", "fire_dragon_roost")),
        ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("iceandfire", "ice_dragon_roost")),
        ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("iceandfire", "lightning_dragon_roost"))
    );

    @Unique
    private static final Map<UUID, Long> dragontweaks$lastUseTime = new HashMap<>();

    @Unique
    private static final Map<UUID, BlockPos> dragontweaks$cachedPos = new HashMap<>();

    @Inject(method = {"use", "m_7203_"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void onUse(Level level, Player player, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.isClientSide) return;

        ItemStack stack = player.getItemInHand(hand);
        UUID playerId = player.getUUID();

        // Check cooldown and use cached position if available
        long now = System.currentTimeMillis();
        Long lastUse = dragontweaks$lastUseTime.get(playerId);
        boolean onCooldown = lastUse != null && (now - lastUse) < COOLDOWN_MS;

        BlockPos cached = dragontweaks$cachedPos.get(playerId);

        // If on cooldown, show cached distance or recharging message
        if (onCooldown) {
            if (cached != null) {
                double dist = Math.sqrt(player.blockPosition().distSqr(cached));
                if (dist <= MAX_RANGE) {
                    dragontweaks$sendFoundMessage(player, cached);
                    cir.setReturnValue(InteractionResultHolder.success(stack));
                    return;
                }
                dragontweaks$cachedPos.remove(playerId);
            }
            long remaining = (COOLDOWN_MS - (now - lastUse)) / 1000;
            player.sendSystemMessage(Component.literal("Staff recharging... " + remaining + "s")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        // Not on cooldown - check cache before doing expensive search
        if (cached != null) {
            double dist = Math.sqrt(player.blockPosition().distSqr(cached));
            if (dist <= MAX_RANGE) {
                dragontweaks$lastUseTime.put(playerId, now);
                dragontweaks$sendFoundMessage(player, cached);
                cir.setReturnValue(InteractionResultHolder.success(stack));
                return;
            }
            dragontweaks$cachedPos.remove(playerId);
        }

        // Search for nearest dragon structure
        ServerLevel serverLevel = (ServerLevel) level;
        Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);

        List<Holder<Structure>> holders = new ArrayList<>();
        for (ResourceKey<Structure> key : DRAGON_STRUCTURES) {
            Optional<Holder.Reference<Structure>> holder = structureRegistry.getHolder(key);
            holder.ifPresent(holders::add);
        }

        if (holders.isEmpty()) {
            player.sendSystemMessage(Component.literal("No dragon structures registered")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        dragontweaks$lastUseTime.put(playerId, now);

        HolderSet<Structure> holderSet = HolderSet.direct(holders);
        BlockPos playerPos = player.blockPosition();

        Pair<BlockPos, Holder<Structure>> result = serverLevel.getChunkSource().getGenerator()
            .findNearestMapStructure(serverLevel, holderSet, playerPos, MAX_RANGE / 16, false);

        if (result == null) {
            player.sendSystemMessage(Component.literal("No dragon lair detected within range")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        BlockPos foundPos = result.getFirst();
        double distance = Math.sqrt(playerPos.distSqr(foundPos));

        if (distance > MAX_RANGE) {
            player.sendSystemMessage(Component.literal("No dragon lair detected within range")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        // Cache the result
        dragontweaks$cachedPos.put(playerId, foundPos);
        dragontweaks$sendFoundMessage(player, foundPos);
        cir.setReturnValue(InteractionResultHolder.success(stack));
    }

    @Unique
    private static void dragontweaks$sendFoundMessage(Player player, BlockPos pos) {
        int dist = (int) Math.round(Math.sqrt(player.blockPosition().distSqr(pos)));
        player.sendSystemMessage(
            Component.literal("Dragon lair detected ")
                .append(Component.literal(dist + " blocks away")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)))
        );
    }
}
