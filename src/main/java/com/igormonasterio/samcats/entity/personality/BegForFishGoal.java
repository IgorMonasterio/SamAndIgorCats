package com.igormonasterio.samcats.entity.personality;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.igormonasterio.samcats.entity.UniqueCat;

import java.util.EnumSet;

/** Bonzo always comes round for treats: show him a fish and he follows you, begging with his broken meow. */
public class BegForFishGoal extends Goal {
    private final UniqueCat cat;
    private Player player;
    private int recalc;
    private int meowIn;

    public BegForFishGoal(UniqueCat cat) {
        this.cat = cat;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    static boolean holdsFish(Player player) {
        return isFish(player.getMainHandItem()) || isFish(player.getOffhandItem());
    }

    private static boolean isFish(ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.COOKED_COD) || stack.is(Items.COOKED_SALMON);
    }

    @Override
    public boolean canUse() {
        if (cat.isOrderedToSit()) return false;
        player = cat.level().getNearestPlayer(TargetingConditions.forNonCombat().range(8.0D)
                .selector(e -> e instanceof Player p && holdsFish(p)), cat);
        return player != null;
    }

    @Override
    public boolean canContinueToUse() {
        return player != null && player.isAlive() && !cat.isOrderedToSit() && holdsFish(player)
                && cat.distanceToSqr(player) < 12 * 12;
    }

    @Override
    public void start() {
        recalc = 0;
        meowIn = 10;
    }

    @Override
    public void tick() {
        cat.getLookControl().setLookAt(player, 30.0F, cat.getMaxHeadXRot());
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            if (cat.distanceToSqr(player) > 2.5D * 2.5D) cat.getNavigation().moveTo(player, 1.0D);
            else cat.getNavigation().stop();
        }
        if (--meowIn <= 0) {
            meowIn = 40 + cat.getRandom().nextInt(40);
            cat.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0F, cat.getVoicePitch());
        }
    }

    @Override
    public void stop() {
        player = null;
        cat.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
