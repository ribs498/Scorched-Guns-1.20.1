package top.ribs.scguns.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import top.ribs.scguns.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ModTags
{
    public static Map<ResourceLocation, TagKey<Block>> blockTagCache = new HashMap<>();

    public static class Blocks
    {
        public static final TagKey<Block> FRAGILE = tag("fragile");

        private static TagKey<Block> tag(String name)
        {
            return BlockTags.create(new ResourceLocation(Reference.MOD_ID, name));
        }
    }

    public static class Entities
    {
        public static final TagKey<EntityType<?>> NONE = tag("none");
        public static final TagKey<EntityType<?>> HEAVY = tag("heavy");
        public static final TagKey<EntityType<?>> VERY_HEAVY = tag("very_heavy");
        public static final TagKey<EntityType<?>> UNDEAD = tag("undead");
        public static final TagKey<EntityType<?>> GHOST = tag("ghost");
        public static final TagKey<EntityType<?>> FIRE = tag("fire");

        public static TagKey<EntityType<?>> tag(String name)
        {
            return TagKey.create(Registries.ENTITY_TYPE,new ResourceLocation(Reference.MOD_ID, name));
        }
    }

    public static TagKey<Block> getBlockTag(String name) {
        return getBlockTag(new ResourceLocation(Reference.MOD_ID, name));
    }

    public static TagKey<Block> getBlockTag(ResourceLocation resourceLocation) {
        if (!blockTagCache.containsKey(resourceLocation)) {
            blockTagCache.put(resourceLocation, BlockTags.create(resourceLocation));
        }
        return blockTagCache.get(resourceLocation);
    }
}
