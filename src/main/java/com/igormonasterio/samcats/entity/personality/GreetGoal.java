package com.igormonasterio.samcats.entity.personality;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;
import java.util.List;

/**
 * La Trico, the neighbourhood gossip: she goes to welcome every cat that turns up in the world,
 * and in between she drops in on the others, one by one.
 */
public class GreetGoal extends Goal {
    private final UniqueCat cat;
    private Cat target;
    private int ticksLeft;
    private int recalc;
    private long nextVisit;

    public GreetGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit()) return false;
        Cat newcomer = cat.takeGreetTarget();
        if (newcomer != null && newcomer.isAlive() && cat.distanceToSqr(newcomer) < 64 * 64) {
            target = newcomer;
            return true;
        }
        if (cat.level().getGameTime() < nextVisit || cat.getRandom().nextInt(reducedTickDelay(100)) != 0) return false;
        List<Cat> neighbours = cat.level().getEntitiesOfClass(Cat.class, cat.getBoundingBox().inflate(24.0D),
                c -> c != cat && c.isAlive());
        target = neighbours.isEmpty() ? null : neighbours.get(cat.getRandom().nextInt(neighbours.size()));
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && target != null && target.isAlive() && !cat.isOrderedToSit();
    }

    @Override
    public void start() {
        ticksLeft = 600;
        recalc = 0;
    }

    @Override
    public void tick() {
        ticksLeft--;
        cat.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (cat.distanceToSqr(target) < 1.8D * 1.8D) {
            Personalities.hearts(cat, 3);
            Personalities.hearts(target, 3);
            cat.playSound(SoundEvents.CAT_PURREOW, 1.0F, cat.getVoicePitch());
            ticksLeft = 0;
            return;
        }
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            cat.getNavigation().moveTo(target, 1.1D);
        }
    }

    @Override
    public void stop() {
        target = null;
        cat.getNavigation().stop();
        nextVisit = cat.level().getGameTime() + 1200 + cat.getRandom().nextInt(1200);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
