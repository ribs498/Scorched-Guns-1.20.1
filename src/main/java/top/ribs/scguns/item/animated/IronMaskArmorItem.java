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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import top.ribs.scguns.client.render.armor.IronMaskArmorRenderer;

import java.util.function.Consumer;

public class IronMaskArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public IronMaskArmorItem(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private IronMaskArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                   EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new IronMaskArmorRenderer();

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
        if (player != null && player.isShiftKeyDown() && isValidPraetorRitualStructure(level, pos)) {

            if (!level.isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    pContext.getItemInHand().shrink(1);
                }

                BlockPos centerPos = findCenterPosition(level, pos);

                level.setBlock(centerPos, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().west(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().east(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().north(), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(centerPos.above().south(), Blocks.AIR.defaultBlockState(), 3);

                spawnCreationEffects((ServerLevel) level, centerPos.above());
                level.playSound(null, centerPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 0.6F);
                level.playSound(null, centerPos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.6F, 1.2F);

                double random = level.random.nextDouble();

                if (random < 0.5) {
                    net.minecraft.world.entity.monster.Vindicator vindicator = new net.minecraft.world.entity.monster.Vindicator(
                            EntityType.VINDICATOR, level);
                    vindicator.moveTo(centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(vindicator);
                    level.playSound(null, centerPos, SoundEvents.VINDICATOR_CELEBRATE, SoundSource.HOSTILE, 1.0F, 0.8F);
                } else if (random < 0.75) {
                    top.ribs.scguns.entity.monster.PraetorEntity praetor = createNeutralPraetor(level, centerPos, player);
                    level.addFreshEntity(praetor);
                    level.playSound(null, centerPos, top.ribs.scguns.init.ModSounds.PRAETOR_IDLE.get(), SoundSource.HOSTILE, 1.0F, 1.2F);
                } else {
                    top.ribs.scguns.entity.monster.PraetorEntity praetor = new top.ribs.scguns.entity.monster.PraetorEntity(
                            top.ribs.scguns.init.ModEntities.PRAETOR.get(), level);
                    praetor.moveTo(centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(praetor);
                    level.playSound(null, centerPos, top.ribs.scguns.init.ModSounds.PRAETOR_IDLE.get(), SoundSource.HOSTILE, 1.0F, 0.8F);
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

    private boolean isValidPraetorRitualStructure(Level level, BlockPos pos) {
        BlockState clickedBlock = level.getBlockState(pos);
        if (!clickedBlock.is(Blocks.CLAY)) {
            return false;
        }

        if (isTShapeNorth(level, pos)) return true;
        if (isTShapeSouth(level, pos)) return true;
        if (isTShapeEast(level, pos)) return true;
        if (isTShapeWest(level, pos)) return true;

        if (isTShapeNorth(level, pos.below())) return true;
        if (isTShapeSouth(level, pos.below())) return true;
        if (isTShapeEast(level, pos.below())) return true;
        if (isTShapeWest(level, pos.below())) return true;

        if (isTShapeNorth(level, pos.north())) return true;
        if (isTShapeSouth(level, pos.south())) return true;
        if (isTShapeEast(level, pos.east())) return true;
        if (isTShapeWest(level, pos.west())) return true;

        return false;
    }

    private boolean isTShapeNorth(Level level, BlockPos basePos) {
        return level.getBlockState(basePos).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().west()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().east()).is(Blocks.CLAY);
    }

    private boolean isTShapeSouth(Level level, BlockPos basePos) {
        return level.getBlockState(basePos).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().west()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().east()).is(Blocks.CLAY);
    }

    private boolean isTShapeEast(Level level, BlockPos basePos) {
        return level.getBlockState(basePos).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().north()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().south()).is(Blocks.CLAY);
    }

    private boolean isTShapeWest(Level level, BlockPos basePos) {
        return level.getBlockState(basePos).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().north()).is(Blocks.CLAY) &&
                level.getBlockState(basePos.above().south()).is(Blocks.CLAY);
    }

    private BlockPos findCenterPosition(Level level, BlockPos pos) {
        if (isTShapeNorth(level, pos) || isTShapeSouth(level, pos) ||
                isTShapeEast(level, pos) || isTShapeWest(level, pos)) return pos;

        if (isTShapeNorth(level, pos.below()) || isTShapeSouth(level, pos.below()) ||
                isTShapeEast(level, pos.below()) || isTShapeWest(level, pos.below())) return pos.below();

        if (isTShapeNorth(level, pos.north()) || isTShapeSouth(level, pos.north()) ||
                isTShapeEast(level, pos.north()) || isTShapeWest(level, pos.north())) return pos.north();

        if (isTShapeNorth(level, pos.south()) || isTShapeSouth(level, pos.south()) ||
                isTShapeEast(level, pos.south()) || isTShapeWest(level, pos.south())) return pos.south();

        if (isTShapeNorth(level, pos.east()) || isTShapeSouth(level, pos.east()) ||
                isTShapeEast(level, pos.east()) || isTShapeWest(level, pos.east())) return pos.east();

        if (isTShapeNorth(level, pos.west()) || isTShapeSouth(level, pos.west()) ||
                isTShapeEast(level, pos.west()) || isTShapeWest(level, pos.west())) return pos.west();

        return pos;
    }

    private void spawnCreationEffects(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 30; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2.5;
            double y = pos.getY() + level.random.nextDouble() * 2.5;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2.5;

            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.05);
        }
        for (int i = 0; i < 20; i++) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + level.random.nextDouble() * 2.0;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.0, 0.1, 0.0, 0.05);
        }
    }

    private top.ribs.scguns.entity.monster.PraetorEntity createNeutralPraetor(Level level, BlockPos centerPos, Player creator) {
        top.ribs.scguns.entity.monster.PraetorEntity praetor = new top.ribs.scguns.entity.monster.PraetorEntity(
                top.ribs.scguns.init.ModEntities.PRAETOR.get(), level);
        praetor.moveTo(centerPos.getX() + 0.5, centerPos.getY(), centerPos.getZ() + 0.5, 0.0F, 0.0F);

        var maxHealthAttribute = praetor.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(maxHealthAttribute.getBaseValue() * 0.6);
            praetor.setHealth((float) maxHealthAttribute.getValue());
        }

        if (creator != null) {
            praetor.targetSelector.getAvailableGoals().removeIf(goal ->
                    goal.getGoal() instanceof net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal);

            praetor.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                    praetor, Player.class, 10, true, false,
                    player -> player != creator && !((Player)player).isCreative() && !player.isSpectator()
            ));
        }

        return praetor;
    }

    private PlayState predicate(AnimationState animationState) {
        animationState.getController().setAnimation(RawAnimation.begin().then("animation.iron_mask.idle", Animation.LoopType.LOOP));
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