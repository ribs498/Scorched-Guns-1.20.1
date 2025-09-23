package top.ribs.scguns.event;


import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.entity.projectile.FireRoundEntity;
import top.ribs.scguns.entity.projectile.ProjectileEntity;

public class SculkHordeEvents {
    //Doing this in a separate event, so we can do this optionally.
    @SubscribeEvent
    public static void onProjectileHit(GunProjectileHitEvent event) {
        if (event.getProjectile().level().isClientSide) {
            return;
        }

        ProjectileEntity projectile = event.getProjectile();
        if (!(projectile instanceof FireRoundEntity)) return;

        Level level = projectile.level();
        HitResult result = event.getRayTrace();


        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) result;

            SpawnCursorAndDisinfect(1, blockResult.getBlockPos(), level);
        }

        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entResult = (EntityHitResult) result;
            Entity entity = entResult.getEntity();

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(ModMobEffects.PURITY.get(), 20*10));
            }

            SpawnCursorAndDisinfect(1, entity.blockPosition(), level);
        }
    }

    private static void SpawnCursorAndDisinfect(int size, BlockPos pos, Level level) {
        for (int y = -size; y < size; y++) {
            for (int x = -size; x < size; x++) {
                for (int z = -size; z < size; z++) {
                    BlockPos new_pos = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockInfestationSystem.tryToCureBlock((ServerLevel) level, new_pos);
                }
            }
        }

        if (Math.random() >= 0.33) {
            CursorSurfacePurifierEntity cursorEntity = new CursorSurfacePurifierEntity(level);

            cursorEntity.setPos(pos.getCenter());
            cursorEntity.setMaxTransformations(8);
            cursorEntity.setMaxRange(50);
            cursorEntity.setSearchIterationsPerTick(5);
            cursorEntity.setMaxLifeTimeMillis(10000 / 2);
            cursorEntity.setTickIntervalMilliseconds(150);
            level.addFreshEntity(cursorEntity);
        }
    }
}
