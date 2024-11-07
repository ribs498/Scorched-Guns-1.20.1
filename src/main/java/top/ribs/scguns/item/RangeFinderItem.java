package top.ribs.scguns.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RangeFinderItem extends Item {
    public RangeFinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide) {
            ClientEventHandler.calculateAndDisplayRange(player);
        }
        return InteractionResultHolder.success(itemstack);
    }

    @Mod.EventBusSubscriber(modid = "scguns", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEventHandler {
        private static int tickCounter = 0;
        private static final int TICK_DELAY = 2;
        private static final double OFFSET = 0.1;
        private static final int MAX_RANGE = 100;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                tickCounter++;
                if (tickCounter >= TICK_DELAY) {
                    tickCounter = 0;

                    ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
                    if (heldItem.getItem() instanceof RangeFinderItem) {
                        showLaserParticles(player);
                    }
                }
            }
        }

        private static void showLaserParticles(LocalPlayer player) {
            Level world = player.level();
            Vec3 start = player.getEyePosition(1.0F);
            Vec3 direction = player.getLookAngle();
            Vec3 end = start.add(direction.scale(MAX_RANGE));

            HitResult hitResult = player.level().clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));
            Vec3 finalEnd = hitResult.getType() != HitResult.Type.MISS ? hitResult.getLocation() : end;

            AABB aabb = new AABB(start, finalEnd).inflate(0.5);
            List<Entity> entities = world.getEntities(player, aabb, e -> !e.isSpectator() && e.isPickable());
            EntityHitResult entityHitResult = null;
            double closestDistance = Double.MAX_VALUE;

            for (Entity entity : entities) {
                AABB entityAABB = entity.getBoundingBox().inflate(0.3);
                Optional<Vec3> optionalHit = entityAABB.clip(start, finalEnd);
                if (optionalHit.isPresent()) {
                    Vec3 hitVec = optionalHit.get();
                    double distance = start.distanceTo(hitVec);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        entityHitResult = new EntityHitResult(entity, hitVec);
                    }
                }
            }

            if (entityHitResult != null) {
                finalEnd = entityHitResult.getLocation();
            }

            Vec3 adjustedPosition = finalEnd.subtract(direction.scale(OFFSET));
            world.addParticle(ModParticleTypes.LASER.get(),
                    adjustedPosition.x, adjustedPosition.y, adjustedPosition.z,
                    2.0D,
                    0.0D,
                    0.0D
            );
        }

        private static void calculateAndDisplayRange(Player player) {
            Vec3 start = player.getEyePosition(1.0F);
            Vec3 direction = player.getLookAngle();
            Vec3 end = start.add(direction.scale(MAX_RANGE));

            BlockHitResult hitResult = player.level().clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));
            AABB aabb = new AABB(start, end).inflate(0.5);
            List<Entity> entities = player.level().getEntities(player, aabb, e -> !e.isSpectator() && e.isPickable());
            EntityHitResult entityHitResult = null;
            double closestDistance = Double.MAX_VALUE;
            for (Entity entity : entities) {
                AABB entityAABB = entity.getBoundingBox().inflate(0.3);
                Optional<Vec3> optionalHit = entityAABB.clip(start, end);
                if (optionalHit.isPresent()) {
                    Vec3 hitVec = optionalHit.get();
                    double distance = start.distanceTo(hitVec);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        entityHitResult = new EntityHitResult(entity, hitVec);
                    }
                }
            }
            if (entityHitResult != null) {
                double distance = start.distanceTo(entityHitResult.getLocation());
                String entityName = entityHitResult.getEntity().getName().getString();
                player.displayClientMessage(Component.literal("Target: " + entityName + " - Distance: " + String.format("%.1f", distance) + " blocks"), true);
            } else if (hitResult.getType() != HitResult.Type.MISS) {
                double distance = start.distanceTo(hitResult.getLocation());
                BlockPos blockPos = hitResult instanceof BlockHitResult ? hitResult.getBlockPos() : null;
                String blockName = player.level().getBlockState(blockPos).getBlock().getName().getString();
                player.displayClientMessage(Component.literal("Target: " + blockName + " - Distance: " + String.format("%.1f", distance) + " blocks"), true);
            } else {
                player.displayClientMessage(Component.literal("No target in range"), true);
            }
        }
    }
}
