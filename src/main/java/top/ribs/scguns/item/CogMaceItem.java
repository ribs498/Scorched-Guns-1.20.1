package top.ribs.scguns.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class CogMaceItem extends SwordItem {

    public CogMaceItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        attacker.resetFallDistance();
        stack.hurtAndBreak(1, attacker, (entity) -> {
            entity.broadcastBreakEvent(entity.getUsedItemHand());
        });

        return super.hurtEnemy(stack, target, attacker);
    }
}