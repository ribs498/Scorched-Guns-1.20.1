package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.block.GeothermalVentBlock;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModItems;

import javax.annotation.Nullable;

public class VentCollectorBlockEntity extends BlockEntity {
    private final ItemStackHandler itemHandler = new ItemStackHandler(64) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final float productionSpeed;
    private int productionCounter;

    public VentCollectorBlockEntity(BlockPos pos, BlockState state, float productionSpeed) {
        super(ModBlockEntities.VENT_COLLECTOR.get(), pos, state);
        this.productionSpeed = productionSpeed;
        this.productionCounter = 0;
    }

    public void tick() {
        assert this.level != null;
        if (!this.level.isClientSide) {
            BlockState belowState = this.level.getBlockState(this.worldPosition.below());
            boolean isGeothermalVentBelow = belowState.getBlock() instanceof GeothermalVentBlock;
            boolean isActive = isGeothermalVentBelow && belowState.getValue(GeothermalVentBlock.ACTIVE);
            if (isActive) {
                productionCounter++;
                if (productionCounter >= (int) (20 / productionSpeed)) {
                    productionCounter = 0;
                    ItemStack stack = itemHandler.getStackInSlot(0);
                    if (stack.isEmpty() || (stack.getItem() == ModItems.NITER_DUST.get() && stack.getCount() < stack.getMaxStackSize())) {
                        itemHandler.insertItem(0, new ItemStack(ModItems.NITER_DUST.get(), 1), false);
                        setChanged();
                    }
                }
            }
            BlockPos northPos = this.worldPosition.relative(Direction.NORTH);
            BlockEntity northBlockEntity = this.level.getBlockEntity(northPos);
            if (northBlockEntity != null) {
                northBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.SOUTH).ifPresent(northHandler -> {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(0);
                    if (!stackInSlot.isEmpty()) {
                        ItemStack remaining = northHandler.insertItem(0, stackInSlot, false);
                        itemHandler.setStackInSlot(0, remaining);
                    }
                });
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
    }
}
