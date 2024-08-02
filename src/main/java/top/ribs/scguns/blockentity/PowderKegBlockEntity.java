package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModBlockEntities;

public class PowderKegBlockEntity extends BlockEntity {
    private int smokeTimer = 0;
    private static final int SMOKE_INTERVAL = 20; // Emit smoke every second (20 ticks)

    public PowderKegBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POWDER_KEG.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PowderKegBlockEntity blockEntity) {
        if (level.isClientSide()) {
            blockEntity.clientTick(level, pos, state);
        }
    }

    private void clientTick(Level level, BlockPos pos, BlockState state) {
        if (++smokeTimer >= SMOKE_INTERVAL) {
            smokeTimer = 0;
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.2,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.2,
                    0, 0.05, 0);
        }
    }
}