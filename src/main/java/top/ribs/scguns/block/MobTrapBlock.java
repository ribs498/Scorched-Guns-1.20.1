package top.ribs.scguns.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.blockentity.MobTrapBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MobTrapBlock extends BaseEntityBlock {

    public MobTrapBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS; // This will prevent the client from processing the interaction
        }

        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof MobTrapBlockEntity mobTrap) {
            if (heldItem.isEmpty()) {
                List<EntityType<?>> storedMobs = mobTrap.getStoredMobs();
                if (storedMobs.isEmpty()) {
                    player.sendSystemMessage(Component.translatable("mobtrap.empty").withStyle(ChatFormatting.GRAY));
                } else {
                    player.sendSystemMessage(Component.translatable("mobtrap.stored_mobs").withStyle(ChatFormatting.GRAY));
                    Map<EntityType<?>, Integer> mobCounts = new HashMap<>();
                    for (EntityType<?> mob : storedMobs) {
                        mobCounts.put(mob, mobCounts.getOrDefault(mob, 0) + 1);
                    }
                    for (Map.Entry<EntityType<?>, Integer> entry : mobCounts.entrySet()) {
                        player.sendSystemMessage(Component.literal("- " + ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey()).toString() + " x" + entry.getValue()).withStyle(ChatFormatting.GREEN));
                    }
                }
            } else if (heldItem.getItem() instanceof SpawnEggItem eggItem) {
                EntityType<?> entityType = eggItem.getType(null);
                if (mobTrap.addMob(entityType)) {
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("mobtrap.too_many_mobs").withStyle(ChatFormatting.RED));
                }
            } else {
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof Player) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MobTrapBlockEntity mobTrap) {
                mobTrap.releaseMobs((ServerLevel) level, pos);
            }
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MOB_TRAP.get(),
                (lvl, pos, blockState, blockEntity) -> {
                    if (blockEntity instanceof MobTrapBlockEntity) {
                        blockEntity.serverTick(lvl, pos);
                    }
                });
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobTrapBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
