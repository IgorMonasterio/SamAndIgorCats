package com.igormonasterio.samcats.entity.personality;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;
import java.util.List;

/**
 * Calcetín, the sock thief: he grabs things lying on the floor and carries them off to his corner.
 * Stroke him (sneak + right click, empty hand) and he gives it all back.
 */
public class StealGoal extends Goal {
    private final UniqueCat cat;
    private ItemEntity loot;
    private boolean carrying;
    private int ticksLeft;
    private int recalc;

    public StealGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit() || cat.stash().size() >= UniqueCat.STASH_SIZE
                || cat.getRandom().nextInt(reducedTickDelay(60)) != 0) return false;
        // Only things that have been lying around for a while: not what someone just dropped.
        List<ItemEntity> items = cat.level().getEntitiesOfClass(ItemEntity.class, cat.getBoundingBox().inflate(10.0D),
                i -> i.isAlive() && i.getAge() > 100 && !i.hasPickUpDelay());
        loot = items.isEmpty() ? null : items.get(cat.getRandom().nextInt(items.size()));
        return loot != null;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && !cat.isOrderedToSit();
    }

    @Override
    public void start() {
        if (cat.stashHome() == null) cat.setStashHome(cat.blockPosition());
        carrying = false;
        ticksLeft = 400;
        recalc = 0;
    }

    @Override
    public void tick() {
        ticksLeft--;
        BlockPos home = cat.stashHome();
        if (!carrying) {
            if (loot == null || !loot.isAlive()) {
                ticksLeft = 0;
                return;
            }
            cat.getLookControl().setLookAt(loot, 30.0F, 30.0F);
            if (cat.distanceToSqr(loot) < 1.5D * 1.5D) {
                cat.stash().add(loot.getItem().copy());
                loot.discard();
                cat.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.6F);
                carrying = true;
                recalc = 0;
            } else if (--recalc <= 0) {
                recalc = adjustedTickDelay(10);
                cat.getNavigation().moveTo(loot, 1.1D);
            }
            return;
        }
        if (home == null || cat.blockPosition().closerThan(home, 2.0D)) {
            ticksLeft = 0;
        } else if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            cat.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 1.2D);
        }
    }

    @Override
    public void stop() {
        loot = null;
        carrying = false;
        cat.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
