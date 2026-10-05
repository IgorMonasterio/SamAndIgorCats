package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.phys.Vec3;
import com.igormonasterio.samcats.entity.NaruEntity;

import java.util.EnumSet;
import java.util.List;

/** The Queen's court: when Naru sits down, the cats around her come and sit in a ring, looking at her. */
public class CourtGoal extends Goal {
    private static final double RANGE = 10.0D;
    private static final double RING = 2.5D;

    private final Cat cat;
    private NaruEntity queen;
    private double angle;
    private int recalc;

    public CourtGoal(Cat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    private boolean ownerNearby() {
        LivingEntity owner = cat.isTame() ? cat.getOwner() : null;
        return owner == null || cat.distanceToSqr(owner) < 16 * 16;
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit() || cat.isLeashed() || cat.isPassenger() || cat.getRandom().nextInt(reducedTickDelay(20)) != 0) return false;
        List<NaruEntity> queens = cat.level().getEntitiesOfClass(NaruEntity.class, cat.getBoundingBox().inflate(RANGE),
                n -> n != cat && n.isAlive() && n.isInSittingPose());
        queen = queens.isEmpty() ? null : queens.get(0);
        return queen != null && ownerNearby();
    }

    @Override
    public boolean canContinueToUse() {
        return queen != null && queen.isAlive() && queen.isInSittingPose() && !cat.isOrderedToSit()
                && cat.distanceToSqr(queen) < 14 * 14 && ownerNearby();
    }

    @Override
    public void start() {
        // Each cat keeps its own seat around the queen.
        angle = (cat.getUUID().hashCode() & 0xFFFF) / 65535.0D * Math.PI * 2.0D;
        recalc = 0;
    }

    @Override
    public void tick() {
        cat.getLookControl().setLookAt(queen, 30.0F, 30.0F);
        Vec3 seat = queen.position().add(Math.cos(angle) * RING, 0.0D, Math.sin(angle) * RING);
        if (cat.distanceToSqr(seat) > 1.2D * 1.2D) {
            cat.setInSittingPose(false);
            if (--recalc <= 0) {
                recalc = adjustedTickDelay(10);
                cat.getNavigation().moveTo(seat.x, seat.y, seat.z, 1.0D);
            }
        } else {
            cat.getNavigation().stop();
            cat.setInSittingPose(true);
        }
    }

    @Override
    public void stop() {
        if (!cat.isOrderedToSit()) cat.setInSittingPose(false);
        queen = null;
        cat.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
