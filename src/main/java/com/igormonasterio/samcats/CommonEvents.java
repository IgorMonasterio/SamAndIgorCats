package com.igormonasterio.samcats;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.igormonasterio.samcats.entity.CatProfiles;
import com.igormonasterio.samcats.entity.UniqueCat;
import com.igormonasterio.samcats.entity.goal.JoinBoxGoal;
import com.igormonasterio.samcats.entity.goal.LaserChaseGoal;
import com.igormonasterio.samcats.entity.goal.SitInBoxGoal;
import com.igormonasterio.samcats.entity.personality.CourtGoal;
import com.igormonasterio.samcats.world.CatSpawner;

@Mod.EventBusSubscriber(modid = SamCats.MODID)
public final class CommonEvents {
    private CommonEvents() {}

    /** Teach every cat (vanilla ones too) about lasers, boxes, Bonzo and the Queen's court. */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Cat cat)) return;
        cat.goalSelector.addGoal(1, new LaserChaseGoal(cat));
        cat.goalSelector.addGoal(2, new JoinBoxGoal(cat));
        // Kalessi only sits on beds and carpets.
        if (!CatProfiles.is(cat, CatProfiles.KALESSI)) cat.goalSelector.addGoal(5, new SitInBoxGoal(cat, 0.9D));
        // The neighbourhood tough guy: everyone steps aside when Bonzo walks by.
        if (!CatProfiles.is(cat, CatProfiles.BONZO)) {
            cat.goalSelector.addGoal(7, new AvoidEntityGoal<>(cat, UniqueCat.class, 2.5F, 1.0D, 1.1D,
                    e -> CatProfiles.is(e, CatProfiles.BONZO)));
        }
        // Everybody joins the Queen's court... except Bonzo (above queens) and Stripey (a loner).
        if (!CatProfiles.is(cat, CatProfiles.NARU) && !CatProfiles.is(cat, CatProfiles.BONZO) && !CatProfiles.is(cat, CatProfiles.STRIPEY)) {
            cat.goalSelector.addGoal(5, new CourtGoal(cat));
        }
    }

    /** Sneak + right click with an empty hand strokes a family cat. And El gato sin nombre won't take a name tag. */
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof UniqueCat cat)) return;
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        boolean client = event.getLevel().isClientSide();

        if (cat.is(CatProfiles.SIN_NOMBRE) && held.is(Items.NAME_TAG) && held.hasCustomHoverName()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(client));
            if (!client) {
                cat.playSound(SoundEvents.CAT_HISS, 1.0F, cat.getVoicePitch());
                player.displayClientMessage(Component.translatable("samcats.msg.no_name", cat.getDisplayName()), true);
            }
            return;
        }
        if (event.getHand() == InteractionHand.MAIN_HAND && player.isShiftKeyDown() && held.isEmpty()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(client));
            if (!client) cat.onPetted(player);
        }
    }

    /** Lince keeps a present from his catch for his owner. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity prey = event.getEntity();
        if (prey.level().isClientSide() || !(event.getSource().getEntity() instanceof UniqueCat cat)
                || !cat.is(CatProfiles.LINCE) || !cat.isTame() || !cat.gift().isEmpty()) return;
        if (prey instanceof Rabbit) {
            cat.setGift(new ItemStack(cat.getRandom().nextInt(7) == 0 ? Items.RABBIT_FOOT : Items.RABBIT_HIDE));
        } else if (prey instanceof Chicken) {
            cat.setGift(new ItemStack(Items.FEATHER));
        }
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

    /**
     * Purring heals: three or more cats in the box with you, or Dolores or Mía (tame, yours) right by your side
     * while you're hurt.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide() || player.tickCount % 40 != 0) return;
        if (BoxStealth.isBoxed(player)) {
            int cats = player.level().getEntitiesOfClass(Cat.class, player.getBoundingBox().inflate(2.5D)).size();
            if (cats >= 3) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
        }
        if (player.getHealth() < player.getMaxHealth()) {
            for (UniqueCat cat : player.level().getEntitiesOfClass(UniqueCat.class, player.getBoundingBox().inflate(4.0D),
                    c -> (c.is(CatProfiles.DOLORES) || c.is(CatProfiles.MIA)) && c.isTame() && c.isOwnedBy(player))) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
                cat.hearts(1);
            }
        }
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
