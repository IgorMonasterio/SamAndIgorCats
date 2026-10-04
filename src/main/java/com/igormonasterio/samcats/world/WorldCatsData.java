package com.igormonasterio.samcats.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** Per-world memory of the one true copy of each family cat. */
public class WorldCatsData extends SavedData {
    private static final String NAME = "samcats_cats";

    /** Cat id → UUID of the living cat. Missing = waiting to (re)appear. */
    private final Map<String, UUID> alive = new HashMap<>();
    /** Cat id → game time from which it may appear again (after dying). */
    private final Map<String, Long> returnAt = new HashMap<>();
    /** Cat id → where the living cat was last known to be. */
    private final Map<String, GlobalPos> lastSeen = new HashMap<>();
    /** Cat id → UUID of the last living cat, kept after it dies or gets lost in case it turns up again. */
    private final Map<String, UUID> previous = new HashMap<>();
    /** Cat id → checks in a row where the living cat should have been loaded but wasn't. Not saved. */
    private final Map<String, Integer> misses = new HashMap<>();
    /** Game time from which the next new cat may show up. */
    long nextNewcomer;

    public static WorldCatsData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(WorldCatsData::load, WorldCatsData::new, NAME);
    }

    @Nullable
    UUID alive(String id) {
        return alive.get(id);
    }

    @Nullable
    UUID previous(String id) {
        return previous.get(id);
    }

    @Nullable
    GlobalPos lastSeen(String id) {
        return lastSeen.get(id);
    }

    boolean isReady(String id, long now) {
        return !alive.containsKey(id) && now >= returnAt.getOrDefault(id, 0L);
    }

    void setAlive(String id, UUID uuid) {
        alive.put(id, uuid);
        misses.remove(id);
        setDirty();
    }

    void setDead(String id, long returnTime) {
        UUID uuid = alive.remove(id);
        if (uuid != null) previous.put(id, uuid);
        returnAt.put(id, returnTime);
        misses.remove(id);
        setDirty();
    }

    void seen(String id, GlobalPos pos) {
        misses.remove(id);
        if (!pos.equals(lastSeen.put(id, pos))) setDirty();
    }

    /** Counts one more check without finding the cat where it should be; returns the count. */
    int miss(String id) {
        return misses.merge(id, 1, Integer::sum);
    }

    void clearMisses(String id) {
        misses.remove(id);
    }

    private static WorldCatsData load(CompoundTag tag) {
        WorldCatsData data = new WorldCatsData();
        CompoundTag cats = tag.getCompound("Cats");
        for (String id : cats.getAllKeys()) {
            CompoundTag cat = cats.getCompound(id);
            if (cat.hasUUID("UUID")) data.alive.put(id, cat.getUUID("UUID"));
            data.returnAt.put(id, cat.getLong("Return"));
            if (cat.hasUUID("Previous")) data.previous.put(id, cat.getUUID("Previous"));
            ResourceLocation dim = ResourceLocation.tryParse(cat.getString("SeenDim"));
            if (dim != null && cat.contains("SeenPos")) {
                data.lastSeen.put(id, GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dim),
                        BlockPos.of(cat.getLong("SeenPos"))));
            }
        }
        data.nextNewcomer = tag.getLong("NextNewcomer");
        // 0.0.1 worlds only knew Naru and Ivy.
        if (tag.hasUUID("Naru")) data.alive.put("naru", tag.getUUID("Naru"));
        if (tag.hasUUID("Ivy")) data.alive.put("ivy", tag.getUUID("Ivy"));
        if (tag.contains("NaruReturn")) data.returnAt.putIfAbsent("naru", tag.getLong("NaruReturn"));
        if (tag.contains("IvyReturn")) data.returnAt.putIfAbsent("ivy", tag.getLong("IvyReturn"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag cats = new CompoundTag();
        for (String id : union()) {
            CompoundTag cat = new CompoundTag();
            if (alive.containsKey(id)) cat.putUUID("UUID", alive.get(id));
            cat.putLong("Return", returnAt.getOrDefault(id, 0L));
            if (previous.containsKey(id)) cat.putUUID("Previous", previous.get(id));
            GlobalPos seen = lastSeen.get(id);
            if (seen != null) {
                cat.putString("SeenDim", seen.dimension().location().toString());
                cat.putLong("SeenPos", seen.pos().asLong());
            }
            cats.put(id, cat);
        }
        tag.put("Cats", cats);
        tag.putLong("NextNewcomer", nextNewcomer);
        return tag;
    }

    private Set<String> union() {
        Set<String> ids = new TreeSet<>(alive.keySet());
        ids.addAll(returnAt.keySet());
        ids.addAll(previous.keySet());
        ids.addAll(lastSeen.keySet());
        return ids;
    }
}
