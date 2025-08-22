package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.block.ThermolithBlock;
import top.ribs.scguns.client.screen.widget.ThermolithMenu;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThermolithBlockEntity extends BlockEntity implements MenuProvider {
    private static final ResourceLocation THERMOLITH_INGREDIENT_TAG = new ResourceLocation("scguns", "thermolith_ingredient");
    private static final ResourceLocation MELTABLE_BLOCKS_TAG = new ResourceLocation("scguns", "meltable_blocks");
    private static final int MELT_INTERVAL = 40;
    private static final double MELT_RADIUS = 6.0;
    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::create);

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateLitState();
        }
    };
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private int tickCounter = 0;
    private boolean isLit = false;
    public ThermolithBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THERMOLITH.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        updateLitState();

        if (isLit) {
            tickCounter++;
            if (tickCounter >= MELT_INTERVAL) {
                tickCounter = 0;
                meltRandomBlock();
            }
        } else {
            tickCounter = 0;
        }
    }

    private void meltRandomBlock() {
        RandomSource rand = RANDOM.get();
        BlockPos startPos = findNearestLava();
        if (startPos == null) {
            startPos = worldPosition;
        }

        for (int radius = 1; radius <= MELT_RADIUS; radius++) {
            List<BlockPos> candidatesAtRadius = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                            BlockPos targetPos = startPos.offset(dx, dy, dz);

                            if (worldPosition.distSqr(targetPos) <= MELT_RADIUS * MELT_RADIUS
                                    && level.getBlockState(targetPos).is(BlockTags.create(MELTABLE_BLOCKS_TAG))) {
                                candidatesAtRadius.add(targetPos);
                            }
                        }
                    }
                }
            }
            if (!candidatesAtRadius.isEmpty()) {
                BlockPos targetPos = candidatesAtRadius.get(rand.nextInt(candidatesAtRadius.size()));

                level.setBlockAndUpdate(targetPos, Blocks.LAVA.defaultBlockState());

                if (rand.nextFloat() < 0.15f) {
                    itemHandler.extractItem(0, 1, false);
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            10, 0.5, 0.5, 0.5, 0.1);
                }
                return;
            }
        }
    }

    private BlockPos findNearestLava() {
        for (int radius = 1; radius <= MELT_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                            BlockPos checkPos = worldPosition.offset(dx, dy, dz);
                            if (level.getBlockState(checkPos).getBlock() == Blocks.LAVA) {
                                return checkPos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void updateLitState() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        boolean shouldBeLit = !stack.isEmpty() && stack.is(ItemTags.create(THERMOLITH_INGREDIENT_TAG));
        if (isLit != shouldBeLit) {
            isLit = shouldBeLit;
            level.setBlock(worldPosition, getBlockState().setValue(ThermolithBlock.LIT, isLit), Block.UPDATE_ALL);
        }
    }
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.thermolith");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ThermolithMenu(id, playerInventory, this);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        isLit = tag.getBoolean("isLit");
        tickCounter = tag.getInt("tickCounter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putBoolean("isLit", isLit);
        tag.putInt("tickCounter", tickCounter);
        super.saveAdditional(tag);
    }

    public int getContainerSize() {
        return itemHandler.getSlots();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    public boolean isLit() {
        return isLit;
    }
}