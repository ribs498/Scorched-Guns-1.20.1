package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.List;

public class GuanoCandleBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 9.0D, 11.0D);

    private static final int CHECK_INTERVAL = 200;
    private static final int SPAWN_CHANCE = 90;
    private static final int SEARCH_RADIUS = 32;
    private static final int MAX_BATS = 8;

    public GuanoCandleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.FALSE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, Boolean.FALSE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!state.getValue(LIT) && itemstack.is(Items.FLINT_AND_STEEL)) {
            world.setBlock(pos, state.setValue(LIT, Boolean.TRUE), 3);
            world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.isCreative()) {
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
            if (!world.isClientSide) {
                world.scheduleTick(pos, this, CHECK_INTERVAL);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        if (state.getValue(LIT) && player.isCrouching() && itemstack.isEmpty()) {
            world.setBlock(pos, state.setValue(LIT, Boolean.FALSE), 3);
            world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY() + 0.75D;
            double d2 = (double)pos.getZ() + 0.5D;
            world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            world.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }

        if (random.nextInt(10) == 0) {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + 0.5D;
            double d2 = pos.getZ() + 0.5D;
            double offsetX = (random.nextDouble() - 0.5D) * 0.3D;
            double offsetZ = (random.nextDouble() - 0.5D) * 0.3D;
            double offsetY = random.nextDouble() * 0.1D;

            world.addParticle(ModParticleTypes.SULFUR_DUST.get(),
                    d0 + offsetX,
                    d1 + offsetY,
                    d2 + offsetZ,
                    0.0D, 0.03D, 0.0D);
        }
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getValue(LIT) && !world.isClientSide) {
            world.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            if (random.nextInt(SPAWN_CHANCE) == 0) {
                trySpawnBat(world, pos, random);
            }
            world.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    private void trySpawnBat(ServerLevel world, BlockPos pos, RandomSource random) {
        AABB searchArea = new AABB(pos).inflate(SEARCH_RADIUS);
        List<Bat> nearbyBats = world.getEntitiesOfClass(Bat.class, searchArea);

        if (nearbyBats.size() >= MAX_BATS) {
            return;
        }

        for (int attempts = 0; attempts < 10; attempts++) {
            double offsetX = (random.nextDouble() - 0.5D) * 10.0D;
            double offsetY = random.nextDouble() * 5.0D;
            double offsetZ = (random.nextDouble() - 0.5D) * 10.0D;

            BlockPos spawnPos = pos.offset((int)offsetX, (int)offsetY, (int)offsetZ);

            if (world.getBlockState(spawnPos).isAir() && world.getBlockState(spawnPos.below()).isSolidRender(world, spawnPos.below())) {
                Bat bat = EntityType.BAT.create(world);
                if (bat != null) {
                    bat.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
                    world.addFreshEntity(bat);
                    world.playSound(null, spawnPos, SoundEvents.BAT_AMBIENT, SoundSource.NEUTRAL, 0.5F, 1.0F);
                    break;
                }
            }
        }
    }
}