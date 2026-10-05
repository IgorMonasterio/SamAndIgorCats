package com.igormonasterio.samcats.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import com.igormonasterio.samcats.ModRegistry;
import com.igormonasterio.samcats.entity.goal.HideInBoxGoal;
import com.igormonasterio.samcats.entity.goal.TeaseNaruGoal;

/**
 * Ivy: calico (white, black and brown). Runs for it whenever Naru comes after her, hides in a cardboard box
 * if there's one around, and sometimes starts the chase herself.
 */
public class IvyEntity extends UniqueCat {
    public IvyEntity(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new HideInBoxGoal(this));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, NaruEntity.class, 10.0F, 1.2D, 1.5D,
                entity -> entity instanceof NaruEntity naru && naru.isChasing()));
        this.goalSelector.addGoal(7, new TeaseNaruGoal(this));
    }

    /** Safe inside a cardboard box: Naru can't catch her there. */
    public boolean isHiddenInBox() {
        return level().getBlockState(blockPosition()).is(ModRegistry.CARDBOARD_BOX.get());
    }
}
