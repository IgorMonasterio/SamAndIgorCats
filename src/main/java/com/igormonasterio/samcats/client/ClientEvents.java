package com.igormonasterio.samcats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.igormonasterio.samcats.BoxStealth;
import com.igormonasterio.samcats.ModRegistry;
import com.igormonasterio.samcats.SamCats;
import com.igormonasterio.samcats.block.CardboardBoxBlock;
import com.igormonasterio.samcats.entity.CatProfile;
import com.igormonasterio.samcats.entity.CatProfiles;

public final class ClientEvents {
    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = SamCats.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            for (CatProfile profile : CatProfiles.ALL) {
                event.<Cat>registerEntityRenderer(ModRegistry.cat(profile.id()), ctx -> new UniqueCatRenderer(ctx, profile));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = SamCats.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {
        /** A sneaking player wearing the box is drawn as just a box. No name tag either. */
        @SubscribeEvent
        public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
            Player player = event.getEntity();
            if (!BoxStealth.isBoxed(player)) return;
            event.setCanceled(true);

            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(0.0D, 0.125D, 0.0D); // undo the vanilla sneaking offset
            float bodyYaw = Mth.rotLerp(event.getPartialTick(), player.yBodyRotO, player.yBodyRot);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
            pose.scale(1.0F, 1.3F, 1.0F);
            pose.translate(-0.5D, 0.0D, -0.5D);
            BlockState box = ModRegistry.CARDBOARD_BOX.get().defaultBlockState().setValue(CardboardBoxBlock.CLOSED, true);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(box, pose, event.getMultiBufferSource(),
                    event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }
}
