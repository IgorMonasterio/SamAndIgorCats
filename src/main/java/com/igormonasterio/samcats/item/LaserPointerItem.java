package com.igormonasterio.samcats.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import com.igormonasterio.samcats.LaserTracker;

import java.util.List;

/** Hold right-click: a red dot appears where you aim and every tamed cat goes after it. */
public class LaserPointerItem extends Item {
    private static final double RANGE = 48.0D;
    private static final DustParticleOptions RED_DOT = new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.05F), 0.9F);

    public LaserPointerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        level.playSound(null, player, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.PLAYERS, 0.3F, 2.0F);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server) || !(user instanceof Player player)) return;
        HitResult hit = player.pick(RANGE, 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            LaserTracker.clear(player.getUUID());
            return;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getNormal());
        Vec3 dot = blockHit.getLocation().add(normal.scale(0.03D));
        server.sendParticles(RED_DOT, dot.x, dot.y, dot.z, 1, 0, 0, 0, 0);
        LaserTracker.update(player, dot);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (!level.isClientSide()) {
            LaserTracker.clear(user.getUUID());
            level.playSound(null, user, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.PLAYERS, 0.3F, 2.0F);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.samcats.laser_pointer.tooltip1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.samcats.laser_pointer.tooltip2").withStyle(ChatFormatting.GRAY));
    }
}
