package com.igormonasterio.samcats.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.world.CatSpawner;

/**
 * One of the family cats: a named cat that never despawns and comes back a day after dying.
 */
public class UniqueCat extends Cat {
    private CatProfile profile;

    public UniqueCat(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    public CatProfile profile() {
        if (profile == null) profile = CatProfiles.of(getType());
        return profile;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data, tag);
        if (!hasCustomName()) setCustomName(getType().getDescription());
        setPersistenceRequired();
        return result;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public float getVoicePitch() {
        return profile().voicePitch() * (0.95F + random.nextFloat() * 0.1F);
    }

    // Kittens are plain cats: every family cat is one of a kind.
    @Nullable
    @Override
    public Cat getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Cat kitten = EntityType.CAT.create(level);
        if (kitten != null && isTame()) {
            kitten.setOwnerUUID(getOwnerUUID());
            kitten.setTame(true);
        }
        return kitten;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        // Another mod may cancel the death (LivingDeathEvent); only a cat that really died counts.
        if (dead && level() instanceof ServerLevel server) CatSpawner.onDeath(server.getServer(), this);
    }
}
