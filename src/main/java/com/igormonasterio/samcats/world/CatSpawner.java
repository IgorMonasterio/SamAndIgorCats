package com.igormonasterio.samcats.world;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import com.igormonasterio.samcats.ModRegistry;
import com.igormonasterio.samcats.entity.CatProfile;
import com.igormonasterio.samcats.entity.CatProfiles;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.List;

/**
 * Keeps exactly one of each family cat per world. Naru and Ivy show up first, together,
 * near a player who has been online for a little while; the rest arrive one by one every
 * few minutes. A cat that dies comes back one Minecraft day later.
 */
public final class CatSpawner {
    private CatSpawner() {}

    private static final int CHECK_INTERVAL = 200;      // 10 s
    private static final int MIN_PLAYER_TICKS = 400;    // player online for 20 s
    private static final long RETURN_DELAY = 24000L;    // one Minecraft day
    private static final int NEWCOMER_MIN = 2400;       // 2 min
    private static final int NEWCOMER_SPREAD = 2400;    // ...to 4 min

    public static void tick(MinecraftServer server) {
        ServerLevel level = server.overworld();
        long now = level.getGameTime();
        if (now % CHECK_INTERVAL != 0) return;

        WorldCatsData data = WorldCatsData.get(server);
        List<CatProfile> ready = CatProfiles.ALL.stream().filter(p -> data.isReady(p.id(), now)).toList();
        if (ready.isEmpty()) return;

        List<ServerPlayer> players = level.players().stream()
                .filter(p -> !p.isSpectator() && p.tickCount > MIN_PLAYER_TICKS).toList();
        if (players.isEmpty()) return;
        ServerPlayer player = players.get(level.random.nextInt(players.size()));

        List<CatProfile> pair = ready.stream()
                .filter(p -> p.id().equals(CatProfiles.NARU) || p.id().equals(CatProfiles.IVY)).toList();
        if (!pair.isEmpty()) {
            // Naru and Ivy always turn up together.
            BlockPos anchor = findSpot(level, player.blockPosition(), 10, 20);
            if (anchor == null) return;
            for (CatProfile profile : pair) {
                BlockPos near = findSpot(level, anchor, 2, 5);
                spawn(level, data, profile, near != null ? near : anchor, player);
            }
            data.nextNewcomer = now + NEWCOMER_MIN + level.random.nextInt(NEWCOMER_SPREAD);
            data.setDirty();
            return;
        }

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

    private static void spawn(ServerLevel level, WorldCatsData data, CatProfile profile, BlockPos pos, ServerPlayer player) {
        UniqueCat cat = ModRegistry.cat(profile.id()).create(level);
        if (cat == null) return;
        cat.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        cat.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        if (!level.addFreshEntity(cat)) return;
        data.setAlive(profile.id(), cat.getUUID());
        level.playSound(null, pos, SoundEvents.CAT_AMBIENT, SoundSource.NEUTRAL, 1.5F, profile.voicePitch());
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

            if (attempt < 24) {
                for (int dy = 6; dy >= -6; dy--) {
                    BlockPos pos = new BlockPos(x, center.getY() + dy, z);
                    if (isFree(level, pos)) return pos;
                }
            } else {
                // Last resort: the surface.
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
