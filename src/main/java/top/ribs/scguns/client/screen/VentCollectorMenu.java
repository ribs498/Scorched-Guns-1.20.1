package top.ribs.scguns.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import top.ribs.scguns.blockentity.VentCollectorBlockEntity;
import top.ribs.scguns.init.ModTags;

import java.util.Objects;

public class VentCollectorMenu extends AbstractContainerMenu {
    private final VentCollectorBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;
    private final ContainerData data;

    public VentCollectorMenu(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory,
                (VentCollectorBlockEntity) Objects.requireNonNull(Minecraft.getInstance().level.getBlockEntity(extraData.readBlockPos())),
                new SimpleContainerData(1));
    }

    public VentCollectorMenu(int windowId, Inventory playerInventory, VentCollectorBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.VENT_COLLECTOR_MENU.get(), windowId);
        checkContainerSize(playerInventory, 4);
        this.blockEntity = entity;
        this.itemHandler = this.blockEntity.getItemHandler();
        this.data = data;

        // Filter slot
        this.addSlot(new SlotItemHandler(itemHandler, 0, 80, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.WEAK_FILTER) || stack.is(ModTags.Items.STRONG_FILTER);
            }
        });

        // Output slots (read-only)
        this.addSlot(new SlotItemHandler(itemHandler, 1, 62, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(itemHandler, 2, 80, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(itemHandler, 3, 98, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public int getFilterCharge() {
        return data.get(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}