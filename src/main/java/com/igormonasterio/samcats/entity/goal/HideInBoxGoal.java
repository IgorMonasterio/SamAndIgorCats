package com.igormonasterio.samcats.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.ModRegistry;
import com.igormonasterio.samcats.block.CardboardBoxBlock;
import com.igormonasterio.samcats.entity.IvyEntity;
import com.igormonasterio.samcats.entity.NaruEntity;

import java.util.EnumSet;

/** When Naru comes after her and there's a cardboard box nearby, Ivy dives in. Naru can't get her in there. */
public class HideInBoxGoal extends Goal {
    private static final int BOX_RANGE = 8;

    private final IvyEntity ivy;
    private BlockPos box;
    private int scanCooldown;
    private int recalc;

    public HideInBoxGoal(IvyEntity ivy) {
        this.ivy = ivy;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    private boolean naruChasing() {
        return !ivy.level().getEntitiesOfClass(NaruEntity.class, ivy.getBoundingBox().inflate(16.0D),
                n -> n.isAlive() && n.isChasing()).isEmpty();
    }

    private static boolean isFreeBox(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModRegistry.CARDBOARD_BOX.get()) && !state.getValue(CardboardBoxBlock.CLOSED) && level.isEmptyBlock(pos.above());
    }

    @Nullable
    private BlockPos findBox() {
        BlockPos center = ivy.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-BOX_RANGE, -2, -BOX_RANGE), center.offset(BOX_RANGE, 2, BOX_RANGE))) {
            if (!isFreeBox(ivy.level(), pos)) continue;
            double distance = pos.distSqr(center);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    @Override
    public boolean canUse() {
        if (ivy.isOrderedToSit() || --scanCooldown > 0) return false;
        scanCooldown = 10;
        if (!naruChasing()) return false;
        box = findBox();
        return box != null;
    }

    @Override
    public boolean canContinueToUse() {
        return box != null && isFreeBox(ivy.level(), box) && naruChasing() && !ivy.isOrderedToSit();
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void tick() {
        if (ivy.blockPosition().equals(box)) {
            ivy.getNavigation().stop();
            ivy.setInSittingPose(true);
        } else if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            ivy.getNavigation().moveTo(box.getX() + 0.5D, box.getY(), box.getZ() + 0.5D, 1.5D);
        }
    }

    @Override
    public void stop() {
        if (!ivy.isOrderedToSit()) ivy.setInSittingPose(false);
        box = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
