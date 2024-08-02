package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModBlockEntities;

public class NitroKegBlockEntity extends BlockEntity {
    private int smokeTimer = 0;
    private static final int SMOKE_INTERVAL = 5;
    private static final int SMOKE_AMOUNT = 3;

    public NitroKegBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NITRO_KEG.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NitroKegBlockEntity blockEntity) {
        if (level.isClientSide()) {
            blockEntity.clientTick(level, pos, state);
        }
    }

    private void clientTick(Level level, BlockPos pos, BlockState state) {
        if (++smokeTimer >= SMOKE_INTERVAL) {
            smokeTimer = 0;
            for (int i = 0; i < SMOKE_AMOUNT; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetY = level.random.nextDouble() * 0.3;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;

                level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 1.0 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        0, 0.05, 0);
            }
        }
    }
}