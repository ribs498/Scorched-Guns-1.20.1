package top.ribs.scguns.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.blockentity.CryoniterBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.StreamSupport;

public class CryoniterBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private static final ResourceLocation CRYONITER_INGREDIENT_TAG = new ResourceLocation("scguns", "cryoniter_ingredient");

    public CryoniterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CryoniterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CryoniterBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (MenuProvider) blockEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CryoniterBlockEntity cryoniterBlockEntity) {
                cryoniterBlockEntity.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CRYONITER.get(),
                (level1, pos, state1, blockEntity) -> ((CryoniterBlockEntity) blockEntity).tick());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && level.isClientSide) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;

            if (random.nextDouble() < 0.3) {
                level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, -0.1, 0.0);
            }

            for (Direction direction : Direction.values()) {
                if (direction != Direction.UP) {
                    BlockPos relativePos = pos.relative(direction);
                    if (!level.getBlockState(relativePos).isSolidRender(level, relativePos)) {
                        double xOffset = direction.getStepX() * 0.52;
                        double yOffset = random.nextDouble() * 0.5;
                        double zOffset = direction.getStepZ() * 0.52;
                        level.addParticle(ParticleTypes.CLOUD,
                                x + xOffset, y + yOffset, z + zOffset,
                                0.0, 0.0, 0.0);
                    }
                }
            }
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(x, y, z, SoundEvents.POWDER_SNOW_FALL, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("info.scguns.cryoniter.accepted_fuel").withStyle(ChatFormatting.AQUA));

            TagKey<Item> ingredientTag = ItemTags.create(CRYONITER_INGREDIENT_TAG);

            List<Item> fuelItems = StreamSupport.stream(
                            BuiltInRegistries.ITEM.getTagOrEmpty(ingredientTag).spliterator(), false)
                    .map(holder -> holder.value())
                    .toList();

            if (!fuelItems.isEmpty()) {
                for (Item item : fuelItems) {
                    tooltip.add(Component.literal("  ")
                            .append(Component.translatable(item.getDescriptionId()).withStyle(ChatFormatting.WHITE)));
                }
            } else {
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("info.scguns.no_fuels"))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }

            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("info.scguns.cryoniter.function")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.translatable("info.scguns.cryoniter.shift_fuel").withStyle(ChatFormatting.GRAY));
        }
    }
}