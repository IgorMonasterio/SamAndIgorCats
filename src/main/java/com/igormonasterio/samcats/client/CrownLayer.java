package com.igormonasterio.samcats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import com.igormonasterio.samcats.SamCats;

/** Naru is the Queen: a little golden crown on her head, between the ears. */
public class CrownLayer extends RenderLayer<Cat, CatModel<Cat>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SamCats.MODID, "textures/entity/crown.png");
    private final ModelPart crown = createCrown();

    public CrownLayer(RenderLayerParent<Cat, CatModel<Cat>> parent) {
        super(parent);
    }

    // In head space (pixels, y down): the head is x -2.5..2.5, y -2..2, z -3..2 and the ears reach y -3.
    private static ModelPart createCrown() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -4.0F, -2.5F, 4.0F, 1.0F, 4.0F)   // band
                .texOffs(0, 6).addBox(-2.0F, -5.0F, -2.5F, 1.0F, 1.0F, 1.0F)   // points
                .texOffs(0, 6).addBox(1.0F, -5.0F, -2.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 6).addBox(-2.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 6).addBox(1.0F, -5.0F, 0.5F, 1.0F, 1.0F, 1.0F)
                .texOffs(0, 9).addBox(-0.5F, -6.0F, -2.5F, 1.0F, 2.0F, 1.0F),  // front point, with the gem
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16).bakeRoot();
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Cat cat, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (cat.isInvisible() || !(getParentModel() instanceof SamCatModel model)) return;
        pose.pushPose();
        model.head().translateAndRotate(pose);
        crown.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
