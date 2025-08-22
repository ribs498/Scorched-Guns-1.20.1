package top.ribs.scguns.item.animated;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.render.armor.BrassMaskArmorRenderer;
import top.ribs.scguns.entity.monster.DissidentEntity;
import top.ribs.scguns.init.ModEntities;

import java.util.function.Consumer;

public class BrassMaskArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public BrassMaskArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BrassMaskArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                   EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new BrassMaskArmorRenderer();

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        if (player != null && player.isShiftKeyDown() && isValidHomUnculusStructure(level, pos)) {

            if (!level.isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    pContext.getItemInHand().shrink(1);
                }

                BlockPos bottomPos, topPos;
                if (level.getBlockState(pos.above()).is(Blocks.CLAY)) {
                    bottomPos = pos;
                    topPos = pos.above();
                } else {
                    bottomPos = pos.below();
                    topPos = pos;
                }

                level.setBlock(bottomPos, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);

                spawnCreationEffects((ServerLevel) level, bottomPos.above());
                level.playSound(null, bottomPos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);

                boolean disableVillagers = Config.COMMON.gameplay.disableVillagerSpawning.get();

                if (disableVillagers) {
                    double dissidentChance = Config.COMMON.gameplay.dissidentSpawnChance.get();

                    if (level.random.nextDouble() < dissidentChance) {
                        DissidentEntity dissident = new DissidentEntity(ModEntities.DISSIDENT.get(), level);
                        dissident.moveTo(bottomPos.getX() + 0.5, bottomPos.getY(), bottomPos.getZ() + 0.5, 0.0F, 0.0F);
                        level.addFreshEntity(dissident);
                        level.playSound(null, bottomPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.0F, 0.8F);
                    }
                } else {
                    boolean createDissident = level.random.nextBoolean();

                    if (createDissident) {
                        DissidentEntity dissident = new DissidentEntity(ModEntities.DISSIDENT.get(), level);
                        dissident.moveTo(bottomPos.getX() + 0.5, bottomPos.getY(), bottomPos.getZ() + 0.5, 0.0F, 0.0F);
                        level.addFreshEntity(dissident);
                        level.playSound(null, bottomPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.0F, 0.8F);
                    } else {
                        Villager villager = new Villager(EntityType.VILLAGER, level);
                        villager.moveTo(bottomPos.getX() + 0.5, bottomPos.getY(), bottomPos.getZ() + 0.5, 0.0F, 0.0F);

                        var villagerTypes = BuiltInRegistries.VILLAGER_TYPE.stream().toList();
                        VillagerType randomType = villagerTypes.get(level.random.nextInt(villagerTypes.size()));
                        villager.setVillagerData(villager.getVillagerData().setType(randomType).setProfession(VillagerProfession.NONE));

                        level.addFreshEntity(villager);
                        level.playSound(null, bottomPos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 1.2F);
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }
        return super.useOn(pContext);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (pPlayer.isShiftKeyDown()) {
            return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
        }

        return super.use(pLevel, pPlayer, pHand);
    }

    /**
     * Checks if the structure at the given position is valid for homunculus creation
     * (2 clay blocks stacked vertically) - can click either block in the stack
     */
    private boolean isValidHomUnculusStructure(Level level, BlockPos pos) {
        BlockState clickedBlock = level.getBlockState(pos);
        if (!clickedBlock.is(Blocks.CLAY)) {
            return false;
        }
        if (level.getBlockState(pos.above()).is(Blocks.CLAY)) {
            return true;
        }
        return level.getBlockState(pos.below()).is(Blocks.CLAY);
    }

    /**
     * Spawns particle effects for the homunculus creation
     */
    private void spawnCreationEffects(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + level.random.nextDouble() * 2.0;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.0, 0.0, 0.0, 0.1);
        }
        for (int i = 0; i < 15; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;
            double y = pos.getY() + level.random.nextDouble() * 1.5;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5;

            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.0, 0.1, 0.0, 0.05);
        }
    }

    private PlayState predicate(AnimationState animationState) {
        animationState.getController().setAnimation(RawAnimation.begin().then("animation.brass_mask.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}