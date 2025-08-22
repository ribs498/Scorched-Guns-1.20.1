package top.ribs.scguns.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.item.attachment.impl.Scope;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LaserSightItem extends ScopeItem {
    public LaserSightItem(Scope scope, Item.Properties properties) {
        super(scope, properties, true);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new LaserSightCapabilityProvider();
    }

    @Mod.EventBusSubscriber(modid = "scguns", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientTickHandler {
        private static int tickCounter = 0;
        private static final int TICK_DELAY = 2;
        private static final double OFFSET = 0.1;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                tickCounter++;
                if (tickCounter >= TICK_DELAY) {
                    tickCounter = 0;

                    Level world = player.level();
                    ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

                    if (heldItem.getItem() instanceof LaserSightItem || (heldItem.getItem() instanceof GunItem && Gun.hasLaserSight(heldItem))) {
                        Vec3 start = player.getEyePosition(1.0F);
                        Vec3 direction = player.getLookAngle();
                        Vec3 end = start.add(direction.scale(50));
                        HitResult hitResult = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
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
                        world.addParticle(ModParticleTypes.LASER.get(), adjustedPosition.x, adjustedPosition.y, adjustedPosition.z, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
    }

    private static class LaserSightCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
        }
    }
}