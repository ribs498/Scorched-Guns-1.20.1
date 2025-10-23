package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ViciousAcidBlock extends LiquidBlock {
    private static final int DAMAGE_INTERVAL = 10;
    private static final float DAMAGE_AMOUNT = 2.0F;
    private static final int ARMOR_DAMAGE = 3;
    private static final int HELD_ITEM_DAMAGE = 5;
    private static final int DROPPED_ITEM_DAMAGE = 5;
    private static final float ENCHANTMENT_REMOVAL_CHANCE = 0.15F;

    public ViciousAcidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;

        if (entity instanceof LivingEntity livingEntity) {
            if (entity.tickCount % DAMAGE_INTERVAL == 0) {
                entity.hurt(level.damageSources().magic(), DAMAGE_AMOUNT);

                damageAndTryCurseClear(livingEntity.getArmorSlots(), ARMOR_DAMAGE, level, entity.blockPosition(), entity);
                damageAndTryClearHeldItems(livingEntity, level, entity.blockPosition());
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            if (entity.tickCount % DAMAGE_INTERVAL == 0) {
                ItemStack stack = itemEntity.getItem();

                if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                    return;
                }

                if (tryRemoveEnchantment(stack, level, entity.blockPosition())) {
                    return;
                }

                if (stack.isDamageableItem()) {
                    int newDamage = stack.getDamageValue() + DROPPED_ITEM_DAMAGE;
                    if (newDamage >= stack.getMaxDamage() - 1) {
                        stack.setDamageValue(stack.getMaxDamage() - 1);
                    } else {
                        stack.setDamageValue(newDamage);
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(LEVEL) == 0 && random.nextInt(5) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.95;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;

            level.addParticle(ModParticleTypes.ACID_BUBBLE.get(),
                    x, y, z,
                    0.0, 0.0, 0.0);
        }
    }

    private void damageAndTryCurseClear(Iterable<ItemStack> items, int damage, Level level, BlockPos pos, Entity entity) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                    continue;
                }

                if (tryRemoveEnchantment(stack, level, entity.blockPosition())) {
                    continue;
                }

                if (stack.isDamageableItem() && entity instanceof LivingEntity living) {
                    int newDamage = stack.getDamageValue() + damage;
                    if (newDamage >= stack.getMaxDamage() - 1) {
                        stack.setDamageValue(stack.getMaxDamage() - 1);
                    } else {
                        stack.hurtAndBreak(damage, living, (e) -> {});
                    }
                }
            }
        }
    }

    private void damageAndTryClearHeldItems(LivingEntity entity, Level level, BlockPos pos) {
        ItemStack mainHand = entity.getMainHandItem();
        if (!mainHand.isEmpty()) {
            if (mainHand.isDamageableItem() && mainHand.getDamageValue() < mainHand.getMaxDamage() - 1) {
                if (!tryRemoveEnchantment(mainHand, level, entity.blockPosition())) {
                    if (mainHand.isDamageableItem()) {
                        int newDamage = mainHand.getDamageValue() + HELD_ITEM_DAMAGE;
                        if (newDamage >= mainHand.getMaxDamage() - 1) {
                            mainHand.setDamageValue(mainHand.getMaxDamage() - 1);
                        } else {
                            mainHand.hurtAndBreak(HELD_ITEM_DAMAGE, entity, (e) -> {});
                        }
                    }
                }
            }
        }

        ItemStack offHand = entity.getOffhandItem();
        if (!offHand.isEmpty()) {
            if (offHand.isDamageableItem() && offHand.getDamageValue() < offHand.getMaxDamage() - 1) {
                if (!tryRemoveEnchantment(offHand, level, entity.blockPosition())) {
                    if (offHand.isDamageableItem()) {
                        int newDamage = offHand.getDamageValue() + HELD_ITEM_DAMAGE;
                        if (newDamage >= offHand.getMaxDamage() - 1) {
                            offHand.setDamageValue(offHand.getMaxDamage() - 1);
                        } else {
                            offHand.hurtAndBreak(HELD_ITEM_DAMAGE, entity, (e) -> {});
                        }
                    }
                }
            }
        }
    }

    private boolean tryRemoveEnchantment(ItemStack stack, Level level, BlockPos pos) {
        if (stack.isEmpty() || level.random.nextFloat() > ENCHANTMENT_REMOVAL_CHANCE) {
            return false;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        if (enchantments.isEmpty()) {
            return false;
        }

        List<Enchantment> allEnchants = new ArrayList<>(enchantments.keySet());
        Enchantment toRemove = allEnchants.get(level.random.nextInt(allEnchants.size()));
        enchantments.remove(toRemove);
        EnchantmentHelper.setEnchantments(enchantments, stack);

        spawnEnchantmentRemovalParticles(level, pos);
        return true;
    }

    private void spawnEnchantmentRemovalParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = level.random.nextGaussian() * 0.3;
                double offsetY = level.random.nextDouble() * 0.5 + 0.5;
                double offsetZ = level.random.nextGaussian() * 0.3;

                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1,
                        0.0, 0.1, 0.0,
                        0.05
                );
            }

            for (int i = 0; i < 10; i++) {
                double offsetX = level.random.nextGaussian() * 0.2;
                double offsetY = level.random.nextDouble() * 0.5 + 0.5;
                double offsetZ = level.random.nextGaussian() * 0.2;

                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1,
                        0.0, 0.1, 0.0,
                        0.02
                );
            }
        }
    }
}