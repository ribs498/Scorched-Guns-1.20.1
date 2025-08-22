package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.entity.monster.TheMerchantEntity;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CShowTotemAnimationMessage;


public class ThePactItem extends Item {
    private static final double SUMMON_RANGE = 12.0;
    private static final int BLINDNESS_DURATION = 100;
    private static final int SLOWNESS_DURATION = 100;

    public ThePactItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) pLevel;

            if (hasNearbyMerchant(serverLevel, pPlayer)) {
                return InteractionResultHolder.fail(itemStack);
            }

            applyPactEffects(pPlayer);

            if (pPlayer instanceof ServerPlayer serverPlayer) {
                PacketHandler.getPlayChannel().sendToPlayer(() -> serverPlayer, new S2CShowTotemAnimationMessage(itemStack));
            }

            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.8F);

            Vec3 spawnPos = findSpawnPosition(pPlayer);

            TheMerchantEntity merchant = new TheMerchantEntity(ModEntities.THE_MERCHANT.get(), serverLevel);
            merchant.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            merchant.setSummoner(pPlayer);

            merchant.setWalkingToSummoner(true);

            if (serverLevel.addFreshEntity(merchant)) {
                merchant.createSpawnEffect();
                if (!pPlayer.isCreative()) {
                    itemStack.shrink(1);
                }

                return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide());
            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide());
    }

    private void applyPactEffects(Player player) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        if (currentHealth > 1.0F) {
            player.setHealth(1.0F);
        }
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 3, false, false, false)); // Slowness 4

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(), SoundSource.PLAYERS, 0.8F, 0.7F);
    }

    private boolean hasNearbyMerchant(ServerLevel level, Player player) {
        return level.getEntitiesOfClass(TheMerchantEntity.class,
                        player.getBoundingBox().inflate(20.0))
                .stream()
                .anyMatch(merchant -> merchant.getSummoner() != null &&
                        merchant.getSummoner().getUUID().equals(player.getUUID()));
    }

    private Vec3 findSpawnPosition(Player player) {
        Vec3 playerPos = player.position();
        Level level = player.level();
        double[] spawnRadii = {SUMMON_RANGE, SUMMON_RANGE + 5, SUMMON_RANGE + 10};

        for (double radius : spawnRadii) {
            for (int attempts = 0; attempts < 32; attempts++) {
                double angle = (attempts * Math.PI * 2) / 32;
                double x = playerPos.x + Math.cos(angle) * radius;
                double z = playerPos.z + Math.sin(angle) * radius;

                for (int yOffset = 5; yOffset >= -10; yOffset--) {
                    int y = (int) playerPos.y + yOffset;

                    if (isValidMerchantSpawnPosition(level, x, y, z)) {
                        return new Vec3(x, y + 1, z);
                    }
                }
            }
        }

        return playerPos.add(0, 1, 0);
    }

    private boolean isValidMerchantSpawnPosition(Level level, double x, int y, double z) {
        BlockPos groundPos = new BlockPos((int) x, y, (int) z);
        BlockPos spawnPos = groundPos.above();
        BlockPos headPos = spawnPos.above();

        if (!level.getBlockState(groundPos).isSolidRender(level, groundPos)) {
            return false;
        }

        if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(headPos).isAir()) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = spawnPos.offset(dx, 0, dz);
                if (!level.getBlockState(checkPos).isAir()) {
                    return false;
                }
            }
        }
        boolean foundGroundNearby = false;
        for (int checkY = y; checkY >= y - 5; checkY--) {
            if (level.getBlockState(new BlockPos((int) x, checkY, (int) z)).isSolidRender(level, new BlockPos((int) x, checkY, (int) z))) {
                foundGroundNearby = true;
                break;
            }
        }

        return foundGroundNearby;
    }
}