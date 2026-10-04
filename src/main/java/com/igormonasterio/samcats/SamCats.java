package com.igormonasterio.samcats;

import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
        ModRegistry.CATS.values().forEach(type -> event.put(type.get(), Cat.createAttributes().build()));
    }
}
