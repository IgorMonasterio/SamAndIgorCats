package com.igormonasterio.samcats.entity.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import com.igormonasterio.samcats.BoxStealth;

import java.util.EnumSet;

/** Any cat nearby, tame or not, can't resist a player hiding in a box: it comes and sits with you. */
public class JoinBoxGoal extends Goal {
    private static final double NOTICE_RANGE = 10.0D;

    private final Cat cat;
    private Player player;

    public JoinBoxGoal(Cat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit() || cat.isPassenger() || cat.tickCount % 10 != 0) return false;
        player = cat.level().getNearestPlayer(cat.getX(), cat.getY(), cat.getZ(), NOTICE_RANGE,
                p -> p instanceof Player pl && BoxStealth.isBoxed(pl));
        return player != null;
    }

    @Override
    public boolean canContinueToUse() {
        return player != null && player.isAlive() && BoxStealth.isBoxed(player)
                && !cat.isOrderedToSit() && cat.distanceToSqr(player) < 14 * 14;
    }

    @Override
    public void tick() {
        cat.getLookControl().setLookAt(player, 30.0F, 30.0F);
        if (cat.distanceToSqr(player) > 1.3D * 1.3D) {
            cat.setInSittingPose(false);
            if (cat.tickCount % 10 == 0) cat.getNavigation().moveTo(player, 1.1D);
        } else {
            cat.getNavigation().stop();
            cat.setInSittingPose(true);
        }
    }

    @Override
    public void stop() {
        player = null;
        cat.setInSittingPose(false);
        cat.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
