package com.igormonasterio.samcats.entity.personality;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/** Lince, the wild one: after a successful hunt he brings a present to his owner. */
public class BringGiftGoal extends Goal {
    private final UniqueCat cat;
    private LivingEntity owner;
    private int recalc;

    public BringGiftGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cat.gift().isEmpty() || !cat.isTame() || cat.isOrderedToSit()) return false;
        owner = cat.getOwner();
        return owner != null && cat.distanceToSqr(owner) < 32 * 32;
    }

    @Override
    public boolean canContinueToUse() {
        return !cat.gift().isEmpty() && owner != null && owner.isAlive() && !cat.isOrderedToSit()
                && cat.distanceToSqr(owner) < 32 * 32;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void tick() {
        cat.getLookControl().setLookAt(owner, 10.0F, cat.getMaxHeadXRot());
        if (cat.distanceToSqr(owner) < 2.2D * 2.2D) {
            cat.spawnAtLocation(cat.gift());
            cat.setGift(ItemStack.EMPTY);
            cat.playSound(SoundEvents.CAT_PURREOW, 1.0F, cat.getVoicePitch());
            return;
        }
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
