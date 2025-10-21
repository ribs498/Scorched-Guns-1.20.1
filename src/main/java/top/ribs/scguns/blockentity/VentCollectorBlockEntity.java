package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.VentBlock;
import top.ribs.scguns.block.VentCollectorBlock;
import top.ribs.scguns.client.screen.VentCollectorMenu;
import top.ribs.scguns.common.VentCollectorConfig;
import top.ribs.scguns.common.VentManager;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.Random;

public class VentCollectorBlockEntity extends BlockEntity implements MenuProvider {
    private static final ResourceLocation DEFAULT_CONFIG_ID = new ResourceLocation(Reference.MOD_ID, "vent_collector");

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
                return isValidFilterItem(stack);
            }
            return slot > 0;
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == 0) {
                return super.insertItem(slot, stack, simulate);
            }
            if (slot > 0) {
                return super.insertItem(slot, stack, simulate);
            }
            return stack;
        }
    };

    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private int productionCounter;
    private int currentTickInterval;
    private int filterCharge;
    private final Random random = new Random();

    private VentCollectorConfig config;

    public VentCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VENT_COLLECTOR.get(), pos, state);
        this.productionCounter = 0;
        this.currentTickInterval = 100;
        this.filterCharge = 0;
        reloadConfig();
    }

    public void reloadConfig() {
        this.config = VentManager.getVentCollectorConfig(DEFAULT_CONFIG_ID);
        if (this.config == null) {
            this.config = new VentCollectorConfig();
        }
    }

    public boolean isValidFilterItem(ItemStack stack) {
        if (this.config == null) return false;

        for (VentCollectorConfig.Filters.FilterItem filterItem : this.config.getFilters().getFilterItems()) {
            if (filterItem.isTag()) {
                ResourceLocation tagLocation = filterItem.getIdentifier();
                if (stack.is(net.minecraft.tags.TagKey.create(
                        net.minecraft.core.registries.Registries.ITEM, tagLocation))) {
                    return true;
                }
            } else {
                ResourceLocation itemLocation = filterItem.getIdentifier();
                net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemLocation);
                if (item != null && stack.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getFilterCharge() {
        return this.filterCharge;
    }

    public int getMaxFilterCharge() {
        if (this.config == null) return 64;
        return this.config.getFilters().getMaxCharge();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VentCollectorBlockEntity blockEntity) {
        if (!level.isClientSide) {
            BlockState belowState = level.getBlockState(pos.below());

            if (!(belowState.getBlock() instanceof VentBlock ventBlock)) {
                return;
            }

            boolean isActive = belowState.getValue(VentBlock.ACTIVE);

            if (!isActive) {
                return;
            }

            if (blockEntity.filterCharge <= 0) {
                return;
            }

            int ventPower = belowState.getValue(VentBlock.VENT_POWER);

            float speedMultiplier = blockEntity.getSpeedMultiplier(ventPower);
            blockEntity.productionCounter += (int) speedMultiplier;

            if (blockEntity.productionCounter >= blockEntity.currentTickInterval) {
                blockEntity.productionCounter = 0;

                blockEntity.currentTickInterval = blockEntity.calculateNextTickInterval(ventBlock);

                if (!ventBlock.shouldProduce(level.random)) {
                    blockEntity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    return;
                }
                ItemStack producedItem = ventBlock.selectRandomOutput(level.random);

                if (producedItem.isEmpty()) {
                    return;
                }
                boolean produced = blockEntity.insertProducedItem(producedItem);

                if (produced) {
                    if (blockEntity.shouldConsumeFilter()) {
                        blockEntity.filterCharge--;
                    }
                }

                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }

            if (blockEntity.filterProcessCooldown > 0) {
                blockEntity.filterProcessCooldown--;
            } else {
                blockEntity.processFilterItem();
                blockEntity.filterProcessCooldown = blockEntity.getFilterProcessCooldown();
            }

            if (blockEntity.pushCooldown > 0) {
                blockEntity.pushCooldown--;
            } else {
                blockEntity.pushItemsToAdjacentInventories(level, pos);
                blockEntity.pushCooldown = blockEntity.getPushCooldown();
            }
        }
    }

    private float getSpeedMultiplier(int ventPower) {
        if (this.config == null) return 1 + (ventPower - 1) * 0.35f;
        float multiplier = this.config.getProcessing().getPowerSpeedMultiplier();
        return 1 + (ventPower - 1) * multiplier;
    }

    private boolean shouldConsumeFilter() {
        if (this.config == null) return random.nextFloat() < 0.5f;
        return random.nextFloat() < this.config.getFilters().getConsumptionChance();
    }

    private int getFilterProcessCooldown() {
        if (this.config == null) return 2;
        return this.config.getFilters().getProcessCooldown();
    }

    private int getPushCooldown() {
        if (this.config == null) return 5;
        return this.config.getProcessing().getPushCooldown();
    }

    private int calculateNextTickInterval(VentBlock ventBlock) {
        if (ventBlock.config == null) {
            ventBlock.reloadConfig();
            if (ventBlock.config == null) return 100;
        }
        return ventBlock.config.getPower().getBaseTickInterval() +
                random.nextInt(ventBlock.config.getPower().getTickWiggleRoom());
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
        if (filterStack.isEmpty() || this.config == null) {
            return;
        }

        int maxCharge = this.config.getFilters().getMaxCharge();
        if (filterCharge >= maxCharge) {
            return;
        }

        for (VentCollectorConfig.Filters.FilterItem filterItem : this.config.getFilters().getFilterItems()) {
            boolean matches = false;

            if (filterItem.isTag()) {
                ResourceLocation tagLocation = filterItem.getIdentifier();
                matches = filterStack.is(net.minecraft.tags.TagKey.create(
                        net.minecraft.core.registries.Registries.ITEM, tagLocation));
            } else {
                ResourceLocation itemLocation = filterItem.getIdentifier();
                net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemLocation);
                matches = item != null && filterStack.is(item);
            }

            if (matches) {
                int chargeToAdd = filterItem.getChargeAmount();
                int chargeNeeded = maxCharge - filterCharge;

                if (chargeNeeded >= chargeToAdd) {
                    filterCharge += chargeToAdd;
                    filterStack.shrink(1);
                    setChanged();
                    return;
                }
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