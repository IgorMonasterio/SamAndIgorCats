package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/** Dolores and Mía are the cuddly ones: they never stray more than a couple of blocks from their owner. */
public class StayCloseGoal extends Goal {
    private final UniqueCat cat;
    private LivingEntity owner;
    private int recalc;

    public StayCloseGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!cat.isTame() || cat.isOrderedToSit() || cat.isLeashed()) return false;
        owner = cat.getOwner();
        return owner != null && !owner.isSpectator() && cat.distanceToSqr(owner) > 3 * 3 && cat.distanceToSqr(owner) < 16 * 16;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && owner.isAlive() && !cat.isOrderedToSit()
                && cat.distanceToSqr(owner) > 2 * 2 && cat.distanceToSqr(owner) < 16 * 16;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void tick() {
        cat.getLookControl().setLookAt(owner, 10.0F, cat.getMaxHeadXRot());
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            cat.getNavigation().moveTo(owner, 1.1D);
        }
    }

    @Override
    public void stop() {
        owner = null;
        cat.getNavigation().stop();
    }
}
