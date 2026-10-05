package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.ai.goal.Goal;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/**
 * Lie down for a while, wherever. El Abuelo has seen it all and naps a lot; Noah rolls over for chin scratches.
 */
public class NapGoal extends Goal {
    private final UniqueCat cat;
    private final int chance;
    private final int minTicks;
    private final int maxTicks;
    private int ticksLeft;

    /** @param chance one in {@code chance} per check; the nap lasts between {@code minTicks} and {@code maxTicks}. */
    public NapGoal(UniqueCat cat, int chance, int minTicks, int maxTicks) {
        this.cat = cat;
        this.chance = chance;
        this.minTicks = minTicks;
        this.maxTicks = maxTicks;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return !cat.isOrderedToSit() && !cat.isLying() && cat.onGround() && !cat.isInWater() && cat.getTarget() == null
                && cat.getRandom().nextInt(reducedTickDelay(chance)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && cat.hurtTime == 0 && !cat.isOrderedToSit() && cat.getTarget() == null && !cat.isInWater();
    }

    @Override
    public void start() {
        ticksLeft = minTicks + cat.getRandom().nextInt(maxTicks - minTicks + 1);
        cat.getNavigation().stop();
        cat.setLying(true);
    }

    @Override
    public void tick() {
        ticksLeft--;
    }

    @Override
    public void stop() {
        cat.setLying(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
