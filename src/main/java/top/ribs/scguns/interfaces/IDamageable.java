package top.ribs.scguns.interfaces;

import top.ribs.scguns.entity.projectile.ProjectileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * An interface for notifying a block it has been hit by a projectile.
 *
 * Author: MrCrayfish
 */
public interface IDamageable
{
    @Deprecated
    default void onBlockDamaged(Level world, BlockState state, BlockPos pos, int damage)
    {
    }

    default void onBlockDamaged(Level world, BlockState state, BlockPos pos, ProjectileEntity projectile, float rawDamage, int damage)
    {
        this.onBlockDamaged(world, state, pos, damage);
    }
}
