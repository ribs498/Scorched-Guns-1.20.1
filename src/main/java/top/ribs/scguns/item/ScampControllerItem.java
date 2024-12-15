package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.monster.SupplyScampEntity;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.util.PlayerScampManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ScampControllerItem extends Item {
    private static final int GLOWING_DURATION = 5;

    public ScampControllerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player && selected) {
            PlayerScampManager.PlayerScampData playerData = PlayerScampManager.getOrCreatePlayerData(player);
            UUID linkedScampId = playerData.getLinkedScampId();

            if (linkedScampId != null) {
                SupplyScampEntity linkedScamp = findScamp(level, linkedScampId);
                if (linkedScamp != null) {
                    // Apply glowing effect to linked scamp
                    linkedScamp.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION, 0, false, false));
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null || level.isClientSide()) {
            return InteractionResult.PASS;
        }

        PlayerScampManager.PlayerScampData playerData = PlayerScampManager.getOrCreatePlayerData(player);

        if (player.isShiftKeyDown()) {
            UUID linkedScampId = playerData.getLinkedScampId();
            if (linkedScampId != null) {
                SupplyScampEntity linkedScamp = findScamp(level, linkedScampId);
                if (linkedScamp != null) {
                    BlockEntity blockEntity = level.getBlockEntity(clickedPos);
                    if (blockEntity instanceof Container) {
                        playerData.setContainerPos(clickedPos);
                        linkedScamp.setLinkedContainer(clickedPos);
                        player.displayClientMessage(Component.translatable("message.supply_scamp.container_linked"), true);
                        return InteractionResult.SUCCESS;
                    } else {
                        linkedScamp.setPatrolOrigin(clickedPos);
                        linkedScamp.spawnPatrolOriginParticles();
                        player.displayClientMessage(Component.translatable("message.supply_scamp.new_patrol_origin"), true);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        } else {
            // Handle scamp state cycling
            UUID linkedScampId = playerData.getLinkedScampId();
            if (linkedScampId != null) {
                SupplyScampEntity linkedScamp = findScamp(level, linkedScampId);
                if (linkedScamp != null) {
                    if (linkedScamp.isOrderedToSit()) {
                        setScampPatrolling(linkedScamp, player);
                    } else if (linkedScamp.isPatrolling()) {
                        setScampFollowing(linkedScamp, player);
                    } else {
                        setScampSitting(linkedScamp, player);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    private SupplyScampEntity findScamp(Level level, UUID scampId) {
        List<SupplyScampEntity> scamps = level.getEntitiesOfClass(
                SupplyScampEntity.class,
                new AABB(level.getSharedSpawnPos()).inflate(64),
                scamp -> scamp.getUUID().equals(scampId)
        );
        return scamps.isEmpty() ? null : scamps.get(0);
    }

    private void setScampPatrolling(SupplyScampEntity scamp, Player player) {
        scamp.setOrderedToSit(false);
        scamp.setSitting(false);
        scamp.setPatrolling(true);
        scamp.setPatrolOrigin(scamp.blockPosition());
        scamp.spawnPatrolOriginParticles();
        player.displayClientMessage(Component.translatable("message.supply_scamp.patrolling"), true);
    }

    private void setScampFollowing(SupplyScampEntity scamp, Player player) {
        scamp.setPatrolling(false);
        scamp.setPatrolOrigin(null);
        player.displayClientMessage(Component.translatable("message.supply_scamp.following"), true);
    }

    private void setScampSitting(SupplyScampEntity scamp, Player player) {
        scamp.setOrderedToSit(true);
        scamp.setSitting(true);
        player.displayClientMessage(Component.translatable("message.supply_scamp.sitting"), true);
    }
}