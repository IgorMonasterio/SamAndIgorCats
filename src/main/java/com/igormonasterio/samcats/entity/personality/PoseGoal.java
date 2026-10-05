package com.igormonasterio.samcats.entity.personality;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/** Valentino lets you admire him: stare at him for a few seconds and he sits down and poses, all sparkles. */
public class PoseGoal extends Goal {
    private static final int GAZE_TICKS = 60;
    private static final int POSE_TICKS = 100;

    private final UniqueCat cat;
    private Player admirer;
    private long gazeStart = -1L;
    private long nextPose;
    private int ticksLeft;

    public PoseGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    private boolean isAdmiring(Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toCat = new Vec3(cat.getX() - player.getX(), cat.getY(0.6D) - player.getEyeY(), cat.getZ() - player.getZ());
        double distance = toCat.length();
        return view.dot(toCat.normalize()) > 1.0D - 0.05D / distance && player.hasLineOfSight(cat);
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit()) return false;
        long now = cat.level().getGameTime();
        Player player = cat.level().getNearestPlayer(cat, 10.0D);
        if (player == null || !isAdmiring(player)) {
            gazeStart = -1L;
            return false;
        }
        if (gazeStart < 0) gazeStart = now;
        admirer = player;
        return now - gazeStart >= GAZE_TICKS && now >= nextPose;
    }

    @Override
    public boolean canContinueToUse() {
        return ticksLeft > 0 && !cat.isOrderedToSit();
    }

    @Override
    public void start() {
        ticksLeft = POSE_TICKS;
        cat.getNavigation().stop();
        cat.setInSittingPose(true);
    }

    @Override
    public void tick() {
        ticksLeft--;
        if (admirer != null) cat.getLookControl().setLookAt(admirer, 30.0F, 30.0F);
        if (ticksLeft % 5 == 0 && cat.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, cat.getX(), cat.getY(0.8D), cat.getZ(), 2, 0.35D, 0.3D, 0.35D, 0.01D);
        }
    }

    @Override
    public void stop() {
        if (!cat.isOrderedToSit()) cat.setInSittingPose(false);
        admirer = null;
        gazeStart = -1L;
        nextPose = cat.level().getGameTime() + 400;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
