package com.igormonasterio.samcats.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import com.igormonasterio.samcats.entity.goal.ChaseIvyGoal;

/** Naru: grey tabby. Spends her free time chasing Ivy around. */
public class NaruEntity extends UniqueCat {
    private boolean chasing;

    public NaruEntity(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(8, new ChaseIvyGoal(this));
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }
}
