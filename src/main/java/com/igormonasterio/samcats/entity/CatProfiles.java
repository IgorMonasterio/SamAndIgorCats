package com.igormonasterio.samcats.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every cat in the mod. Names live in the lang files; this only holds what the code needs.
 */
public final class CatProfiles {
    private CatProfiles() {}

    public static final String NARU = "naru";
    public static final String IVY = "ivy";

    public static final List<CatProfile> ALL = List.of(
            new CatProfile(NARU, 1.0F, 1.0F, 0x8C8C8C, 0x3A3A3A),
            new CatProfile(IVY, 1.0F, 1.05F, 0xF2F2F2, 0x6B4226),
            new CatProfile("batman", 0.72F, 1.15F, 0x1A1A24, 0xCDCD46),
            new CatProfile("bonzo", 1.1F, 0.55F, 0x2A1E18, 0xF0CD37),
            new CatProfile("calcetin", 1.0F, 1.0F, 0x16151F, 0xEAEAEA),
            new CatProfile("cheeto", 1.0F, 1.1F, 0xEE9640, 0xF5F5F5),
            new CatProfile("dolores", 0.95F, 1.1F, 0x16151F, 0xFFFFFF),
            new CatProfile("el_abuelo", 1.05F, 0.8F, 0xE9DCC7, 0x31271F),
            new CatProfile("el_bebe", 0.85F, 1.3F, 0xB4B4B4, 0x707070),
            new CatProfile("sin_nombre", 1.0F, 1.0F, 0xEAA939, 0xEAEAEA),
            new CatProfile("el_negrito", 1.0F, 0.95F, 0x0C0C12, 0xF5D732),
            new CatProfile("itlerina", 0.95F, 1.15F, 0xFDF9FB, 0x18161E),
            new CatProfile("kalessi", 0.85F, 1.25F, 0xDCDEDE, 0xC2A88C),
            new CatProfile("la_trico", 1.0F, 1.0F, 0xDCDEDE, 0xDB9C3E),
            new CatProfile("lince", 1.05F, 0.9F, 0x5E4A3A, 0x2E241C),
            new CatProfile("mia", 1.0F, 1.05F, 0x16151F, 0x96603A),
            new CatProfile("noah", 1.0F, 1.0F, 0x48474A, 0xCD9137),
            new CatProfile("nube", 1.0F, 1.15F, 0xF7F7F7, 0x6B4F42),
            new CatProfile("oliver", 1.0F, 1.0F, 0xBCC2C8, 0x96B4CD),
            new CatProfile("stripey", 1.0F, 1.0F, 0x7A5A3C, 0x2E2018),
            new CatProfile("valentino", 1.12F, 0.9F, 0x82868F, 0xEBA528));

    private static final Map<String, CatProfile> BY_ID = new LinkedHashMap<>();

    static {
        for (CatProfile profile : ALL) BY_ID.put(profile.id(), profile);
    }

    public static CatProfile byId(String id) {
        CatProfile profile = BY_ID.get(id);
        if (profile == null) throw new IllegalArgumentException("Unknown cat " + id);
        return profile;
    }

    public static CatProfile of(EntityType<?> type) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return byId(key == null ? "" : key.getPath());
    }
}
