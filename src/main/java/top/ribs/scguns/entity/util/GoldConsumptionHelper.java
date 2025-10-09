package top.ribs.scguns.entity.util;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

public class GoldConsumptionHelper {
    private static final float DEFAULT_GOLD_VALUE = 3.0F;
    private static final float DEFAULT_PIGLIN_LOVED_VALUE = 4.0F;

    public static boolean isGoldItem(ItemStack stack) {
        if (isPoisonItem(stack)) {
            return true;
        }

        if (isVanillaGoldItem(stack)) {
            return true;
        }

        return stack.is(ItemTags.PIGLIN_LOVED);
    }

    private static boolean isVanillaGoldItem(ItemStack stack) {
        return stack.is(Items.GOLD_INGOT) ||
                stack.is(Items.GOLD_NUGGET) ||
                stack.is(Items.GOLD_BLOCK) ||
                stack.is(Items.RAW_GOLD) ||
                stack.is(Items.RAW_GOLD_BLOCK) ||
                (stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.GOLD) ||
                stack.is(Items.GOLDEN_SWORD) ||
                stack.is(Items.GOLDEN_PICKAXE) ||
                stack.is(Items.GOLDEN_AXE) ||
                stack.is(Items.GOLDEN_SHOVEL) ||
                stack.is(Items.GOLDEN_HOE) ||
                stack.is(Items.GOLDEN_APPLE) ||
                stack.is(Items.GOLDEN_CARROT) ||
                stack.is(Items.GOLDEN_HORSE_ARMOR) ||
                stack.is(Items.DEEPSLATE_GOLD_ORE) ||
                stack.is(Items.GOLD_ORE) ||
                stack.is(Items.NETHER_GOLD_ORE) ||
                stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    public static boolean isPoisonItem(ItemStack stack) {
        return stack.is(ModItems.SULFUR_CHUNK.get()) ||
                stack.is(ModBlocks.SULFUR_BLOCK.get().asItem()) ||
                stack.is(ModBlocks.NETHER_SULFUR_ORE.get().asItem()) ||
                stack.is(ModBlocks.SULFUR_ORE.get().asItem()) ||
                stack.is(ModBlocks.DEEPSLATE_SULFUR_ORE.get().asItem()) ||
                stack.is(ModItems.SULFUR_DUST.get());
    }

    public static float getGoldNuggetValue(ItemStack stack) {
        if (stack.is(Items.GOLD_NUGGET)) return 0.7F;
        if (stack.is(Items.GOLD_INGOT)) return 6.0F;
        if (stack.is(Items.GOLD_BLOCK)) return 54.0F;
        if (stack.is(Items.RAW_GOLD)) return 6.0F;
        if (stack.is(Items.RAW_GOLD_BLOCK)) return 54.0F;
        if (stack.is(Items.GOLDEN_APPLE)) return 48.0F;
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) return 432.0F;
        if (stack.is(Items.GOLDEN_CARROT)) return 6.0F;
        if (stack.is(Items.GOLDEN_HORSE_ARMOR)) return 24.0F;

        if (stack.is(Items.NETHER_GOLD_ORE) || stack.is(Items.GOLD_ORE) || stack.is(Items.DEEPSLATE_GOLD_ORE)) {
            return 5.0F;
        }

        if (stack.is(Items.GOLDEN_HELMET)) return 30.0F;
        if (stack.is(Items.GOLDEN_CHESTPLATE)) return 48.0F;
        if (stack.is(Items.GOLDEN_LEGGINGS)) return 42.0F;
        if (stack.is(Items.GOLDEN_BOOTS)) return 24.0F;

        if (stack.is(Items.GOLDEN_SWORD)) return 12.0F;
        if (stack.is(Items.GOLDEN_PICKAXE)) return 18.0F;
        if (stack.is(Items.GOLDEN_AXE)) return 18.0F;
        if (stack.is(Items.GOLDEN_SHOVEL)) return 6.0F;
        if (stack.is(Items.GOLDEN_HOE)) return 12.0F;

        if (stack.is(ItemTags.PIGLIN_LOVED)) {
            return DEFAULT_PIGLIN_LOVED_VALUE;
        }

        return DEFAULT_GOLD_VALUE;
    }

    public static float getHealthFromGold(ItemStack stack) {
        float goldValue = getGoldNuggetValue(stack);
        return Math.min(goldValue / 3.0F, 8.0F);
    }

    public static void applyPoisonEffects(Mob entity, ItemStack poisonStack) {
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        entity.hurt(entity.damageSources().magic(), 2.0F);
        entity.playSound(SoundEvents.PLAYER_HURT, 1.0F, 0.8F + entity.getRandom().nextFloat() * 0.4F);

        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double particleX = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.6;
                double particleY = entity.getY() + entity.getEyeHeight() - 0.1;
                double particleZ = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.6;
                double velocityX = (entity.getRandom().nextDouble() - 0.5) * 0.3;
                double velocityY = entity.getRandom().nextDouble() * 0.3 + 0.1;
                double velocityZ = (entity.getRandom().nextDouble() - 0.5) * 0.3;

                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
                        particleX, particleY, particleZ,
                        1, velocityX, velocityY, velocityZ, 0.0);
            }
        }
    }

    public static void showEatingParticles(Mob entity) {
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3; i++) {
                double particleX = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 0.5;
                double particleY = entity.getY() + entity.getEyeHeight() - 0.1;
                double particleZ = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 0.5;
                double velocityX = (entity.getRandom().nextDouble() - 0.5) * 0.15;
                double velocityY = entity.getRandom().nextDouble() * 0.15;
                double velocityZ = (entity.getRandom().nextDouble() - 0.5) * 0.15;

                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
                        particleX, particleY, particleZ,
                        1, velocityX, velocityY, velocityZ, 0.0);
            }
        }
    }

    public static void showSlagProductionParticles(Mob entity) {
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double particleX = entity.getX() + (entity.getRandom().nextDouble() - 0.5);
                double particleY = entity.getY() + entity.getEyeHeight();
                double particleZ = entity.getZ() + (entity.getRandom().nextDouble() - 0.5);
                double velocityX = (entity.getRandom().nextDouble() - 0.5) * 0.4;
                double velocityY = entity.getRandom().nextDouble() * 0.4 + 0.2;
                double velocityZ = (entity.getRandom().nextDouble() - 0.5) * 0.4;

                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME,
                        particleX, particleY, particleZ,
                        1, velocityX, velocityY, velocityZ, 0.0);
            }
        }
    }
}