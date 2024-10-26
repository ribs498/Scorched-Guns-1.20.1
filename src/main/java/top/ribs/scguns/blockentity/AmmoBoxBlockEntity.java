package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.block.AmmoBoxBlock;
import top.ribs.scguns.client.screen.AmmoBoxMenu;
import top.ribs.scguns.init.ModBlockEntities;

public class AmmoBoxBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openersCounter;

    public AmmoBoxBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.AMMO_BOX.get(), pPos, pBlockState);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY); // 27 slots like a chest
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level pLevel, BlockPos pPos, BlockState pState) {
                AmmoBoxBlockEntity.this.playSound(pState, SoundEvents.CHEST_OPEN);
            }

            protected void onClose(Level pLevel, BlockPos pPos, BlockState pState) {
                AmmoBoxBlockEntity.this.playSound(pState, SoundEvents.CHEST_CLOSE);
            }

            protected void openerCountChanged(Level pLevel, BlockPos pPos, BlockState pState, int pOpenerCount, int pNewOpeners) {}

            protected boolean isOwnContainer(Player pPlayer) {
                return pPlayer.containerMenu instanceof ChestMenu && ((ChestMenu) pPlayer.containerMenu).getContainer() == AmmoBoxBlockEntity.this;
            }
        };
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
        return Component.translatable("container.ammo_box");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return ChestMenu.threeRows(id, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27; // 27 slots like a regular chest
    }

    @Override
    public void startOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
            BlockState state = this.getBlockState();
            if (!state.getValue(AmmoBoxBlock.OPEN)) {
                this.getLevel().setBlock(this.worldPosition, state.setValue(AmmoBoxBlock.OPEN, true), 3);
            }
        }
    }

    @Override
    public void stopOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
            BlockState state = this.getBlockState();
            if (state.getValue(AmmoBoxBlock.OPEN)) {
                this.getLevel().setBlock(this.worldPosition, state.setValue(AmmoBoxBlock.OPEN, false), 3);
            }
        }
    }


    void playSound(BlockState state, SoundEvent sound) {
        this.level.playSound(null, this.worldPosition, sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
    }
}
