package top.ribs.scguns.init;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ModMuzzleFlashes {
    private static final Map<String, ResourceLocation> muzzleFlashes = new HashMap<>();

    public static void registerMuzzleFlash(String flashType, ResourceLocation texture) {
        muzzleFlashes.put(flashType, texture);
    }

    public static ResourceLocation getMuzzleFlashTexture(String flashType) {
        return muzzleFlashes.getOrDefault(flashType,
                new ResourceLocation("scguns", "textures/effect/muzzle_flash_1.png"));
    }

    public static void init() {
        registerMuzzleFlash("flash_type_1", new ResourceLocation("scguns", "textures/effect/muzzle_flash_1.png"));
        registerMuzzleFlash("flash_type_2", new ResourceLocation("scguns", "textures/effect/muzzle_flash_2.png"));
        registerMuzzleFlash("flash_type_3", new ResourceLocation("scguns", "textures/effect/muzzle_flash_3.png"));
        registerMuzzleFlash("flash_type_4", new ResourceLocation("scguns", "textures/effect/muzzle_flash_4.png"));
        registerMuzzleFlash("flash_type_5", new ResourceLocation("scguns", "textures/effect/muzzle_flash_5.png"));
        registerMuzzleFlash("flash_type_6", new ResourceLocation("scguns", "textures/effect/muzzle_flash_6.png"));
        registerMuzzleFlash("flash_type_7", new ResourceLocation("scguns", "textures/effect/muzzle_flash_7.png"));
    }
}
