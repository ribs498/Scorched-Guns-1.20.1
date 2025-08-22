package top.ribs.scguns.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.enchantment.EnchantmentTypes;
import top.ribs.scguns.interfaces.IGunModifier;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;

public class BayonetItem extends UnderBarrelItem {
    private final float attackDamage;
    private final float attackSpeed;
    private final IGunModifier modifier;

    public BayonetItem(UnderBarrel underBarrel, Properties properties, float attackDamage, float attackSpeed) {
        super(underBarrel, properties);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.modifier = underBarrel.getModifier();
    }

    public BayonetItem(UnderBarrel underBarrel, Properties properties, boolean colored, float attackDamage, float attackSpeed) {
        super(underBarrel, properties, colored);
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.modifier = underBarrel.getModifier();
    }

    public float getAdditionalDamage() {
        return this.modifier.additionalDamage();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentTypes.BAYONET || enchantment.category == EnchantmentCategory.WEAPON) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) -> {
            entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (state.getDestroySpeed(world, pos) != 0.0F) {
            stack.hurtAndBreak(2, entityLiving, (entity) -> {
                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return ImmutableMultimap.of(
                    Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double) this.attackDamage, AttributeModifier.Operation.ADDITION),
                    Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double) this.attackSpeed, AttributeModifier.Operation.ADDITION)
            );
        }
        return super.getDefaultAttributeModifiers(equipmentSlot);
    }

    public float getDamage() {
        return this.attackDamage;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(Blocks.COBWEB) ? 15.0F : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState block) {
        return block.is(Blocks.COBWEB);
    }
}


