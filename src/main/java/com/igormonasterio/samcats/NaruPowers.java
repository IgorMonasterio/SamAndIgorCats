package com.igormonasterio.samcats;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.entity.NaruEntity;

import java.util.List;
import java.util.function.Predicate;

/** The Queen's powers: nine lives for whoever she's near, a roar against monsters and cat's eyes for her owner. */
@Mod.EventBusSubscriber(modid = SamCats.MODID)
public final class NaruPowers {
    private NaruPowers() {}

    private static final double SAVE_RANGE = 16.0D;
    private static final double ROAR_TRIGGER_RANGE = 12.0D;
    private static final double ROAR_RANGE = 8.0D;
    private static final double NIGHT_VISION_RANGE = 16.0D;

    @Nullable
    private static NaruEntity nearbyNaru(Player player, double range, Predicate<NaruEntity> filter) {
        List<NaruEntity> narus = player.level().getEntitiesOfClass(NaruEntity.class, player.getBoundingBox().inflate(range),
                n -> n.isAlive() && n.isTame() && filter.test(n));
        return narus.isEmpty() ? null : narus.get(0);
    }

    /** The nine lives of the Queen: once a Minecraft day, a tame Naru nearby keeps you from dying. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || event.isCanceled()) return;
        // Nothing saves you from the void or /kill.
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        NaruEntity naru = nearbyNaru(player, SAVE_RANGE, NaruEntity::canSave);
        if (naru == null) return;

        event.setCanceled(true);
        naru.markSaved();
        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
        player.clearFire();
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY(1.0D), player.getZ(), 60, 0.5D, 0.8D, 0.5D, 0.3D);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, naru.getX(), naru.getY(0.5D), naru.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.2D);
        level.playSound(null, naru.blockPosition(), SoundEvents.CAT_PURREOW, SoundSource.NEUTRAL, 2.0F, 0.8F);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6F, 1.2F);
        player.sendSystemMessage(Component.translatable("samcats.msg.naru_saved", naru.getDisplayName()).withStyle(ChatFormatting.GOLD));
    }

    /** The Queen's roar: a monster hurts you near a tame Naru, she hisses and every monster around her backs off. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
                || !(event.getSource().getEntity() instanceof Enemy)) return;
        NaruEntity naru = nearbyNaru(player, ROAR_TRIGGER_RANGE, NaruEntity::canRoar);
        if (naru == null) return;

        naru.markRoared();
        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, naru.blockPosition(), SoundEvents.CAT_HISS, SoundSource.NEUTRAL, 3.0F, 0.5F);
        level.sendParticles(ParticleTypes.POOF, naru.getX(), naru.getY(0.5D), naru.getZ(), 30, 1.5D, 0.3D, 1.5D, 0.05D);
        Vec3 center = naru.position();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, naru.getBoundingBox().inflate(ROAR_RANGE),
                m -> m instanceof Enemy && m.isAlive() && BoxStealth.canBeFooled(m))) {
            mob.knockback(1.2D, center.x - mob.getX(), center.z - mob.getZ());
            mob.setTarget(null);
            if (mob instanceof PathfinderMob walker) {
                Vec3 away = DefaultRandomPos.getPosAway(walker, 16, 7, center);
                if (away != null) walker.getNavigation().moveTo(away.x, away.y, away.z, 1.4D);
            }
        }
    }

    /** Cat's eyes: with your tame Naru by your side, you see in the dark. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide() || player.tickCount % 40 != 0) return;
        if (nearbyNaru(player, NIGHT_VISION_RANGE, n -> n.isOwnedBy(player)) != null) {
            // Longer than 200 ticks so the effect doesn't flicker.
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
        }
    }
}
