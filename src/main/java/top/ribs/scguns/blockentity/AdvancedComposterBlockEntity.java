package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.block.AdvancedComposterBlock;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;


public class AdvancedComposterBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT_START = 1;
    private static final int OUTPUT_SLOT_COUNT = 3;
    private static final int TOTAL_SLOTS = OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT;

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    private static final int[] SLOTS_FOR_UP = new int[]{INPUT_SLOT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{OUTPUT_SLOT_START, OUTPUT_SLOT_START + 1, OUTPUT_SLOT_START + 2};
    private static final int[] SLOTS_FOR_SIDES = new int[]{INPUT_SLOT};

    private static final int MAX_COMPOST_TIME = 300;
    private int compostTime = 0;
    private boolean isComposting = false;

    public AdvancedComposterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_COMPOSTER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            if (state.getValue(AdvancedComposterBlock.LEVEL) == 7 && isComposting) {
                compostTime++;
                if (compostTime >= MAX_COMPOST_TIME) {
                    completeComposting(level, pos, state);
                }
                setChanged();
            }
        }
    }

    public void startComposting() {
        if (!isComposting) {
            this.compostTime = 0;
            this.isComposting = true;
            setChanged();
        }
    }

    private void completeComposting(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AdvancedComposterBlock.LEVEL, 8), 3);
        level.playSound(null, pos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.isComposting = false;
        createOutputItems();
        setChanged();
    }

    private void createOutputItems() {
        List<Item> possibleDrops = new ArrayList<>();
        Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(ModTags.Items.COMPOST_DROPS).forEach(possibleDrops::add);
        if (possibleDrops.isEmpty()) {
            return;
        }
        Random random = new Random();
        int totalItems = random.nextInt(3) + 1;
        List<ItemStack> output = new ArrayList<>();
        for (int i = 0; i < totalItems; i++) {
            Item item = possibleDrops.get(random.nextInt(possibleDrops.size()));
            output.add(new ItemStack(item, 1));
        }
        for (int slot = OUTPUT_SLOT_START; slot < TOTAL_SLOTS && !output.isEmpty(); slot++) {
            if (items.get(slot).isEmpty()) {
                items.set(slot, output.remove(0));
            }
        }

        setChanged();
    }

    public boolean extractOneItem(Player player) {
        for (int slot = OUTPUT_SLOT_START; slot < TOTAL_SLOTS; slot++) {
            ItemStack stack = removeItem(slot, 1);
            if (!stack.isEmpty()) {
                if (player != null) {
                    player.getInventory().add(stack);
                } else {
                    assert level != null;
                    Block.popResource(level, worldPosition.above(), stack);
                }
                setChanged();
                return true;
            }
        }
        return false;
    }

    public int getVisualLevel() {
        if (isOutputEmpty()) {
            return 0;
        } else {
            int filledSlots = (int) IntStream.range(OUTPUT_SLOT_START, TOTAL_SLOTS)
                    .filter(slot -> !items.get(slot).isEmpty())
                    .count();
            return Math.max(8, 11 - filledSlots);
        }
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        ItemStack stack = ContainerHelper.removeItem(items, index, count);
        if (!stack.isEmpty() && index >= OUTPUT_SLOT_START) {
            setChanged();
            if (level != null) {
                level.setBlock(worldPosition, getBlockState().setValue(AdvancedComposterBlock.LEVEL, getVisualLevel()), 3);
            }
        }
        return stack;
    }

    public boolean isOutputEmpty() {
        for (int slot = OUTPUT_SLOT_START; slot < TOTAL_SLOTS; slot++) {
            if (!items.get(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(items, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        if (index == INPUT_SLOT && !stack.isEmpty()) {
            tryCompostItem(stack);
        }
        setChanged();
    }

    private void tryCompostItem(ItemStack stack) {
        BlockState state = this.getBlockState();
        int currentLevel = state.getValue(AdvancedComposterBlock.LEVEL);
        if (currentLevel < 7 && state.getBlock() instanceof AdvancedComposterBlock composterBlock && composterBlock.isCompostable(stack)) {
            BlockState newState = composterBlock.addItem(null, state, level, worldPosition, stack);
            level.setBlock(worldPosition, newState, 3);
            stack.shrink(1);
            composterBlock.playComposterEffects(level, worldPosition, newState);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return index == INPUT_SLOT && canPlaceItem(index, itemStack) && this.getBlockState().getValue(AdvancedComposterBlock.LEVEL) < 7;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index >= OUTPUT_SLOT_START && index < TOTAL_SLOTS && direction == Direction.DOWN && !stack.isEmpty();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == INPUT_SLOT && getBlockState().getBlock() instanceof AdvancedComposterBlock composterBlock && composterBlock.isCompostable(stack);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.compostTime = nbt.getInt("CompostTime");
        this.isComposting = nbt.getBoolean("IsComposting");
        ContainerHelper.loadAllItems(nbt, this.items);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("CompostTime", this.compostTime);
        nbt.putBoolean("IsComposting", this.isComposting);
        ContainerHelper.saveAllItems(nbt, this.items);
    }

    public void drops() {
        for (ItemStack item : items) {
            assert level != null;
            Block.popResource(level, worldPosition, item);
        }
    }
}