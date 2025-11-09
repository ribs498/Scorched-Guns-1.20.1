package top.ribs.scguns.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.ribs.scguns.config.MobGuideConfig;

import javax.annotation.Nullable;

public class MobGuideHelper {

    @Nullable
    public static ItemStack createGuideBook(EntityType<?> entityType) {
        MobGuideConfig.MobGuide guide = MobGuideConfig.getGuide(entityType);
        if (guide == null) {
            return null;
        }

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();

        tag.putString("title", guide.getTitle().getString());

        String authorKey = guide.titleKey().replace(".title", ".author");
        Component authorComponent = Component.translatable(authorKey);
        tag.putString("author", authorComponent.getString());

        tag.putInt("generation", 0);

        ListTag pages = new ListTag();

        for (MobGuideConfig.GuidePage page : guide.getPages()) {
            Component textComponent = page.getTextComponent();
            String jsonText = Component.Serializer.toJson(textComponent);
            pages.add(StringTag.valueOf(jsonText));
        }

        tag.put("pages", pages);
        tag.putBoolean("resolved", false);

        return book;
    }
}