package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.ai.goal.Goal;
import com.igormonasterio.samcats.entity.CatProfiles;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;
import java.util.List;

/** Batman is the boss of the street cats: while nobody has tamed them, they trail after her. */
public class FollowLeaderGoal extends Goal {
    private static final double SEARCH_RANGE = 32.0D;

    private final UniqueCat cat;
    private UniqueCat leader;
    private int recalc;

    public FollowLeaderGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cat.isTame() || cat.isOrderedToSit() || cat.isLeashed() || cat.getRandom().nextInt(reducedTickDelay(20)) != 0) return false;
        List<UniqueCat> bosses = cat.level().getEntitiesOfClass(UniqueCat.class, cat.getBoundingBox().inflate(SEARCH_RANGE),
                c -> c.is(CatProfiles.BATMAN) && c.isAlive());
        leader = bosses.isEmpty() ? null : bosses.get(0);
        return leader != null && cat.distanceToSqr(leader) > 6 * 6;
    }

    @Override
    public boolean canContinueToUse() {
        return leader != null && leader.isAlive() && !cat.isTame() && !cat.isOrderedToSit()
                && cat.distanceToSqr(leader) > 3 * 3 && cat.distanceToSqr(leader) < 40 * 40;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void tick() {
        cat.getLookControl().setLookAt(leader, 10.0F, cat.getMaxHeadXRot());
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            cat.getNavigation().moveTo(leader, 1.0D);
        }
    }

    @Override
    public void stop() {
        leader = null;
        cat.getNavigation().stop();
    }
}
