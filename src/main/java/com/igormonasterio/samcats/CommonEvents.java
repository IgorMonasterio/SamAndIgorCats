package com.igormonasterio.samcats;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.igormonasterio.samcats.entity.goal.JoinBoxGoal;
import com.igormonasterio.samcats.entity.goal.LaserChaseGoal;
import com.igormonasterio.samcats.entity.goal.SitInBoxGoal;
import com.igormonasterio.samcats.world.CatSpawner;

@Mod.EventBusSubscriber(modid = SamCats.MODID)
public final class CommonEvents {
    private CommonEvents() {}

    /** Teach every cat (vanilla ones too) about lasers and boxes. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Cat cat)) return;
        cat.goalSelector.addGoal(1, new LaserChaseGoal(cat));
        cat.goalSelector.addGoal(2, new JoinBoxGoal(cat));
        cat.goalSelector.addGoal(5, new SitInBoxGoal(cat, 0.9D));
    }

    /** Monsters can't pick a hidden box-player as a new target. */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player && BoxStealth.isHidden(player)
                && BoxStealth.canBeFooled(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** ...and the ones already chasing you lose track as soon as you hide. */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.tickCount % 10 != 0) return;
        if (entity instanceof Mob mob && mob.getTarget() instanceof Player player
                && BoxStealth.isHidden(player) && BoxStealth.canBeFooled(mob)) {
            mob.setTarget(null);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    /** Three or more cats in the box with you: their purring heals you. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide() || player.tickCount % 40 != 0) return;
        if (!BoxStealth.isBoxed(player)) return;
        int cats = player.level().getEntitiesOfClass(Cat.class, player.getBoundingBox().inflate(2.5D)).size();
        if (cats >= 3) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) CatSpawner.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LaserTracker.clear(event.getEntity().getUUID());
    }
}
