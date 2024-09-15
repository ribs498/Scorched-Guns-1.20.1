package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.monster.SupplyScampEntity;
import top.ribs.scguns.init.ModEntities;

import java.util.List;
import java.util.UUID;

public class ScampControllerItem extends Item {
    private static final String LINKED_SCAMP_UUID = "LinkedScampUUID";
    private static final int SEARCH_RANGE = 64;

    public ScampControllerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player != null && !level.isClientSide() && player.isShiftKeyDown()) {
            UUID linkedScampUUID = getLinkedScampUUID(itemStack);
            if (linkedScampUUID != null) {
                SupplyScampEntity linkedScamp = findLinkedScamp(level, linkedScampUUID);

                if (linkedScamp != null) {
                    // Check if the clicked block is a container
                    BlockEntity blockEntity = level.getBlockEntity(clickedPos);
                    if (blockEntity instanceof Container) {
                        // Link the scamp to the container
                        linkedScamp.setLinkedContainer(clickedPos);
                        player.displayClientMessage(Component.translatable("message.supply_scamp.container_linked"), true);
                        return InteractionResult.SUCCESS;
                    } else {
                        // Set the new patrol origin to the clicked block position
                        linkedScamp.setPatrolOrigin(clickedPos);
                        linkedScamp.spawnPatrolOriginParticles(); // Show particles at the new patrol point

                        // Notify the player
                        player.displayClientMessage(Component.translatable("message.supply_scamp.new_patrol_origin"), true);

                        return InteractionResult.SUCCESS;
                    }
                }
            }
        } else {
            // Fall back to the existing behavior of interacting with scamps via ray tracing
            assert player != null;
            Vec3 from = player.getEyePosition(1.0F);
            Vec3 look = player.getLookAngle();
            Vec3 to = from.add(look.x * 3.0D, look.y * 3.0D, look.z * 3.0D);

            AABB boundingBox = new AABB(from, to).inflate(0.5);
            List<SupplyScampEntity> nearbyScamps = level.getEntitiesOfClass(SupplyScampEntity.class, boundingBox);

            if (!nearbyScamps.isEmpty()) {
                SupplyScampEntity closestScamp = nearbyScamps.get(0);

                if (closestScamp.isTame() && closestScamp.isOwnedBy(player)) {
                    UUID currentLinkedUUID = getLinkedScampUUID(itemStack);
                    UUID newScampUUID = closestScamp.getUUID();

                    if (currentLinkedUUID == null || !currentLinkedUUID.equals(newScampUUID)) {
                        linkScamp(itemStack, closestScamp);
                        player.displayClientMessage(Component.translatable("message.supply_scamp.linked"), true);
                    } else {
                        if (closestScamp.isOrderedToSit()) {
                            setScampPatrolling(closestScamp, player);
                        } else if (closestScamp.isPatrolling()) {
                            setScampFollowing(closestScamp, player);
                        } else if (!closestScamp.isPatrolling()) {
                            setScampSitting(closestScamp, player);
                        }

                    }

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
    public void linkScamp(ItemStack itemStack, SupplyScampEntity scampEntity) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.putUUID(LINKED_SCAMP_UUID, scampEntity.getUUID());
        scampEntity.setLinkedToController(true);
    }

    public UUID getLinkedScampUUID(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getTag();
        return nbt != null && nbt.hasUUID(LINKED_SCAMP_UUID) ? nbt.getUUID(LINKED_SCAMP_UUID) : null;
    }

    public void unlinkScamp(Level level, UUID scampUUID) {
        SupplyScampEntity scamp = findLinkedScamp(level, scampUUID);
        if (scamp != null) {
            scamp.setLinkedToController(false);
        }
    }

    private SupplyScampEntity findLinkedScamp(Level level, UUID scampUUID) {
        List<SupplyScampEntity> scamps = level.getEntitiesOfClass(SupplyScampEntity.class,
                new AABB(level.getSharedSpawnPos()).inflate(SEARCH_RANGE),
                scamp -> scamp.getUUID().equals(scampUUID));

        return scamps.isEmpty() ? null : scamps.get(0);
    }

    private void setScampPatrolling(SupplyScampEntity scampEntity, Player player) {
        scampEntity.setOrderedToSit(false);
        scampEntity.setSitting(false);
        scampEntity.setPatrolling(true);
        scampEntity.setPatrolOrigin(scampEntity.blockPosition());
        scampEntity.spawnPatrolOriginParticles();
        player.displayClientMessage(Component.translatable("message.supply_scamp.patrolling"), true);
    }

    private void setScampFollowing(SupplyScampEntity scampEntity, Player player) {
        scampEntity.setPatrolling(false);
        scampEntity.setPatrolOrigin(null);
        player.displayClientMessage(Component.translatable("message.supply_scamp.following"), true);
    }

    private void setScampSitting(SupplyScampEntity scampEntity, Player player) {
        scampEntity.setOrderedToSit(true);
        scampEntity.setSitting(true);
        player.displayClientMessage(Component.translatable("message.supply_scamp.sitting"), true);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);

        if (pIsSelected && pEntity instanceof Player player && !pLevel.isClientSide()) {
            UUID linkedScampUUID = getLinkedScampUUID(pStack);
            if (linkedScampUUID != null) {
                SupplyScampEntity linkedScamp = findLinkedScamp(pLevel, linkedScampUUID);
                if (linkedScamp != null) {
                    MobEffectInstance glowingEffect = new MobEffectInstance(MobEffects.GLOWING, 5, 0, false, false);
                    linkedScamp.addEffect(glowingEffect);

                }
            }
        }
    }
}