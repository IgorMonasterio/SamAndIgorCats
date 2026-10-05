package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;
import java.util.List;

/** El Bebé, the youngest of the gang: every now and then he chases whatever moves (or lies on the floor) and pounces. */
public class PlayGoal extends Goal {
    private final UniqueCat cat;
    private Entity target;
    private int ticksLeft;
    private int recalc;
    private long nextPlay;

    public PlayGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit() || cat.isLying() || cat.level().getGameTime() < nextPlay
                || cat.getRandom().nextInt(reducedTickDelay(40)) != 0) return false;
        List<ItemEntity> items = cat.level().getEntitiesOfClass(ItemEntity.class, cat.getBoundingBox().inflate(8.0D), Entity::isAlive);
        if (!items.isEmpty()) {
            target = items.get(cat.getRandom().nextInt(items.size()));
            return true;
        }
        List<Cat> friends = cat.level().getEntitiesOfClass(Cat.class, cat.getBoundingBox().inflate(10.0D),
                c -> c != cat && c.isAlive());
        target = friends.isEmpty() ? null : friends.get(cat.getRandom().nextInt(friends.size()));
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && target != null && target.isAlive() && !cat.isOrderedToSit();
    }

    @Override
    public void start() {
        ticksLeft = 120;
        recalc = 0;
    }

    @Override
    public void tick() {
        ticksLeft--;
        cat.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (cat.distanceToSqr(target) < 1.6D * 1.6D) {
            if (cat.onGround()) {
                // Pounce!
                double dx = target.getX() - cat.getX();
                double dz = target.getZ() - cat.getZ();
                cat.setDeltaMovement(dx * 0.3D, 0.42D, dz * 0.3D);
            }
            ticksLeft = 0;
            return;
        }
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(5);
            cat.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.3D);
        }
    }

    @Override
    public void stop() {
        target = null;
        cat.getNavigation().stop();
        nextPlay = cat.level().getGameTime() + 400 + cat.getRandom().nextInt(400);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
