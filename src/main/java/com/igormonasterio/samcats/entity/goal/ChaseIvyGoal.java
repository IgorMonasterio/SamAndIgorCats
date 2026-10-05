package com.igormonasterio.samcats.entity.goal;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import com.igormonasterio.samcats.entity.IvyEntity;
import com.igormonasterio.samcats.entity.NaruEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Every now and then Naru spots Ivy and goes after her (straight away if Ivy provoked her). Ivy flees
 * (see IvyEntity); if she makes it into a cardboard box, Naru can't catch her and gives up.
 */
public class ChaseIvyGoal extends Goal {
    private static final double SEARCH_RANGE = 16.0D;
    private static final int MAX_CHASE_TICKS = 200;
    private static final int GIVE_UP_TICKS = 60;

    private final NaruEntity naru;
    private IvyEntity ivy;
    private int ticksLeft;
    private long nextChaseTime;
    private boolean foiled;

    public ChaseIvyGoal(NaruEntity naru) {
        this.naru = naru;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (naru.isOrderedToSit() || naru.isInSittingPose()) return false;
        boolean provoked = naru.takeProvoked();
        if (!provoked && (naru.level().getGameTime() < nextChaseTime || naru.getRandom().nextInt(reducedTickDelay(60)) != 0)) return false;
        List<IvyEntity> ivies = naru.level().getEntitiesOfClass(IvyEntity.class,
                naru.getBoundingBox().inflate(SEARCH_RANGE), i -> i.isAlive() && !i.isOrderedToSit());
        ivy = ivies.isEmpty() ? null : ivies.get(0);
        return ivy != null;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && ivy != null && ivy.isAlive() && !naru.isOrderedToSit()
                && naru.distanceToSqr(ivy) < 24 * 24;
    }

    @Override
    public void start() {
        ticksLeft = MAX_CHASE_TICKS;
        foiled = false;
        naru.setChasing(true);
        naru.playSound(SoundEvents.CAT_PURREOW, 1.0F, 1.2F);
    }

    @Override
    public void tick() {
        ticksLeft--;
        naru.getLookControl().setLookAt(ivy, 30.0F, 30.0F);
        if (ivy.isHiddenInBox()) {
            // Safe in the box: Naru sits it out for a moment and then gives up.
            if (!foiled) {
                foiled = true;
                ticksLeft = Math.min(ticksLeft, GIVE_UP_TICKS);
            }
            if (naru.distanceToSqr(ivy) < 2.0D * 2.0D) naru.getNavigation().stop();
            else if (ticksLeft % 5 == 0) naru.getNavigation().moveTo(ivy, 1.0D);
            return;
        }
        if (ticksLeft % 5 == 0) naru.getNavigation().moveTo(ivy, 1.4D);
        if (naru.distanceToSqr(ivy) < 1.5D * 1.5D) {
            // Caught her! Ivy is not amused.
            ivy.playSound(SoundEvents.CAT_HISS, 1.0F, 1.0F);
            ticksLeft = 0;
        }
    }

    @Override
    public void stop() {
        if (foiled) naru.playSound(SoundEvents.CAT_AMBIENT, 1.0F, 0.8F);
        foiled = false;
        naru.setChasing(false);
        naru.getNavigation().stop();
        ivy = null;
        // Rest 30-90 seconds before the next round.
        nextChaseTime = naru.level().getGameTime() + 600 + naru.getRandom().nextInt(1200);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
