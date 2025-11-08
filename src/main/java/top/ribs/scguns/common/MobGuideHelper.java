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
import java.util.ArrayList;
import java.util.List;

public class MobGuideHelper {
    private static final int MAX_CHARS_PER_PAGE = 256;
    private static final int MAX_LINES_PER_PAGE = 14;

    /**
     * Creates a written book ItemStack from a mob guide configuration
     */
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
            String pageText = textComponent.getString();

            List<String> splitPages = splitTextIntoPages(pageText);

            for (String splitPage : splitPages) {
                Component splitComponent = Component.literal(splitPage);
                String jsonText = Component.Serializer.toJson(splitComponent);
                pages.add(StringTag.valueOf(jsonText));
            }
        }

        tag.put("pages", pages);
        tag.putBoolean("resolved", true);

        return book;
    }


    private static List<String> splitTextIntoPages(String text) {
        List<String> result = new ArrayList<>();

        String[] lines = text.split("\n");

        StringBuilder currentPage = new StringBuilder();
        int currentLines = 0;
        int currentChars = 0;

        for (String line : lines) {
            int visibleLength = getVisibleLength(line);
            int totalLength = line.length();

            int estimatedLines = Math.max(1, (visibleLength / 19) + 1);

            if (currentLines + estimatedLines > MAX_LINES_PER_PAGE ||
                    currentChars + totalLength > MAX_CHARS_PER_PAGE) {

                if (!currentPage.isEmpty()) {
                    result.add(currentPage.toString());
                    currentPage = new StringBuilder();
                    currentLines = 0;
                    currentChars = 0;
                }
            }

            if (!currentPage.isEmpty()) {
                currentPage.append("\n");
                currentChars++;
            }
            currentPage.append(line);
            currentLines += estimatedLines;
            currentChars += totalLength;
        }

        if (!currentPage.isEmpty()) {
            result.add(currentPage.toString());
        }

        return result.isEmpty() ? List.of("") : result;
    }

    /**
     * Calculate visible length of string (excluding formatting codes)
     */
    private static int getVisibleLength(String text) {
        int length = 0;
        boolean inFormatCode = false;

        for (char c : text.toCharArray()) {
            if (c == '§') {
                inFormatCode = true;
            } else if (inFormatCode) {
                inFormatCode = false;
            } else {
                length++;
            }
        }

        return length;
    }
}