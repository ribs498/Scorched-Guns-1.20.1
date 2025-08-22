package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
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
import top.ribs.scguns.block.SulfurVentBlock;
import top.ribs.scguns.block.VentCollectorBlock;
import top.ribs.scguns.client.screen.VentCollectorMenu;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class VentCollectorBlockEntity extends BlockEntity implements MenuProvider {
    private static final int BASE_TICK_INTERVAL = 100;
    private static final int TICK_WIGGLE_ROOM = 60;
    private static final float POWER_SPEED_MULTIPLIER = 0.35f;
    private static final int MAX_FILTER_CHARGE = 64;
    private static final int WEAK_FILTER_CHARGE = 4;
    private static final int STRONG_FILTER_CHARGE = 8;
    private static final float FILTER_CONSUMPTION_CHANCE = 0.5f;
    private static final int FILTER_PROCESS_COOLDOWN = 2;
    private static final int PUSH_COOLDOWN = 5;
    private int pushCooldown = 0;
    private int filterProcessCooldown = 0;
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == 0) {
                processFilterItem();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.is(ModTags.Items.WEAK_FILTER) || stack.is(ModTags.Items.STRONG_FILTER);
            }
            return slot > 0 && (stack.is(ModTags.Items.GEOTHERMAL_VENT_OUTPUT) || stack.is(ModTags.Items.SULFUR_VENT_OUTPUT));
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot > 0 && (stack.is(ModTags.Items.GEOTHERMAL_VENT_OUTPUT) || stack.is(ModTags.Items.SULFUR_VENT_OUTPUT))) {
                return super.insertItem(slot, stack, simulate);
            }
            return slot == 0 ? super.insertItem(slot, stack, simulate) : stack;
        }

    };

    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private int productionCounter;
    private int currentTickInterval;
    private int filterCharge;
    private final Random random = new Random();

    public VentCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VENT_COLLECTOR.get(), pos, state);
        this.productionCounter = 0;
        this.currentTickInterval = calculateNextTickInterval();
        this.filterCharge = 0;
    }

    public int getFilterCharge() {
        return this.filterCharge;
    }

    private int calculateNextTickInterval() {
        return BASE_TICK_INTERVAL + random.nextInt(TICK_WIGGLE_ROOM);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VentCollectorBlockEntity blockEntity) {
        if (!level.isClientSide) {
            BlockState belowState = level.getBlockState(pos.below());
            boolean isGeothermalVentBelow = belowState.getBlock() instanceof GeothermalVentBlock;
            boolean isSulfurVentBelow = belowState.getBlock() instanceof SulfurVentBlock;
            boolean isActive = (isGeothermalVentBelow && belowState.getValue(GeothermalVentBlock.ACTIVE)) ||
                    (isSulfurVentBelow && belowState.getValue(SulfurVentBlock.ACTIVE));

            if (isActive && blockEntity.filterCharge > 0) {
                int ventPower = 1;
                if (isGeothermalVentBelow) {
                    ventPower = belowState.getValue(GeothermalVentBlock.VENT_POWER);
                } else {
                    ventPower = belowState.getValue(SulfurVentBlock.VENT_POWER);
                }

                float speedMultiplier = 1 + (ventPower - 1) * POWER_SPEED_MULTIPLIER;
                blockEntity.productionCounter += (int) speedMultiplier;

                if (blockEntity.productionCounter >= blockEntity.currentTickInterval) {
                    blockEntity.productionCounter = 0;
                    blockEntity.currentTickInterval = blockEntity.calculateNextTickInterval();

                    boolean produced;
                    if (isGeothermalVentBelow) {
                        produced = blockEntity.produceFromTag(ModTags.Items.GEOTHERMAL_VENT_OUTPUT);
                    } else {
                        produced = blockEntity.produceFromTag(ModTags.Items.SULFUR_VENT_OUTPUT);
                    }

                    if (produced && blockEntity.random.nextFloat() < FILTER_CONSUMPTION_CHANCE) {
                        blockEntity.filterCharge--;
                    }

                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }

            if (blockEntity.filterProcessCooldown > 0) {
                blockEntity.filterProcessCooldown--;
            } else {
                blockEntity.processFilterItem();
                blockEntity.filterProcessCooldown = FILTER_PROCESS_COOLDOWN;
            }

            if (blockEntity.pushCooldown > 0) {
                blockEntity.pushCooldown--;
            } else {
                blockEntity.pushItemsToAdjacentInventories(level, pos);
                blockEntity.pushCooldown = PUSH_COOLDOWN;
            }
        }
    }

    private boolean produceFromTag(net.minecraft.tags.TagKey<Item> tag) {
        List<Item> tagItems = new java.util.ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            tagItems.add(holder.value());
        }

        if (tagItems.isEmpty()) {
            return false;
        }
        Item selectedItem = tagItems.get(random.nextInt(tagItems.size()));
        ItemStack producedItem = new ItemStack(selectedItem, 1);

        return insertProducedItem(producedItem);
    }

    private boolean insertProducedItem(ItemStack producedItem) {
        for (int i = 1; i <= 3; i++) {
            ItemStack remaining = itemHandler.insertItem(i, producedItem, false);
            if (remaining.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void processFilterItem() {
        ItemStack filterStack = itemHandler.getStackInSlot(0);
        if (!filterStack.isEmpty()) {
            int chargeToAdd = 0;
            if (filterStack.is(ModTags.Items.WEAK_FILTER)) {
                chargeToAdd = WEAK_FILTER_CHARGE;
            } else if (filterStack.is(ModTags.Items.STRONG_FILTER)) {
                chargeToAdd = STRONG_FILTER_CHARGE;
            }

            int chargeNeeded = MAX_FILTER_CHARGE - filterCharge;
            if (chargeToAdd > 0 && chargeNeeded >= chargeToAdd) {
                filterCharge += chargeToAdd;
                filterStack.shrink(1);
                setChanged();
            }
        }
    }

    private void pushItemsToAdjacentInventories(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof VentCollectorBlock)) {
            return;
        }

        Direction facing = state.getValue(VentCollectorBlock.FACING);
        boolean isConnected = state.getValue(VentCollectorBlock.ATTACHED);

        if (!isConnected) {
            return;
        }

        BlockPos adjacentPos = pos.relative(facing);
        BlockEntity adjacentEntity = level.getBlockEntity(adjacentPos);
        if (adjacentEntity != null) {
            IItemHandler adjacentHandler = adjacentEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).orElse(null);
            for (int i = 1; i <= 3; i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    ItemStack singleItem = stack.copy();
                    singleItem.setCount(1);
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(adjacentHandler, singleItem, false);
                    if (remaining.isEmpty()) {
                        itemHandler.extractItem(i, 1, false);
                        setChanged();
                        return;
                    }
                }
            }
        }
    }

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? VentCollectorBlockEntity.this.filterCharge : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                VentCollectorBlockEntity.this.filterCharge = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.scguns.vent_collector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new VentCollectorMenu(windowId, playerInventory, this, this.data);
    }

    public ContainerData getData() {
        return this.data;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FilterCharge", filterCharge);
        tag.putInt("PushCooldown", pushCooldown);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        filterCharge = tag.getInt("FilterCharge");
        pushCooldown = tag.getInt("PushCooldown");
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
}