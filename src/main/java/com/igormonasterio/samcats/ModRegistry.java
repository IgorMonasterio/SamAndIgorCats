package com.igormonasterio.samcats;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.igormonasterio.samcats.block.CardboardBoxBlock;
import com.igormonasterio.samcats.entity.CatProfile;
import com.igormonasterio.samcats.entity.CatProfiles;
import com.igormonasterio.samcats.entity.IvyEntity;
import com.igormonasterio.samcats.entity.NaruEntity;
import com.igormonasterio.samcats.entity.UniqueCat;
import com.igormonasterio.samcats.item.CardboardBoxItem;
import com.igormonasterio.samcats.item.LaserPointerItem;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModRegistry {
    private ModRegistry() {}

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SamCats.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SamCats.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SamCats.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SamCats.MODID);

    // --- The family cats: one entity type and one spawn egg each ---
    public static final Map<String, RegistryObject<EntityType<UniqueCat>>> CATS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> CAT_EGGS = new LinkedHashMap<>();

    static {
        for (CatProfile profile : CatProfiles.ALL) {
            String id = profile.id();
            RegistryObject<EntityType<UniqueCat>> type = ENTITIES.register(id,
                    () -> EntityType.Builder.<UniqueCat>of((t, level) -> switch (id) {
                                case CatProfiles.NARU -> new NaruEntity(t, level);
                                case CatProfiles.IVY -> new IvyEntity(t, level);
                                default -> new UniqueCat(t, level);
                            }, MobCategory.CREATURE)
                            .sized(0.6F * profile.scale(), 0.7F * profile.scale()).clientTrackingRange(10).build(id));
            CATS.put(id, type);
            CAT_EGGS.put(id, ITEMS.register(id + "_spawn_egg",
                    () -> new ForgeSpawnEggItem(type, profile.eggBase(), profile.eggSpots(), new Item.Properties())));
        }
    }

    public static EntityType<UniqueCat> cat(String id) {
        return CATS.get(id).get();
    }

    // --- Cardboard box ---
    public static final RegistryObject<CardboardBoxBlock> CARDBOARD_BOX = BLOCKS.register("cardboard_box",
            () -> new CardboardBoxBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(0.4F).sound(SoundType.WOOL).noOcclusion()));
    public static final RegistryObject<Item> CARDBOARD_BOX_ITEM = ITEMS.register("cardboard_box",
            () -> new CardboardBoxItem(CARDBOARD_BOX.get(), new Item.Properties().stacksTo(16)));

    // --- Laser pointer ---
    public static final RegistryObject<Item> LASER_POINTER = ITEMS.register("laser_pointer",
            () -> new LaserPointerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.samcats"))
            .icon(() -> new ItemStack(LASER_POINTER.get()))
            .displayItems((params, out) -> {
                out.accept(CARDBOARD_BOX_ITEM.get());
                out.accept(LASER_POINTER.get());
                CAT_EGGS.values().forEach(egg -> out.accept(egg.get()));
            })
            .build());

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        TABS.register(modBus);
    }
}
