package com.igormonasterio.samcats.entity.personality;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import com.igormonasterio.samcats.entity.CatProfiles;
import com.igormonasterio.samcats.entity.UniqueCat;

/**
 * What makes each family cat itself. Naru and Ivy keep theirs in their own classes; the rest live here.
 * Goal priorities fit around the vanilla cat ones: 1 float/panic, 2 sit when ordered, 3 relax on owner, 4 tempt,
 * 5 lie on bed, 6 follow owner, 7 sit on block, 8 leap, 9 attack, 10 breed, 11 stroll, 12 look at player.
 */
public final class Personalities {
    private Personalities() {}

    /** Ten minutes of good mood after Oliver gets a stroke. */
    private static final int OLIVER_CALM_TICKS = 12000;
    /** Two minutes of Luck after stroking the Queen. */
    private static final int NARU_LUCK_TICKS = 2400;

    public static void addGoals(UniqueCat cat) {
        String id = cat.profile().id();
        if (CatProfiles.STRAYS.contains(id)) cat.goalSelector.addGoal(10, new FollowLeaderGoal(cat));
        switch (id) {
            case CatProfiles.BONZO -> cat.goalSelector.addGoal(4, new BegForFishGoal(cat));
            case CatProfiles.DOLORES, CatProfiles.MIA -> cat.goalSelector.addGoal(5, new StayCloseGoal(cat));
            case CatProfiles.ITLERINA -> cat.goalSelector.addGoal(3, new ShyGoal(cat));
            case CatProfiles.STRIPEY -> cat.goalSelector.addGoal(9, new AvoidEntityGoal<>(cat, Cat.class, 4.0F, 0.9D, 1.0D, e -> e != cat));
            case CatProfiles.LINCE -> {
                cat.goalSelector.addGoal(4, new BringGiftGoal(cat));
                cat.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(cat, Rabbit.class, 200, false, false, e -> !cat.isOrderedToSit()));
                cat.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(cat, Chicken.class, 600, false, false, e -> !cat.isOrderedToSit()));
            }
            case CatProfiles.EL_BEBE -> cat.goalSelector.addGoal(8, new PlayGoal(cat));
            case CatProfiles.EL_ABUELO -> cat.goalSelector.addGoal(9, new NapGoal(cat, 250, 600, 1800));
            case CatProfiles.NOAH -> cat.goalSelector.addGoal(9, new NapGoal(cat, 900, 200, 600));
            case CatProfiles.OLIVER -> cat.goalSelector.addGoal(3, new GrumpyGoal(cat));
            case CatProfiles.VALENTINO -> cat.goalSelector.addGoal(4, new PoseGoal(cat));
            case CatProfiles.KALESSI -> {
                // A princess doesn't sit on chests and furnaces like everyone else.
                cat.goalSelector.removeAllGoals(g -> g instanceof CatSitOnBlockGoal);
                cat.goalSelector.addGoal(7, new SitOnSoftGoal(cat, 0.9D));
            }
            case CatProfiles.CHEETO -> cat.goalSelector.addGoal(5, new CuriousGoal(cat));
            case CatProfiles.CALCETIN -> cat.goalSelector.addGoal(6, new StealGoal(cat));
            case CatProfiles.LA_TRICO -> cat.goalSelector.addGoal(6, new GreetGoal(cat));
            case CatProfiles.NUBE -> cat.goalSelector.addGoal(4, new RainShelterGoal(cat));
            default -> {}
        }
    }

    /** Extra reactions to a stroke (the hearts and the purr are already done). */
    public static void onPetted(UniqueCat cat, Player player) {
        switch (cat.profile().id()) {
            case CatProfiles.NOAH -> {
                if (cat.isLying()) {
                    cat.hearts(10);
                    cat.playSound(SoundEvents.CAT_PURREOW, 1.0F, cat.getVoicePitch());
                }
            }
            case CatProfiles.OLIVER -> cat.calmDown(OLIVER_CALM_TICKS);
            case CatProfiles.CALCETIN -> {
                if (!cat.stash().isEmpty()) {
                    cat.giveStashBack(player);
                    player.displayClientMessage(Component.translatable("samcats.msg.stash_returned", cat.getDisplayName()), true);
                }
            }
            case CatProfiles.NARU -> {
                if (cat.isTame()) {
                    cat.hearts(6);
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, NARU_LUCK_TICKS, 0));
                }
            }
            default -> {}
        }
    }

    public static void hearts(Entity entity, int count) {
        if (entity.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight() + 0.2D, entity.getZ(),
                    count, 0.3D, 0.2D, 0.3D, 0.0D);
        }
    }
}
