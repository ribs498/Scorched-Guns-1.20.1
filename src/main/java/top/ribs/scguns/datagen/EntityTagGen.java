package top.ribs.scguns.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.ModTags;
import top.ribs.scguns.init.ModEntities;

import java.util.concurrent.CompletableFuture;

public class EntityTagGen extends EntityTypeTagsProvider
{
    public EntityTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.tag(ModTags.Entities.NONE);

        this.tag(ModTags.Entities.HEAVY)
                .add(EntityType.GHAST)
                .add(EntityType.HOGLIN)
                .add(EntityType.POLAR_BEAR)
                .add(EntityType.RAVAGER)
                .add(EntityType.SNIFFER)
                .add(EntityType.TURTLE)
                .add(EntityType.ZOGLIN);

        this.tag(ModTags.Entities.VERY_HEAVY)
                .add(EntityType.ELDER_GUARDIAN)
                .add(EntityType.ENDER_DRAGON)
                .add(EntityType.WARDEN)
                .add(EntityType.WITHER);

        this.tag(ModTags.Entities.UNDEAD)
                .add(EntityType.ZOMBIE)
                .add(EntityType.DROWNED)
                .add(EntityType.HUSK)
                .add(EntityType.PHANTOM)
                .add(EntityType.SKELETON)
                .add(EntityType.SKELETON_HORSE)
                .add(EntityType.STRAY)
                .add(EntityType.WITHER)
                .add(EntityType.WITHER_SKELETON)
                .add(EntityType.ZOGLIN)
                .add(EntityType.ZOMBIE)
                .add(EntityType.ZOMBIE_HORSE)
                .add(EntityType.ZOMBIFIED_PIGLIN)
                .add(EntityType.ZOMBIE_VILLAGER);

        this.tag(ModTags.Entities.GHOST)
                .add(EntityType.GHAST)
                .add(EntityType.PHANTOM)
                .add(EntityType.WITHER);

        this.tag(ModTags.Entities.FIRE)
                .add(EntityType.BLAZE)
                .add(EntityType.GHAST)
                .add(EntityType.HOGLIN)
                .add(EntityType.HUSK)
                .add(EntityType.MAGMA_CUBE)
                .add(EntityType.PIGLIN)
                .add(EntityType.PIGLIN_BRUTE)
                .add(EntityType.STRIDER)
                .add(EntityType.WITHER)
                .add(EntityType.WITHER_SKELETON)
                .add(EntityType.ENDERMAN);
    }
}
