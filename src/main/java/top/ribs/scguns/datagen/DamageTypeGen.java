package top.ribs.scguns.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModDamageTypes;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public class DamageTypeGen extends TagsProvider<DamageType>
{
    public DamageTypeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.tag(DamageTypeTags.IS_PROJECTILE).add(ModDamageTypes.BULLET);



    }
}
