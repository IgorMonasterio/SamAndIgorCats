package com.igormonasterio.samcats;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side record of where each player's laser dot is right now. */
public final class LaserTracker {
    private LaserTracker() {}

    private record Dot(ResourceKey<Level> dimension, Vec3 pos, long gameTime) {}

    private static final Map<UUID, Dot> DOTS = new ConcurrentHashMap<>();

    public static void update(Player player, Vec3 pos) {
        DOTS.put(player.getUUID(), new Dot(player.level().dimension(), pos, player.level().getGameTime()));
    }

    public static void clear(UUID player) {
        DOTS.remove(player);
    }

    /** Closest dot in this level, younger than maxAge ticks and within maxDist blocks of {@code from}. */
    @Nullable
    public static Vec3 nearest(Level level, Vec3 from, double maxDist, int maxAge) {
        long now = level.getGameTime();
        Vec3 best = null;
        double bestDist = maxDist * maxDist;
        for (Dot dot : DOTS.values()) {
            if (dot.dimension() != level.dimension() || now - dot.gameTime() > maxAge) continue;
            double dist = dot.pos().distanceToSqr(from);
            if (dist < bestDist) {
                bestDist = dist;
                best = dot.pos();
            }
        }
        return best;
    }
}
