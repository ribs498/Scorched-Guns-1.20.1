package top.ribs.scguns.blockentity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.BasicTurretBlock;
import top.ribs.scguns.block.TurretTargetingBlock;
import top.ribs.scguns.client.screen.BasicTurretMenu;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BasicTurretBlockEntity extends TurretBlockEntity {
    private static final ResourceLocation TURRET_ID = new ResourceLocation(Reference.MOD_ID, "basic_turret");

    public BasicTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_TURRET.get(), pos, state, TURRET_ID);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.basic_turret");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        boolean hasTargetingModule = false;
        if (this.level != null) {
            for (Direction direction : Direction.values()) {
                BlockState blockState = this.level.getBlockState(this.worldPosition.relative(direction));
                if (blockState.getBlock() instanceof TurretTargetingBlock) {
                    hasTargetingModule = true;
                    break;
                }
            }
        }

        if (!hasTargetingModule) {
            if (this.level != null && !this.level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.scguns.turret_needs_targeting_module")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return null;
        }

        return new BasicTurretMenu(id, playerInventory, this);
    }

    @Override
    protected boolean isPowered(BlockState state) {
        return state.getValue(BasicTurretBlock.POWERED);
    }
}