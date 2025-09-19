package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.init.ModParticleTypes;

public class CogLocatorItem extends Item {
    public CogLocatorItem(Properties properties) {
        super(properties.durability(8));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos playerPos = player.blockPosition();
            BlockPos structurePos = findNearestCogChambers(serverLevel, playerPos);

            if (structurePos != null) {
                Vec3 playerVec = player.position();
                Vec3 structureVec = Vec3.atCenterOf(structurePos);
                Vec3 direction = structureVec.subtract(playerVec).normalize();
                createPlasmaRingLine(serverLevel, playerVec, direction);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.2F);

                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));

                return InteractionResultHolder.success(itemStack);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 0.8F);

                itemStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                return InteractionResultHolder.fail(itemStack);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private BlockPos findNearestCogChambers(ServerLevel level, BlockPos playerPos) {
        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var cogChambersKey = net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,
                new ResourceLocation("scguns", "cog_chambers"));

        var cogChambers = structureRegistry.getHolder(cogChambersKey);

        if (cogChambers.isPresent()) {
            HolderSet<Structure> holderSet = HolderSet.direct(cogChambers.get());
            var structurePos = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, holderSet, playerPos, 100, false);

            if (structurePos != null) {
                return structurePos.getFirst();
            }
        }

        return null;
    }

    private void createPlasmaRingLine(ServerLevel level, Vec3 start, Vec3 direction) {
        double lineLength = 25.0;
        int ringCount = 8;

        for (int i = 0; i < ringCount; i++) {
            double progress = (double) i / (double) (ringCount - 1);
            Vec3 ringPos = start.add(
                    direction.x * lineLength * progress,
                    direction.y * lineLength * progress + 1.5,
                    direction.z * lineLength * progress
            );
            double offsetX = (level.random.nextDouble() - 0.5) * 0.2;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.2;

            level.sendParticles(ModParticleTypes.PLASMA_RING.get(),
                    ringPos.x + offsetX,
                    ringPos.y + offsetY,
                    ringPos.z + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0 // Speed
            );
        }
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * 2 * Math.PI;
            double radius = 1.0 + (level.random.nextDouble() * 0.5);
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.5;

            level.sendParticles(ModParticleTypes.GREEN_FLAME.get(),
                    start.x + offsetX,
                    start.y + 1.0 + offsetY,
                    start.z + offsetZ,
                    1,
                    0.0, 0.1, 0.0,
                    0.05
            );
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF88;
    }
}