package com.igormonasterio.samcats;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags;

/** Metal Gear rules: wear the box, sneak, don't hit anything, and monsters lose you. */
public final class BoxStealth {
    private BoxStealth() {}

    /** Ticks after hitting a mob during which the box doesn't hide you. */
    private static final int ATTACK_GRACE = 60;

    public static boolean wearsBox(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModRegistry.CARDBOARD_BOX_ITEM.get());
    }

    /** Wearing the box and sneaking: drawn as a box on screen. */
    public static boolean isBoxed(Player player) {
        return wearsBox(player) && player.isCrouching() && !player.isSpectator();
    }

    /** Boxed and hasn't attacked anything recently: monsters can't see you. */
    public static boolean isHidden(Player player) {
        return isBoxed(player) && player.tickCount - player.getLastHurtMobTimestamp() > ATTACK_GRACE;
    }

    /** Bosses and the Warden aren't fooled by a box. */
    public static boolean canBeFooled(LivingEntity mob) {
        return !(mob instanceof Warden) && !mob.getType().is(Tags.EntityTypes.BOSSES);
    }
}
