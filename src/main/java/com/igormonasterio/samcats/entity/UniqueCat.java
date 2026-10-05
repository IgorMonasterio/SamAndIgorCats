package com.igormonasterio.samcats.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.entity.personality.Personalities;
import com.igormonasterio.samcats.world.CatSpawner;

import java.util.ArrayList;
import java.util.List;

/**
 * One of the family cats: a named cat that never despawns and comes back a day after dying.
 * Each one has its own personality (see {@link Personalities}).
 */
public class UniqueCat extends Cat {
    /** How many stacks Calcetín can hoard. */
    public static final int STASH_SIZE = 9;

    private CatProfile profile;

    // Personality state. Only the cats that use each field ever fill it.
    private final List<ItemStack> stash = new ArrayList<>();   // Calcetín's loot
    @Nullable private BlockPos stashHome;                      // ...and his corner
    private long calmUntil;                                    // Oliver, after a stroke
    @Nullable private Cat greetTarget;                         // La Trico's next newcomer
    private ItemStack gift = ItemStack.EMPTY;                  // Lince's catch for his owner

    public UniqueCat(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    public CatProfile profile() {
        if (profile == null) profile = CatProfiles.of(getType());
        return profile;
    }

    public boolean is(String id) {
        return profile().id().equals(id);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        Personalities.addGoals(this);
    }

    @Override
    protected void reassessTameGoals() {
        super.reassessTameGoals();
        // Cheeto is curious and Itlerina has her own rules: neither uses the vanilla "run from every player".
        if (is(CatProfiles.CHEETO) || is(CatProfiles.ITLERINA)) {
            goalSelector.removeAllGoals(g -> g instanceof AvoidEntityGoal<?> && g.getClass().getEnclosingClass() == Cat.class);
        }
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

    /** Sneak + right click with an empty hand. Every cat enjoys it; some of them react in their own way. */
    public void onPetted(Player player) {
        hearts(4);
        playSound(SoundEvents.CAT_PURR, 1.0F, getVoicePitch());
        Personalities.onPetted(this, player);
    }

    public void hearts(int count) {
        Personalities.hearts(this, count);
    }

    // --- Nube floats down like a cloud ---
    @Override
    public void aiStep() {
        super.aiStep();
        if (is(CatProfiles.NUBE) && !level().isClientSide && !onGround() && !isInWater()) {
            Vec3 motion = getDeltaMovement();
            if (motion.y < -0.08D) {
                setDeltaMovement(motion.x, -0.08D, motion.z);
                resetFallDistance();
                if (tickCount % 2 == 0 && level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.2D, getZ(), 1, 0.15D, 0.05D, 0.15D, 0.0D);
                }
            }
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return !is(CatProfiles.NUBE) && super.causeFallDamage(distance, multiplier, source);
    }

    // --- Calcetín's stash ---
    public List<ItemStack> stash() {
        return stash;
    }

    @Nullable
    public BlockPos stashHome() {
        return stashHome;
    }

    public void setStashHome(@Nullable BlockPos pos) {
        this.stashHome = pos;
    }

    public void giveStashBack(Player player) {
        if (stash.isEmpty()) return;
        for (ItemStack stack : stash) {
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
        stash.clear();
        playSound(SoundEvents.ITEM_PICKUP, 0.6F, 1.0F);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        stash.forEach(this::spawnAtLocation);
        stash.clear();
    }

    // --- Oliver calms down after a stroke ---
    public boolean isCalm() {
        return level().getGameTime() < calmUntil;
    }

    public void calmDown(int ticks) {
        calmUntil = level().getGameTime() + ticks;
    }

    // --- La Trico welcomes newcomers ---
    @Nullable
    public Cat takeGreetTarget() {
        Cat target = greetTarget;
        greetTarget = null;
        return target;
    }

    public void greet(Cat newcomer) {
        this.greetTarget = newcomer;
    }

    // --- Lince brings presents ---
    public ItemStack gift() {
        return gift;
    }

    public void setGift(ItemStack gift) {
        this.gift = gift;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!stash.isEmpty()) {
            ListTag list = new ListTag();
            stash.forEach(stack -> list.add(stack.save(new CompoundTag())));
            tag.put("SamcatsStash", list);
        }
        if (stashHome != null) tag.put("SamcatsStashHome", NbtUtils.writeBlockPos(stashHome));
        if (calmUntil > 0) tag.putLong("SamcatsCalmUntil", calmUntil);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        stash.clear();
        for (Tag item : tag.getList("SamcatsStash", Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.of((CompoundTag) item);
            if (!stack.isEmpty()) stash.add(stack);
        }
        stashHome = tag.contains("SamcatsStashHome") ? NbtUtils.readBlockPos(tag.getCompound("SamcatsStashHome")) : null;
        calmUntil = tag.getLong("SamcatsCalmUntil");
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (level() instanceof ServerLevel server) CatSpawner.onLoaded(server.getServer(), this);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (level() instanceof ServerLevel server) CatSpawner.onUnloaded(server.getServer(), this);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        // Another mod may cancel the death (LivingDeathEvent); only a cat that really died counts.
        if (dead && level() instanceof ServerLevel server) CatSpawner.onDeath(server.getServer(), this);
    }
}
