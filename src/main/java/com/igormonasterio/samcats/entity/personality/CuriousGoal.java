package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/** Cheeto, the new friend: before he's tamed he tags along behind you, keeping a few blocks away. */
public class CuriousGoal extends Goal {
    private final UniqueCat cat;
    private Player player;
    private int ticksLeft;
    private int recalc;
    private long nextTime;

    public CuriousGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cat.isTame() || cat.isOrderedToSit() || cat.level().getGameTime() < nextTime
                || cat.getRandom().nextInt(reducedTickDelay(20)) != 0) return false;
        player = cat.level().getNearestPlayer(cat, 20.0D);
        return player != null && cat.distanceToSqr(player) > 7 * 7;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && player != null && player.isAlive() && !player.isSpectator() && !cat.isTame()
                && cat.distanceToSqr(player) < 24 * 24;
    }

    @Override
    public void start() {
        ticksLeft = 600;
        recalc = 0;
    }

    @Override
    public void tick() {
        ticksLeft--;
        cat.getLookControl().setLookAt(player, 30.0F, cat.getMaxHeadXRot());
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            double distance = cat.distanceToSqr(player);
            if (distance > 7 * 7) cat.getNavigation().moveTo(player, 0.8D);
            else if (distance < 5 * 5) cat.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        player = null;
        cat.getNavigation().stop();
        nextTime = cat.level().getGameTime() + 400 + cat.getRandom().nextInt(800);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
