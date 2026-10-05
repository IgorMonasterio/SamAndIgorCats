package com.igormonasterio.samcats.entity.personality;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import com.igormonasterio.samcats.entity.UniqueCat;

/** Kalessi is a princess: she only sits on beds and carpets. */
public class SitOnSoftGoal extends MoveToBlockGoal {
    private final UniqueCat cat;

    public SitOnSoftGoal(UniqueCat cat, double speed) {
        super(cat, speed, 12);
        this.cat = cat;
    }

    @Override
    public boolean canUse() {
        return !cat.isOrderedToSit() && !cat.isPassenger() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        cat.setInSittingPose(false);
    }

    @Override
    public void stop() {
        super.stop();
        cat.setInSittingPose(false);
    }

    @Override
    public void tick() {
        super.tick();
        cat.setInSittingPose(isReachedTarget());
    }

    // A carpet is walked onto (its block is the floor); a bed is sat on top of.
    @Override
    protected BlockPos getMoveToTarget() {
        return cat.level().getBlockState(blockPos).is(BlockTags.WOOL_CARPETS) ? blockPos : blockPos.above();
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return (state.is(BlockTags.WOOL_CARPETS) || state.is(BlockTags.BEDS)) && level.isEmptyBlock(pos.above());
    }
}
