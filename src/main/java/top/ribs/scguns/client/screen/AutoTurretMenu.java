package top.ribs.scguns.client.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.blockentity.AutoTurretBlockEntity;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.item.EnemyLogItem;
import top.ribs.scguns.item.TeamLogItem;

public class AutoTurretMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos pos;

    public AutoTurretMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public AutoTurretMenu(int id, Inventory playerInventory, BlockEntity entity) {
        super(ModMenuTypes.AUTO_TURRET_MENU.get(), id);

        if (!(entity instanceof AutoTurretBlockEntity turretBlockEntity)) {
            throw new IllegalStateException("Unexpected BlockEntity type: " + entity);
        }

        this.level = playerInventory.player.level();
        this.pos = entity.getBlockPos();
        ItemStackHandler handler = turretBlockEntity.getItemStackHandler();

        // Add slots for the turret's internal inventory (3x3 grid)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotItemHandler(handler, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }
        this.addSlot(new SlotItemHandler(handler, 9, 134, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof TeamLogItem || stack.getItem() instanceof EnemyLogItem;
            }
        });
        // Add player inventory slots (3x9 grid)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots (1x9)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, pos), player, ModBlocks.AUTO_TURRET.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 10) {
                if (!this.moveItemStackTo(itemstack1, 10, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public AutoTurretBlockEntity getBlockEntity() {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        return blockEntity instanceof AutoTurretBlockEntity turretBlockEntity ? turretBlockEntity : null;
    }
}
