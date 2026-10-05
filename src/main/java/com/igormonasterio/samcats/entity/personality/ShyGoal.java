package com.igormonasterio.samcats.entity.personality;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import com.igormonasterio.samcats.entity.UniqueCat;

/**
 * Itlerina is very shy: she runs from anyone who isn't sneaking (her owner aside), so you have to creep up on her.
 * Replaces the vanilla "untamed cats run from players", which doesn't care about sneaking.
 */
public class ShyGoal extends AvoidEntityGoal<Player> {
    private final UniqueCat cat;

    public ShyGoal(UniqueCat cat) {
        super(cat, Player.class, 8.0F, 1.0D, 1.4D,
                e -> e instanceof Player p && !p.isCrouching() && !(cat.isTame() && cat.isOwnedBy(p)));
        this.cat = cat;
    }

    @Override
    public boolean canUse() {
        return !cat.isOrderedToSit() && super.canUse();
    }
}
