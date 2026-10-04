package com.igormonasterio.samcats.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;

/** Ivy: calico (white, black and brown). Runs for it whenever Naru comes after her. */
public class IvyEntity extends UniqueCat {
    public IvyEntity(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, NaruEntity.class, 10.0F, 1.2D, 1.5D,
                entity -> entity instanceof NaruEntity naru && naru.isChasing()));
    }
}
