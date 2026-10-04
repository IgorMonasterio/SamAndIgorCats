package com.igormonasterio.samcats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import com.igormonasterio.samcats.SamCats;
import com.igormonasterio.samcats.entity.CatProfile;

/** The vanilla cat model with each family cat's own coat and size. */
public class UniqueCatRenderer extends CatRenderer {
    private final ResourceLocation texture;
    private final float size;

    public UniqueCatRenderer(EntityRendererProvider.Context context, CatProfile profile) {
        super(context);
        this.texture = new ResourceLocation(SamCats.MODID, "textures/entity/" + profile.id() + ".png");
        this.size = profile.scale();
        this.shadowRadius *= size;
    }

    @Override
    public ResourceLocation getTextureLocation(Cat cat) {
        return texture;
    }

    @Override
    protected void scale(Cat cat, PoseStack pose, float partialTick) {
        super.scale(cat, pose, partialTick);
        pose.scale(size, size, size);
    }
}
