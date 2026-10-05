package com.igormonasterio.samcats;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.igormonasterio.samcats.entity.CatProfile;
import com.igormonasterio.samcats.entity.CatProfiles;

/**
 * SamAndIgorCats: a family of unique cats (one of each per world), the cardboard box and the laser pointer.
 */
@Mod(SamCats.MODID)
public class SamCats {
    public static final String MODID = "samcats";

    public SamCats() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(modBus);
        modBus.addListener(this::onAttributes);
    }

    private void onAttributes(EntityAttributeCreationEvent event) {
        for (CatProfile profile : CatProfiles.ALL) {
            AttributeSupplier.Builder attributes = Cat.createAttributes();
            // El Abuelo has seen it all and takes his time.
            if (profile.id().equals(CatProfiles.EL_ABUELO)) attributes.add(Attributes.MOVEMENT_SPEED, 0.22D);
            event.put(ModRegistry.cat(profile.id()), attributes.build());
        }
    }
}
