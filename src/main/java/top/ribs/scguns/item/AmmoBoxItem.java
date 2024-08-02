package top.ribs.scguns.item;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AmmoBoxItem extends Item {
    public static final String TAG_ITEMS = "Items";
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public AmmoBoxItem(Item.Properties properties) {
        super(properties);
    }


    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (stack.getCount() == 1 && action == ClickAction.SECONDARY) {
            ItemStack itemStackInSlot = slot.getItem();
            if (itemStackInSlot.isEmpty()) {
                this.playRemoveOneSound(player);
                removeOne(stack).ifPresent(removedStack -> add(stack, slot.safeInsert(removedStack)));
            } else if (itemStackInSlot.is(ItemTags.create(getAmmoTag()))) {
                int maxInsertCount = getMaxItemCount() - getTotalItemCount(stack);
                int itemsToInsert = Math.min(itemStackInSlot.getCount(), maxInsertCount);
                int insertedItems = add(stack, slot.safeTake(itemStackInSlot.getCount(), itemsToInsert, player));
                if (insertedItems > 0) {
                    this.playInsertSound(player);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
        if (stack.getCount() != 1) {
            return false;
        } else if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (otherStack.isEmpty()) {
                removeOne(stack).ifPresent(removedStack -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(removedStack);
                });
            } else if (otherStack.is(ItemTags.create(getAmmoTag()))) {
                int maxInsertCount = getMaxItemCount() - getTotalItemCount(stack);
                int itemsToInsert = Math.min(otherStack.getCount(), maxInsertCount);
                int insertedItems = add(stack, otherStack.copyWithCount(itemsToInsert));
                if (insertedItems > 0) {
                    this.playInsertSound(player);
                    otherStack.shrink(insertedItems);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getTotalItemCount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + 12 * getTotalItemCount(stack) / getMaxItemCount(), 13);
    }

    protected abstract ResourceLocation getAmmoTag();

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    public static int add(ItemStack pouchStack, ItemStack insertedStack) {
        if (!insertedStack.isEmpty() && insertedStack.is(ItemTags.create(new ResourceLocation("scguns", "ammo")))) {
            CompoundTag compoundTag = pouchStack.getOrCreateTag();
            if (!compoundTag.contains(TAG_ITEMS)) {
                compoundTag.put(TAG_ITEMS, new ListTag());
            }

            int maxItemCount = getMaxItemCount(pouchStack);
            int itemsToInsert = Math.min(insertedStack.getCount(), maxItemCount - getTotalItemCount(pouchStack));

            if (itemsToInsert == 0) {
                return 0;
            }

            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack existingStack = ItemStack.of(itemTag);
                if (ItemStack.isSameItemSameTags(existingStack, insertedStack)) {
                    int remainingSpace = Math.min(existingStack.getMaxStackSize() - existingStack.getCount(), itemsToInsert);
                    existingStack.grow(remainingSpace);
                    itemsToInsert -= remainingSpace;
                    existingStack.save(itemTag);
                    listTag.set(i, itemTag);
                    if (itemsToInsert <= 0) {
                        break;
                    }
                }
            }

            while (itemsToInsert > 0) {
                int countToInsert = Math.min(insertedStack.getMaxStackSize(), itemsToInsert);
                ItemStack newItemStack = insertedStack.copyWithCount(countToInsert);
                CompoundTag newItemTag = new CompoundTag();
                newItemStack.save(newItemTag);
                listTag.add(newItemTag);
                itemsToInsert -= countToInsert;
            }

            compoundTag.put(TAG_ITEMS, listTag);
            return insertedStack.getCount() - itemsToInsert;
        } else {
            return 0;
        }
    }

    public static int getTotalItemCount(ItemStack stack) {
        return getContents(stack).mapToInt(ItemStack::getCount).sum();
    }

    private static Optional<ItemStack> removeOne(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrCreateTag();
        if (!compoundTag.contains(TAG_ITEMS)) {
            return Optional.empty();
        } else {
            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            if (listTag.isEmpty()) {
                return Optional.empty();
            } else {
                CompoundTag itemTag = listTag.getCompound(0);
                ItemStack itemStack = ItemStack.of(itemTag);
                listTag.remove(0);
                if (listTag.isEmpty()) {
                    stack.removeTagKey(TAG_ITEMS);
                }
                return Optional.of(itemStack);
            }
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        NonNullList<ItemStack> nonNullList = NonNullList.create();
        getContents(stack).forEach(nonNullList::add);
        return Optional.of(new BundleTooltip(nonNullList, getTotalItemCount(stack)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        // tooltipComponents.add(Component.translatable("item.scguns.ammo_pouch.fullness").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getContents(itemEntity.getItem()));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public static int getMaxItemCount(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof AmmoBoxItem) {
            return ((AmmoBoxItem) item).getMaxItemCount();
        }
        return 256;
    }

    protected abstract int getMaxItemCount();

    public static Stream<ItemStack> getContents(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag == null) {
            return Stream.empty();
        } else {
            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            return listTag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
        }
    }
}
