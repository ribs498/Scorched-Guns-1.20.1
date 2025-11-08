package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class AnthraliteHammerItem extends PickaxeItem {
    public AnthraliteHammerItem(Tier tier, float attackDamage, Item.Properties properties) {
        super(tier, 1, attackDamage, properties);
    }

    @Override
    public boolean mineBlock(ItemStack itemstack, Level world, BlockState blockstate, BlockPos pos, LivingEntity entity) {
        boolean retval = super.mineBlock(itemstack, world, blockstate, pos, entity);

        try {
            Class<?> hammerProcedureClass = Class.forName("create_ironworks.procedures.HammerProcedure");
            java.lang.reflect.Method executeMethod = hammerProcedureClass.getMethod("execute",
                    LevelAccessor.class, double.class, double.class, double.class, Entity.class);
            executeMethod.invoke(null, world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), entity);
        } catch (Exception e) {
        }

        return retval;
    }
}