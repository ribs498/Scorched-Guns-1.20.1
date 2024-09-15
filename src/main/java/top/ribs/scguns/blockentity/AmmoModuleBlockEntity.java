package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.ribs.scguns.block.AmmoModuleBlock;
import top.ribs.scguns.init.ModBlockEntities;

public class AmmoModuleBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openersCounter;
    private int transferCooldown = 0;
    private static final int TRANSFER_COOLDOWN = 3;
    public AmmoModuleBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AMMO_MODULE.get(), pPos, pBlockState);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level p_155062_, BlockPos p_155063_, BlockState p_155064_) {
                AmmoModuleBlockEntity.this.playSound(p_155064_, SoundEvents.BARREL_OPEN);
            }

            protected void onClose(Level p_155072_, BlockPos p_155073_, BlockState p_155074_) {
                AmmoModuleBlockEntity.this.playSound(p_155074_, SoundEvents.BARREL_CLOSE);
            }

            protected void openerCountChanged(Level p_155066_, BlockPos p_155067_, BlockState p_155068_, int p_155069_, int p_155070_) {
            }

            protected boolean isOwnContainer(Player p_155060_) {
                if (p_155060_.containerMenu instanceof ChestMenu) {
                    Container $$1 = ((ChestMenu)p_155060_.containerMenu).getContainer();
                    return $$1 == AmmoModuleBlockEntity.this;
                } else {
                    return false;
                }
            }
        };
    }
    public static void tick(Level level, BlockPos pos, BlockState state, AmmoModuleBlockEntity ammoModule) {
        if (level.isClientSide()) return;

        if (ammoModule.transferCooldown > 0) {
            ammoModule.transferCooldown--;
            return;
        }
        Direction facing = state.getValue(AmmoModuleBlock.FACING);
        Direction targetDirection = facing.getOpposite();
        BlockPos adjacentPos = pos.relative(targetDirection);
        BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
        if (adjacentEntity != null) {
            adjacentEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                transferItemToInventory(ammoModule, handler);
            });
        }
        ammoModule.transferCooldown = TRANSFER_COOLDOWN;
    }
    private static void transferItemToInventory(AmmoModuleBlockEntity ammoModule, IItemHandler handler) {
        for (int i = 0; i < ammoModule.getContainerSize(); i++) {
            ItemStack stack = ammoModule.getItem(i);
            if (!stack.isEmpty()) {
                ItemStack singleItemStack = stack.split(1);
                ItemStack remainingStack = TransferHelper.transferItemToInventory(handler, singleItemStack);
                if (!remainingStack.isEmpty()) {
                    stack.grow(remainingStack.getCount());
                }
                ammoModule.setItem(i, stack);
                ammoModule.setChanged();
                break;
            }
        }
    }
    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.ammo_module");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return ChestMenu.threeRows(id, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }
    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }
    void playSound(BlockState state, SoundEvent sound) {
        Vec3i directionVec = state.getValue(AmmoModuleBlock.FACING).getNormal();
        double x = this.worldPosition.getX() + 0.5 + directionVec.getX() / 2.0;
        double y = this.worldPosition.getY() + 0.5 + directionVec.getY() / 2.0;
        double z = this.worldPosition.getZ() + 0.5 + directionVec.getZ() / 2.0;
        this.level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TransferCooldown", this.transferCooldown);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.transferCooldown = tag.getInt("TransferCooldown");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    public IItemHandlerModifiable getItemStackHandler() {
        return new IItemHandlerModifiable() {
            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                AmmoModuleBlockEntity.this.items.set(slot, stack);
            }

            @Override
            public int getSlots() {
                return AmmoModuleBlockEntity.this.items.size();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return AmmoModuleBlockEntity.this.items.get(slot);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return AmmoModuleBlockEntity.this.items.get(slot);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return true;
            }
        };
    }
    public static class TransferHelper {
        public static ItemStack transferItemToInventory(IItemHandler inventory, ItemStack stack) {
            ItemStack remainingStack = stack.copy();

            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                remainingStack = inventory.insertItem(slot, remainingStack, false);
                if (remainingStack.isEmpty()) {
                    break;
                }
            }

            return remainingStack;
        }
    }
}
