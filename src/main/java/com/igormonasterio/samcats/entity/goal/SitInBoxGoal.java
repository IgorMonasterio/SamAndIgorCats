package com.igormonasterio.samcats.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import com.igormonasterio.samcats.ModRegistry;
import com.igormonasterio.samcats.block.CardboardBoxBlock;

/** If it fits, I sits: cats walk into a placed cardboard box and sit in it for a while. */
public class SitInBoxGoal extends MoveToBlockGoal {
    private final Cat cat;

    public SitInBoxGoal(Cat cat, double speed) {
        super(cat, speed, 8);
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

    // Walk into the box itself, not on top of it.
    @Override
    protected BlockPos getMoveToTarget() {
        return blockPos;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModRegistry.CARDBOARD_BOX.get()) && !state.getValue(CardboardBoxBlock.CLOSED)
                && level.isEmptyBlock(pos.above());
    }
}
