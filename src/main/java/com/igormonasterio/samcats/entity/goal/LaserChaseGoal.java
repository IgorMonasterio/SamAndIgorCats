package com.igormonasterio.samcats.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.phys.Vec3;
import com.igormonasterio.samcats.LaserTracker;

import java.util.EnumSet;

/** Tamed cats drop everything to chase a laser dot, pouncing when they get close. */
public class LaserChaseGoal extends Goal {
    private static final double NOTICE_RANGE = 24.0D;
    private static final double KEEP_RANGE = 28.0D;

    private final Cat cat;
    private Vec3 dot;

    public LaserChaseGoal(Cat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!cat.isTame() || cat.isOrderedToSit() || cat.isPassenger()) return false;
        dot = LaserTracker.nearest(cat.level(), cat.position(), NOTICE_RANGE, 3);
        return dot != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !cat.isOrderedToSit() && LaserTracker.nearest(cat.level(), cat.position(), KEEP_RANGE, 10) != null;
    }

    @Override
    public void start() {
        cat.setInSittingPose(false);
        cat.setLying(false);
    }

    @Override
    public void tick() {
        Vec3 latest = LaserTracker.nearest(cat.level(), cat.position(), KEEP_RANGE, 10);
        if (latest != null) dot = latest;
        cat.getLookControl().setLookAt(dot.x, dot.y, dot.z, 30.0F, 30.0F);
        if (cat.tickCount % 4 == 0) cat.getNavigation().moveTo(dot.x, dot.y, dot.z, 1.5D);

        double dx = dot.x - cat.getX();
        double dz = dot.z - cat.getZ();
        if (dx * dx + dz * dz < 2.0D && cat.onGround() && cat.getRandom().nextInt(12) == 0) {
            cat.setDeltaMovement(dx * 0.25D, 0.42D, dz * 0.25D); // pounce!
        }
    }

    @Override
    public void stop() {
        dot = null;
        cat.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
