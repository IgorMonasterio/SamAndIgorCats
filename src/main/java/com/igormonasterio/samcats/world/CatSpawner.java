package com.igormonasterio.samcats.world;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.ModRegistry;
import com.igormonasterio.samcats.entity.CatProfile;
import com.igormonasterio.samcats.entity.CatProfiles;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.List;
import java.util.UUID;

/**
 * Keeps exactly one of each family cat per world. They arrive one by one, in random order,
 * near a player (in any dimension) who has been online for a little while: the first soon after,
 * then one every few minutes. A cat that dies comes back one Minecraft day later.
 * A cat that vanishes without dying (a crash at the wrong moment, another mod) is given up
 * for lost and comes back too; if the lost one turns up again, it is taken back.
 */
public final class CatSpawner {
    private CatSpawner() {}

    private static final int CHECK_INTERVAL = 200;      // 10 s
    private static final int MIN_PLAYER_TICKS = 400;    // player online for 20 s: the first cat turns up 20-30 s after joining
    private static final long RETURN_DELAY = 24000L;    // one Minecraft day
    private static final int NEWCOMER_MIN = 2400;       // 2 min
    private static final int NEWCOMER_SPREAD = 2400;    // ...to 4 min
    private static final int MISSES_TO_LOSE = 2;        // checks in a row not found where it should be
    private static final int LOST_SEARCH_CHUNKS = 2;    // chunk radius around the last known spot

    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        if (now % CHECK_INTERVAL != 0) return;

        WorldCatsData data = WorldCatsData.get(server);
        track(server, data, now);
        List<CatProfile> ready = CatProfiles.ALL.stream().filter(p -> data.isReady(p.id(), now)).toList();
        if (ready.isEmpty()) return;

        // Any dimension: players who live in the Nether or the End get cats too.
        List<ServerPlayer> players = server.getPlayerList().getPlayers().stream()
                .filter(p -> !p.isSpectator() && p.tickCount > MIN_PLAYER_TICKS).toList();
        if (players.isEmpty()) return;
        ServerPlayer player = players.get(server.overworld().random.nextInt(players.size()));
        ServerLevel level = player.serverLevel();

