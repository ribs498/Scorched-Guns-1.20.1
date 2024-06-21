package top.ribs.scguns.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.*;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModSounds;

import java.util.concurrent.CompletableFuture;


/**
 * Author: MrCrayfish
 */
public class GunGen extends GunProvider
{
    public GunGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    protected void registerGuns()
    {


        /* TODO: Revisit this one boi!
        this.addGun(new ResourceLocation(Reference.MOD_ID, "bubble_cannon"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.BURST)
                .setBurstAmount(3)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(3.0F)
                .setAlwaysSpread(true)
                .setSpread(10.0F)

                // Reloads
                .setMaxAmmo(3)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(30)
                .setEmptyMagTimer(10)

                // Projectile
                .setAmmo(ModItems.POCKET_BUBBLE.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(8.0F)
                .setAdvantage(ModTags.Entities.FIRE.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(2F)
                .setProjectileLife(200)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.TYPHOONEE_FIRE.get())
                .setReloadSound(ModSounds.ITEM_PISTOL_RELOAD.get())
                .setCockSound(ModSounds.ITEM_PISTOL_COCK.get())
                .setEnchantedFireSound(ModSounds.TYPHOONEE_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 2.5, -3.03)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.45, -1.25))

                .build());*/

    }
}
