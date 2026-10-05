package com.igormonasterio.samcats.entity.personality;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import com.igormonasterio.samcats.entity.UniqueCat;

/** Oliver looks grumpy: run at him and he hisses and backs off... unless you've stroked him lately. */
public class GrumpyGoal extends AvoidEntityGoal<Player> {
    private final UniqueCat cat;

    public GrumpyGoal(UniqueCat cat) {
        super(cat, Player.class, 6.0F, 1.0D, 1.3D, e -> e.isSprinting() && !cat.isCalm());
        this.cat = cat;
    }

    @Override
    public boolean canUse() {
        return !cat.isOrderedToSit() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        cat.playSound(SoundEvents.CAT_HISS, 1.0F, cat.getVoicePitch());
    }
}
