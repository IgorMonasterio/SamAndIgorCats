package com.igormonasterio.samcats.client;

import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Cat;

/** The vanilla cat model, with its head reachable so Naru's crown can sit on it. */
public class SamCatModel extends CatModel<Cat> {
    public SamCatModel(ModelPart root) {
        super(root);
    }

    public ModelPart head() {
        return head;
    }
}
