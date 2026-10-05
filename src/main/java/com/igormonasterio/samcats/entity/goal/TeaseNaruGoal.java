package com.igormonasterio.samcats.entity.goal;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import com.igormonasterio.samcats.entity.IvyEntity;
import com.igormonasterio.samcats.entity.NaruEntity;

import java.util.EnumSet;
import java.util.List;

/** Ivy isn't so innocent: now and then she walks up to Naru, gives her a tap and runs for it. */
public class TeaseNaruGoal extends Goal {
    private final IvyEntity ivy;
    private NaruEntity naru;
    private int ticksLeft;
    private int recalc;
    private long nextTease;

    public TeaseNaruGoal(IvyEntity ivy) {
        this.ivy = ivy;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (ivy.isOrderedToSit() || ivy.level().getGameTime() < nextTease
                || ivy.getRandom().nextInt(reducedTickDelay(100)) != 0) return false;
        List<NaruEntity> narus = ivy.level().getEntitiesOfClass(NaruEntity.class, ivy.getBoundingBox().inflate(16.0D),
                n -> n.isAlive() && !n.isChasing() && !n.isOrderedToSit());
        naru = narus.isEmpty() ? null : narus.get(0);
        return naru != null;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && naru != null && naru.isAlive() && !naru.isChasing() && !ivy.isOrderedToSit();
    }

    @Override
    public void start() {
        ticksLeft = 200;
        recalc = 0;
    }

    @Override
    public void tick() {
        ticksLeft--;
        ivy.getLookControl().setLookAt(naru, 30.0F, 30.0F);
        if (ivy.distanceToSqr(naru) < 1.6D * 1.6D) {
            // Tap! ...and Naru takes the bait.
            ivy.playSound(SoundEvents.CAT_AMBIENT, 1.0F, 1.4F);
            naru.provoke();
            ticksLeft = 0;
            return;
        }
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            ivy.getNavigation().moveTo(naru, 1.2D);
        }
    }

    @Override
    public void stop() {
        naru = null;
        ivy.getNavigation().stop();
        nextTease = ivy.level().getGameTime() + 1200 + ivy.getRandom().nextInt(2400);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
