package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightningRodConnectorBlock extends LightningRodBlock {
    public LightningRodConnectorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onLightningStrike(BlockState pState, Level pLevel, BlockPos pPos) {
        pLevel.setBlock(pPos, pState.setValue(POWERED, true), 3);
        propagatePoweredState(pLevel, pPos, true);
        pLevel.scheduleTick(pPos, this, 8);
        pLevel.levelEvent(3002, pPos, pState.getValue(FACING).getAxis().ordinal());
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        pLevel.setBlock(pPos, pState.setValue(POWERED, false), 3);
        propagatePoweredState(pLevel, pPos, false);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
        checkAndUpdatePoweredState(pLevel, pPos);
    }

    private void propagatePoweredState(Level pLevel, BlockPos pPos, boolean isPowered) {
        BlockPos belowPos = pPos.below();
        BlockState belowState = pLevel.getBlockState(belowPos);

        if (belowState.getBlock() instanceof LightningRodConnectorBlock) {
            pLevel.setBlock(belowPos, belowState.setValue(POWERED, isPowered), 3);
            ((LightningRodConnectorBlock) belowState.getBlock()).propagatePoweredState(pLevel, belowPos, isPowered);
        }
    }

    private void checkAndUpdatePoweredState(Level pLevel, BlockPos pPos) {
        BlockPos abovePos = pPos.above();
        BlockState aboveState = pLevel.getBlockState(abovePos);

        if ((aboveState.getBlock() instanceof LightningRodBlock || aboveState.getBlock() instanceof LightningRodConnectorBlock)
                && aboveState.getValue(POWERED)) {
            pLevel.setBlock(pPos, pLevel.getBlockState(pPos).setValue(POWERED, true), 3);
            propagatePoweredState(pLevel, pPos, true);
        } else {
            pLevel.setBlock(pPos, pLevel.getBlockState(pPos).setValue(POWERED, false), 3);
            propagatePoweredState(pLevel, pPos, false);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, pContext.getClickedFace()).setValue(WATERLOGGED, flag);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pState.getValue(POWERED) && pState.getValue(FACING) == pDirection ? 15 : 0;
    }

    @Override
    public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        if (pLevel.isThundering() && pProjectile instanceof ThrownTrident && ((ThrownTrident) pProjectile).isChanneling()) {
            BlockPos blockpos = pHit.getBlockPos();
            if (pLevel.canSeeSky(blockpos)) {
                LightningBolt lightningbolt = (LightningBolt) EntityType.LIGHTNING_BOLT.create(pLevel);
                if (lightningbolt != null) {
                    lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos.above()));
                    Entity entity = pProjectile.getOwner();
                    lightningbolt.setCause(entity instanceof ServerPlayer ? (ServerPlayer) entity : null);
                    pLevel.addFreshEntity(lightningbolt);
                }

                pLevel.playSound(null, blockpos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWERED, WATERLOGGED);
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }
}
