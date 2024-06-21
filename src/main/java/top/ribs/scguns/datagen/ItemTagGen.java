package top.ribs.scguns.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.ribs.scguns.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ItemTagGen extends ItemTagsProvider
{
    public static Map<ResourceLocation, TagKey<Block>> blockTagCache = new HashMap<>();
    public static Map<ResourceLocation, TagKey<Item>> itemTagCache = new HashMap<>();

    public ItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockTagProvider, Reference.MOD_ID, existingFileHelper);
    }

    public static TagKey<Block> getBlockTag(ResourceLocation resourceLocation) {
        if (!blockTagCache.containsKey(resourceLocation)) {
            blockTagCache.put(resourceLocation, BlockTags.create(resourceLocation));
        }
        return blockTagCache.get(resourceLocation);
    }

    public static TagKey<Item> getItemTag(ResourceLocation resourceLocation) {
        if (!itemTagCache.containsKey(resourceLocation)) {
            itemTagCache.put(resourceLocation, ItemTags.create(resourceLocation));
        }
        return itemTagCache.get(resourceLocation);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
    }
}
