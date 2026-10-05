package com.igormonasterio.samcats.entity.personality;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/** Nube doesn't like getting wet: when it rains she looks for a roof. */
public class RainShelterGoal extends Goal {
    private final UniqueCat cat;
    private Vec3 shelter;

    public RainShelterGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit() || !cat.level().isRainingAt(cat.blockPosition())
                || cat.getRandom().nextInt(reducedTickDelay(20)) != 0) return false;
        shelter = findShelter();
        return shelter != null;
    }

    @Nullable
    private Vec3 findShelter() {
        Level level = cat.level();
        RandomSource random = cat.getRandom();
        BlockPos base = cat.blockPosition();
        for (int i = 0; i < 20; i++) {
            BlockPos pos = base.offset(random.nextInt(21) - 10, random.nextInt(7) - 3, random.nextInt(21) - 10);
            if (!level.canSeeSky(pos) && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                    && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                return Vec3.atBottomCenterOf(pos);
            }
        }
        return null;
    }

    @Override
    public boolean canContinueToUse() {
        return !cat.getNavigation().isDone() && !cat.isOrderedToSit();
    }

    @Override
    public void start() {
        cat.getNavigation().moveTo(shelter.x, shelter.y, shelter.z, 1.2D);
    }

    @Override
    public void stop() {
        shelter = null;
    }
}
