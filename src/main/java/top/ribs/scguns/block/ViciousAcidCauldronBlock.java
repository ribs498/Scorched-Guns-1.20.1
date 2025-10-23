package top.ribs.scguns.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViciousAcidCauldronBlock extends AbstractCauldronBlock {
    private static final int DAMAGE_INTERVAL = 10;
    private static final float DAMAGE_AMOUNT = 2.0F;
    private static final int ARMOR_DAMAGE = 3;
    private static final int HELD_ITEM_DAMAGE = 5;
    private static final int DROPPED_ITEM_DAMAGE = 5;
    private static final float ENCHANTMENT_REMOVAL_CHANCE = 0.2F;

    public ViciousAcidCauldronBlock(Properties properties) {
        super(properties, Map.of());
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return 0.9375;
    }

    @Override
    public boolean isFull(BlockState state) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.BUCKET)) {
            if (!level.isClientSide) {
                player.setItemInHand(hand, new ItemStack(ModItems.VICIOUS_ACID_BUCKET.get()));
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.85;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;

            level.addParticle(ModParticleTypes.ACID_BUBBLE.get(),
                    x, y, z,
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) return;

        if (!isEntityInsideContent(state, pos, entity)) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            if (entity.tickCount % DAMAGE_INTERVAL == 0) {
                entity.hurt(level.damageSources().magic(), DAMAGE_AMOUNT);

                damageAndTryCurseClear(livingEntity.getArmorSlots(), ARMOR_DAMAGE, level, pos, entity);
                damageAndTryClearHeldItems(livingEntity, level, pos);
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            if (entity.tickCount % DAMAGE_INTERVAL == 0) {
                ItemStack stack = itemEntity.getItem();

                if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                    return;
                }

                if (tryRemoveEnchantment(stack, level, pos)) {
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

    private void damageAndTryCurseClear(Iterable<ItemStack> items, int damage, Level level, BlockPos pos, Entity entity) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                    continue;
                }

                if (tryRemoveEnchantment(stack, level, pos)) {
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
                if (!tryRemoveEnchantment(mainHand, level, pos)) {
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
                if (!tryRemoveEnchantment(offHand, level, pos)) {
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
                double offsetY = level.random.nextDouble() * 0.3 + 0.5;
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
                double offsetY = level.random.nextDouble() * 0.3 + 0.5;
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