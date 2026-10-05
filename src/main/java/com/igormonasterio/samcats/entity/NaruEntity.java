package com.igormonasterio.samcats.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import com.igormonasterio.samcats.entity.goal.ChaseIvyGoal;

/**
 * Naru: grey tabby, the Queen cat. Spends her free time chasing Ivy around. She wears a crown, holds court,
 * gives night vision to her owner, roars at monsters that hurt you and, once a day, saves you from dying
 * (see {@link com.igormonasterio.samcats.NaruPowers}).
 */
public class NaruEntity extends UniqueCat {
    /** Once per Minecraft day. */
    public static final long SAVE_COOLDOWN = 24000L;
    /** 30 seconds between roars. */
    public static final long ROAR_COOLDOWN = 600L;

    private boolean chasing;
    private boolean provoked;
    private long lastSave = -SAVE_COOLDOWN;
    private long lastRoar = -ROAR_COOLDOWN;

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

    /** Ivy tapped her: the next chase starts right away. */
    public void provoke() {
        this.provoked = true;
    }

    public boolean takeProvoked() {
        boolean was = provoked;
        provoked = false;
        return was;
    }

    public boolean canSave() {
        return level().getGameTime() - lastSave >= SAVE_COOLDOWN;
    }

    public void markSaved() {
        lastSave = level().getGameTime();
    }

    public boolean canRoar() {
        return level().getGameTime() - lastRoar >= ROAR_COOLDOWN;
    }

    public void markRoared() {
        lastRoar = level().getGameTime();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("SamcatsLastSave", lastSave);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SamcatsLastSave")) lastSave = tag.getLong("SamcatsLastSave");
    }
}
