package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.blockentity.LightningBatteryBlockEntity;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModItems;

import java.util.Random;

public class LightningBattery extends Block implements EntityBlock {
    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");
    public static final EnumProperty<ChargeLevel> CHARGE_LEVEL = EnumProperty.create("charge_level", ChargeLevel.class);

    private static final int CHARGE_AMOUNT = 8000;

    public LightningBattery(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CHARGED, Boolean.FALSE)
                .setValue(CHARGE_LEVEL, ChargeLevel.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGED, CHARGE_LEVEL);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);
        if (!world.isClientSide) {
            world.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LightningBatteryBlockEntity) {
                ((LightningBatteryBlockEntity) blockEntity).drops();
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (world.isThundering() && isNearbyRodPowered(world, pos)) {
            chargeBattery(world, pos, state);
        }
    }

    private void chargeBattery(Level world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LightningBatteryBlockEntity) {
            LightningBatteryBlockEntity battery = (LightningBatteryBlockEntity) blockEntity;
            int energyStored = battery.getEnergy();
            if (energyStored < battery.getMaxEnergy()) {
                battery.setEnergyStored(energyStored + CHARGE_AMOUNT);
                world.setBlock(pos, state.setValue(CHARGED, true)
                        .setValue(CHARGE_LEVEL, calculateChargeLevel(energyStored + CHARGE_AMOUNT)), 3);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(CHARGED)) {
            for (int i = 0; i < 5; ++i) {
                double d0 = (double) pos.getX() + random.nextDouble();
                double d1 = (double) pos.getY() + random.nextDouble();
                double d2 = (double) pos.getZ() + random.nextDouble();
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LightningBatteryBlockEntity) {
            LightningBatteryBlockEntity batteryEntity = (LightningBatteryBlockEntity) blockEntity;
            batteryEntity.tick();
        }
        world.scheduleTick(pos, this, 1);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof LightningBatteryBlockEntity) {
                MenuProvider containerProvider = (LightningBatteryBlockEntity) blockEntity;
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    public static ChargeLevel calculateChargeLevel(int energyStored) {
        if (energyStored > 40000) {
            return ChargeLevel.HIGH;
        } else if (energyStored > 16000) {
            return ChargeLevel.MID;
        } else if (energyStored > 0) {
            return ChargeLevel.LOW;
        } else {
            return ChargeLevel.NONE;
        }
    }

    private boolean isNearbyRodPowered(Level world, BlockPos pos) {
        for (int y = 1; y <= 5; y++) {
            BlockPos abovePos = pos.above(y);
            BlockState state = world.getBlockState(abovePos);
            if ((state.getBlock() instanceof LightningRodBlock || state.getBlock() instanceof LightningRodConnectorBlock)
                    && state.getValue(BlockStateProperties.POWERED)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightningBatteryBlockEntity(pos, state);
    }

    public enum ChargeLevel implements StringRepresentable {
        NONE("none"),
        LOW("low"),
        MID("mid"),
        HIGH("high");

        private final String name;

        ChargeLevel(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.getSerializedName();
        }
    }
}