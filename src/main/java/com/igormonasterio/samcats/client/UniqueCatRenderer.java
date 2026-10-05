package com.igormonasterio.samcats.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import com.igormonasterio.samcats.SamCats;
import com.igormonasterio.samcats.entity.CatProfile;
import com.igormonasterio.samcats.entity.CatProfiles;

/** The vanilla cat model with each family cat's own coat and size. Naru wears a crown; El Negrito's eyes glow. */
public class UniqueCatRenderer extends CatRenderer {
    private static final RenderType NEGRITO_EYES = RenderType.eyes(
            new ResourceLocation(SamCats.MODID, "textures/entity/el_negrito_eyes.png"));

    private final ResourceLocation texture;
    private final float size;

    public UniqueCatRenderer(EntityRendererProvider.Context context, CatProfile profile) {
        super(context);
        this.model = new SamCatModel(context.bakeLayer(ModelLayers.CAT));
        this.texture = new ResourceLocation(SamCats.MODID, "textures/entity/" + profile.id() + ".png");
        this.size = profile.scale();
        this.shadowRadius *= size;
        if (profile.id().equals(CatProfiles.NARU)) addLayer(new CrownLayer(this));
        // At night, all you can see of El Negrito are his eyes.
        if (profile.id().equals(CatProfiles.EL_NEGRITO)) {
            addLayer(new EyesLayer<Cat, CatModel<Cat>>(this) {
                @Override
                public RenderType renderType() {
                    return NEGRITO_EYES;
                }
            });
        }
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
