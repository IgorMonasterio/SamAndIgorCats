package com.igormonasterio.samcats.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Place it on the ground for the cats, or right-click in the air to wear it. */
public class CardboardBoxItem extends BlockItem implements Equipable {
    public CardboardBoxItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getCount() == 1) return this.swapWithEquipmentSlot(this, level, player, hand);

        // From a stack, wear a single box: the vanilla swap would put the whole stack on your head.
        if (!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return InteractionResultHolder.fail(held);
        if (!level.isClientSide()) player.awardStat(Stats.ITEM_USED.get(this));
        player.setItemSlot(EquipmentSlot.HEAD, held.copyWithCount(1));
        if (!player.getAbilities().instabuild) held.shrink(1);
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.samcats.cardboard_box.tooltip1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.samcats.cardboard_box.tooltip2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.samcats.cardboard_box.tooltip3").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