        if (now < data.nextNewcomer) return;
        CatProfile profile = ready.get(level.random.nextInt(ready.size()));
        BlockPos spot = findSpot(level, player.blockPosition(), 10, 20);
        if (spot == null) return;
        spawn(level, data, profile, spot, player);
        data.nextNewcomer = now + NEWCOMER_MIN + level.random.nextInt(NEWCOMER_SPREAD);
        data.setDirty();
    }

    /** Called when any family cat dies; only the world's own copy counts. */
    public static void onDeath(MinecraftServer server, UniqueCat cat) {
        WorldCatsData data = WorldCatsData.get(server);
        String id = cat.profile().id();
        if (!cat.getUUID().equals(data.alive(id))) return;
        data.setDead(id, server.overworld().getGameTime() + RETURN_DELAY);
        server.getPlayerList().broadcastSystemMessage(
                Component.translatable("samcats.msg.ran_away", cat.getDisplayName()).withStyle(ChatFormatting.GRAY), false);
    }

    /** A family cat entered a level: spawned, loaded from disk or arrived from another dimension. */
    public static void onLoaded(MinecraftServer server, UniqueCat cat) {
        if (cat.isDeadOrDying()) return;
        WorldCatsData data = WorldCatsData.get(server);
        String id = cat.profile().id();
        UUID uuid = cat.getUUID();
        if (data.alive(id) == null && uuid.equals(data.previous(id))) {
            // The world's own cat was given up for lost (e.g. the data was saved but its chunk wasn't
            // before a crash), and here it is again: it's still the real one.
            data.setAlive(id, uuid);
        }
        if (uuid.equals(data.alive(id))) data.seen(id, GlobalPos.of(cat.level().dimension(), cat.blockPosition()));
    }

    /** A family cat left its level. Remember where it was saved, or notice it was deleted. */
    public static void onUnloaded(MinecraftServer server, UniqueCat cat) {
        WorldCatsData data = WorldCatsData.get(server);
        String id = cat.profile().id();
        if (!cat.getUUID().equals(data.alive(id))) return;
        Entity.RemovalReason reason = cat.getRemovalReason();
        if (reason == Entity.RemovalReason.DISCARDED) {
            // Deleted without dying (usually another mod): treat it as lost, it comes back soon.
            data.setDead(id, server.overworld().getGameTime());
        } else if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
            data.seen(id, GlobalPos.of(cat.level().dimension(), cat.blockPosition()));
        }
    }

    /**
     * Checks the tracked cats against the world. A cat that's loaded is followed around; one that's
     * nowhere to be found although the area where it was last seen is loaded is given up for lost.
     */
    private static void track(MinecraftServer server, WorldCatsData data, long now) {
        for (CatProfile profile : CatProfiles.ALL) {
            String id = profile.id();
            UUID uuid = data.alive(id);
            if (uuid == null) continue;
            Entity cat = find(server, uuid);
            if (cat != null) {
                data.seen(id, GlobalPos.of(cat.level().dimension(), cat.blockPosition()));
            } else if (!isAreaLoaded(server, data.lastSeen(id))) {
                data.clearMisses(id);
            } else if (data.miss(id) >= MISSES_TO_LOSE) {
                data.setDead(id, now);
            }
        }
    }

    @Nullable
    private static Entity find(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }

    /** Whether the entities of every chunk around {@code pos} are loaded, so a cat there would be found. */
    private static boolean isAreaLoaded(MinecraftServer server, @Nullable GlobalPos pos) {
        if (pos == null) return false;
        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null) return false;
        ChunkPos center = new ChunkPos(pos.pos());
        for (int dx = -LOST_SEARCH_CHUNKS; dx <= LOST_SEARCH_CHUNKS; dx++) {
            for (int dz = -LOST_SEARCH_CHUNKS; dz <= LOST_SEARCH_CHUNKS; dz++) {
                if (!level.areEntitiesLoaded(ChunkPos.asLong(center.x + dx, center.z + dz))) return false;
            }
        }
        return true;
    }

    private static void spawn(ServerLevel level, WorldCatsData data, CatProfile profile, BlockPos pos, ServerPlayer player) {
        UniqueCat cat = ModRegistry.cat(profile.id()).create(level);
        if (cat == null) return;
        cat.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        cat.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        if (!level.addFreshEntity(cat)) return;
        data.setAlive(profile.id(), cat.getUUID());
        data.seen(profile.id(), GlobalPos.of(level.dimension(), pos));
        level.playSound(null, pos, SoundEvents.CAT_AMBIENT, SoundSource.NEUTRAL, 1.5F, profile.voicePitch());
        // La Trico, the neighbourhood gossip, comes over to say hello.
        level.getEntitiesOfClass(UniqueCat.class, cat.getBoundingBox().inflate(64.0D), c -> c != cat && c.is(CatProfiles.LA_TRICO))
                .forEach(trico -> trico.greet(cat));
        player.sendSystemMessage(Component.translatable("samcats.msg.appeared", cat.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    /** A free spot (solid floor, two air blocks) in a ring around {@code center}, near its height first. */
    @Nullable
    private static BlockPos findSpot(ServerLevel level, BlockPos center, int minRadius, int maxRadius) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < 32; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
            if (!level.hasChunkAt(new BlockPos(x, center.getY(), z))) continue;

            if (attempt < 24 || level.dimensionType().hasCeiling()) {
                for (int dy = 6; dy >= -6; dy--) {
                    BlockPos pos = new BlockPos(x, center.getY() + dy, z);
                    if (isFree(level, pos)) return pos;
                }
            } else {
                // Last resort: the surface (not in the Nether, where that's the bedrock roof).
                BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
                if (isFree(level, pos)) return pos;
            }
        }
        return null;
    }

    private static boolean isFree(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && level.getFluidState(pos).isEmpty();
    }
}
