package top.ribs.scguns.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.blockentity.AmmoBoxBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AmmoBoxBlock extends Block implements EntityBlock {
    public AmmoBoxBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AmmoBoxBlockEntity) {
                Containers.dropContents(world, pos, ((AmmoBoxBlockEntity)blockEntity).getItems());
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && player.isCreative()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AmmoBoxBlockEntity) {
                ((AmmoBoxBlockEntity)blockEntity).clearContent();
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options) {
        super.appendHoverText(stack, world, tooltip, options);
        if (stack.hasTag()) {
            CompoundTag compoundTag = stack.getTagElement("BlockEntityTag");
            if (compoundTag != null) {
                tooltip.add(Component.literal("Contains items").withStyle(ChatFormatting.GRAY));
            }
        }
    }

}
