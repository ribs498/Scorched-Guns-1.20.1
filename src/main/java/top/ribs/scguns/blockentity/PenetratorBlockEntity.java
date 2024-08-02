package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import top.ribs.scguns.block.PenetratorBlock;
import top.ribs.scguns.init.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PenetratorBlockEntity extends BlockEntity {
    public PenetratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PENETRATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PenetratorBlockEntity blockEntity) {
        if (!level.isClientSide) {
            boolean isPowered = level.hasNeighborSignal(pos);

            if (isPowered) {
                blockEntity.explode(level, pos, state);
                level.removeBlock(pos, false);
            }
        }
    }

    private void explode(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(PenetratorBlock.FACING);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        List<ItemStack> allDrops = new ArrayList<>();

        for (int i = 1; i <= ((PenetratorBlock) state.getBlock()).getTunnelLength(); i++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (facing.getAxis() == Direction.Axis.X) {
                        mutablePos.setWithOffset(pos, facing.getStepX() * i, y, z);
                    } else if (facing.getAxis() == Direction.Axis.Y) {
                        mutablePos.setWithOffset(pos, z, facing.getStepY() * i, y);
                    } else {
                        mutablePos.setWithOffset(pos, y, z, facing.getStepZ() * i);
                    }

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                                mutablePos.getX() + 0.5, mutablePos.getY() + 0.5, mutablePos.getZ() + 0.5,
                                3, 0.1, 0.1, 0.1, 0.0);
                        serverLevel.sendParticles(ParticleTypes.SMOKE,
                                mutablePos.getX() + 0.5, mutablePos.getY() + 0.5, mutablePos.getZ() + 0.5,
                                6, 0.2, 0.2, 0.2, 0.02);
                    }

                    BlockState targetBlockState = level.getBlockState(mutablePos);

                    if (targetBlockState.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    if (!targetBlockState.isAir()) {
                        // Collect drops from the destroyed block
                        List<ItemStack> drops = Block.getDrops(targetBlockState, (ServerLevel)level, mutablePos, null);
                        allDrops.addAll(drops);

                        // Remove the block
                        level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 3);

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.levelEvent(2001, mutablePos, Block.getId(targetBlockState));
                        }
                    }

                    applyDamageToEntities(level, mutablePos);
                }
            }
        }

        // Drop all collected items at the penetrator's position
        for (ItemStack item : allDrops) {
            Block.popResource(level, pos, item);
        }

        dropItemsAfterExplosion((ServerLevel) level, pos);
    }
    private void dropItemsAfterExplosion(ServerLevel level, BlockPos pos) {
        TagKey<Item> tagKey = ItemTags.create(new ResourceLocation("scguns", "penetrator_drops"));
        List<Item> items = new ArrayList<>();
        BuiltInRegistries.ITEM.getTagOrEmpty(tagKey).forEach(holder -> items.add(holder.value()));
        Random random = new Random();
        int itemsToDrop = random.nextInt(3) + 3;

        for (int i = 0; i < itemsToDrop; i++) {
            if (!items.isEmpty()) {
                Item item = items.get(random.nextInt(items.size()));
                ItemStack stack = new ItemStack(item, random.nextInt(2) + 1);
                Block.popResource(level, pos, stack);
            }
        }
    }
    private void applyDamageToEntities(Level level, BlockPos pos) {
        List<Entity> entities = level.getEntities(null, new AABB(
                pos.getX() - 2.0, pos.getY() - 2.0, pos.getZ() - 2.0,
                pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0
        ));

        if (level instanceof ServerLevel serverLevel) {
            DamageSource explosionDamage = serverLevel.damageSources().explosion(null);

            for (Entity entity : entities) {
                float damage = entity instanceof Player ? 20.0F : 9.0F;

                // Check if the entity is a player and if they are in Survival mode
                if (entity instanceof ServerPlayer player) {
                    if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
                        continue; // Skip damage application for non-Survival players
                    }
                    player.hurt(explosionDamage, damage);
                    player.causeFoodExhaustion(damage * 0.1F);
                    player.setHealth(player.getHealth() - damage);
                } else {
                    boolean damaged = entity.hurt(explosionDamage, damage);
                    if (!damaged && entity instanceof LivingEntity livingEntity) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 0));
                    }
                }

                double dx = entity.getX() - pos.getX();
                double dy = entity.getY() - pos.getY();
                double dz = entity.getZ() - pos.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance != 0) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            dx / distance * 0.4,
                            dy / distance * 0.4,
                            dz / distance * 0.4
                    ));
                }
            }
        }
    }


}