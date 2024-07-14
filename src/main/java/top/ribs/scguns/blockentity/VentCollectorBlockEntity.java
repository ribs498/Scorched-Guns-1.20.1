package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.block.GeothermalVentBlock;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModItems;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VentCollectorBlockEntity extends BlockEntity {
    private static final int BASE_TICK_INTERVAL = 60;
    private static final int TICK_WIGGLE_ROOM = 6;
    private final ItemStackHandler itemHandler = new ItemStackHandler(64) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private int productionCounter;
    private int currentTickInterval;
    private final Random random = new Random();

    public VentCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VENT_COLLECTOR.get(), pos, state);
        this.productionCounter = 0;
        this.currentTickInterval = calculateNextTickInterval();
    }

    private int calculateNextTickInterval() {
        return BASE_TICK_INTERVAL + random.nextInt(TICK_WIGGLE_ROOM);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VentCollectorBlockEntity blockEntity) {
        if (!level.isClientSide) {
            BlockState belowState = level.getBlockState(pos.below());
            boolean isGeothermalVentBelow = belowState.getBlock() instanceof GeothermalVentBlock;
            boolean isActive = isGeothermalVentBelow && belowState.getValue(GeothermalVentBlock.ACTIVE);
            if (isActive) {
                blockEntity.productionCounter++;
                if (blockEntity.productionCounter >= blockEntity.currentTickInterval) {
                    blockEntity.productionCounter = 0;
                    blockEntity.currentTickInterval = blockEntity.calculateNextTickInterval();
                    ItemStack stack = blockEntity.itemHandler.getStackInSlot(0);
                    if (stack.isEmpty() || (stack.getItem() == ModItems.NITER_DUST.get() && stack.getCount() < stack.getMaxStackSize())) {
                        blockEntity.itemHandler.insertItem(0, new ItemStack(ModItems.NITER_DUST.get(), 1), false);
                        blockEntity.setChanged();
                    }
                }
            }
            blockEntity.transferItemsToAdjacentStorage();
        }
    }

    public void transferItemsToAdjacentStorage() {
        for (Direction direction : Direction.values()) {
            assert this.level != null;
            BlockEntity adjacentBlockEntity = this.level.getBlockEntity(this.worldPosition.relative(direction));
            if (adjacentBlockEntity != null) {
                adjacentBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).ifPresent(adjacentHandler -> {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(0);
                    if (!stackInSlot.isEmpty()) {
                        ItemStack remaining = ItemHandlerHelper.insertItem(adjacentHandler, stackInSlot, false);
                        itemHandler.setStackInSlot(0, remaining);
                        setChanged();
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