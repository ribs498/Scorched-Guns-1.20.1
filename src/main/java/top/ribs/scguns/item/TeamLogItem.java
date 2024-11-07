package top.ribs.scguns.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TeamLogItem extends Item {

    public TeamLogItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            EntityHitResult hitResult = rayTraceEntities(level, player);
            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity targetEntity) {
                if (player.isShiftKeyDown()) {
                    boolean added = addEntityTypeToBlacklist(itemStack, targetEntity);
                    if (added) {
                        player.displayClientMessage(Component.literal("Added all " + targetEntity.getType().getDescription().getString() + " to blacklist"), true);
                    } else {
                        player.displayClientMessage(Component.literal(targetEntity.getType().getDescription().getString() + " is already blacklisted"), true);
                    }
                } else {
                    boolean added = addEntityToTeamLog(itemStack, targetEntity);
                    if (added) {
                        player.displayClientMessage(Component.literal("Added " + targetEntity.getName().getString() + " to Team Log"), true);
                    } else {
                        player.displayClientMessage(Component.literal(targetEntity.getName().getString() + " is already in Team Log"), true);
                    }
                }
                logCurrentEntities(itemStack, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    EntityHitResult rayTraceEntities(Level level, Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        Vec3 endPos = eyePosition.add(lookVector.scale(5.0));
        AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(5.0)).inflate(1.0D, 1.0D, 1.0D);
        List<Entity> entities = level.getEntities(player, searchBox, entity -> entity instanceof LivingEntity && entity != player);
        EntityHitResult closestHitResult = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            AABB boundingBox = entity.getBoundingBox();
            Vec3 intercept = boundingBox.clip(eyePosition, endPos).orElse(null);
            if (intercept != null) {
                double distance = eyePosition.distanceTo(intercept);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestHitResult = new EntityHitResult(entity, intercept);
                }
            }
        }
        return closestHitResult;
    }
    boolean addEntityToTeamLog(ItemStack stack, LivingEntity targetEntity) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag existingTag = listTag.getCompound(i);
            if (existingTag.getUUID("UUID").equals(targetEntity.getUUID())) {
                return false;
            }
        }
        CompoundTag entityTag = new CompoundTag();
        entityTag.putUUID("UUID", targetEntity.getUUID());
        entityTag.putString("Name", targetEntity.getName().getString());
        entityTag.putString("EntityType", EntityType.getKey(targetEntity.getType()).toString());
        listTag.add(entityTag);
        tag.put("Entities", listTag);
        stack.setTag(tag);
        return true;
    }

    boolean addEntityTypeToBlacklist(ItemStack stack, LivingEntity targetEntity) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
        String entityTypeKey = EntityType.getKey(targetEntity.getType()).toString();
        for (int i = 0; i < blacklistTag.size(); i++) {
            if (blacklistTag.getString(i).equals(entityTypeKey)) {
                return false;
            }
        }
        blacklistTag.add(StringTag.valueOf(entityTypeKey));
        tag.put("Blacklist", blacklistTag);
        stack.setTag(tag);
        return true;
    }

    void logCurrentEntities(ItemStack stack, Player player) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("Entities", Tag.TAG_LIST)) {
                ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
                if (!listTag.isEmpty()) {
                    player.displayClientMessage(Component.literal("Entities in Team Log:"), true);
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompound(i);
                        String entityName = entityTag.getString("Name");
                        String entityType = entityTag.getString("EntityType");
                        player.displayClientMessage(Component.literal("- " + entityName + " (" + entityType + ")"), true);
                    }
                } else {
                    player.displayClientMessage(Component.literal("No specific entities logged."), true);
                }
            }

            if (tag.contains("Blacklist", Tag.TAG_LIST)) {
                ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
                if (!blacklistTag.isEmpty()) {
                    player.displayClientMessage(Component.literal("Blacklisted Entity Types:"), true);
                    for (int i = 0; i < blacklistTag.size(); i++) {
                        String entityType = blacklistTag.getString(i);
                        player.displayClientMessage(Component.literal("- " + entityType), true);
                    }
                } else {
                    player.displayClientMessage(Component.literal("No entity types blacklisted."), true);
                }
            }
        } else {
            player.displayClientMessage(Component.literal("No entities or types logged in the item."), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("Entities", Tag.TAG_LIST)) {
                ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
                if (!listTag.isEmpty()) {
                    tooltip.add(Component.literal("Logged Entities:"));
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag entityTag = listTag.getCompound(i);
                        String entityName = entityTag.getString("Name");
                        String entityType = entityTag.getString("EntityType");
                        tooltip.add(Component.literal("- " + entityName + " (" + entityType + ")"));
                    }
                }
            }

            if (tag.contains("Blacklist", Tag.TAG_LIST)) {
                ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
                if (!blacklistTag.isEmpty()) {
                    tooltip.add(Component.literal("Blacklisted Entity Types:"));
                    for (int i = 0; i < blacklistTag.size(); i++) {
                        String entityType = blacklistTag.getString(i);
                        tooltip.add(Component.literal("- " + entityType));
                    }
                }
            }
        } else {
            tooltip.add(Component.literal("No entities or types logged"));
        }
    }
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // Check for any type of logged data
            if (tag.contains("Entities", Tag.TAG_LIST)) {
                ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
                if (!listTag.isEmpty()) {
                    return true;
                }
            }
            if (tag.contains("Blacklist", Tag.TAG_LIST)) {
                ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_STRING);
                if (!blacklistTag.isEmpty()) {
                    return true;
                }
            }
            // For EnemyLogItem whitelist checks
            if (tag.contains("Whitelist", Tag.TAG_LIST)) {
                ListTag whitelistTag = tag.getList("Whitelist", Tag.TAG_COMPOUND);
                if (!whitelistTag.isEmpty()) {
                    return true;
                }
            }
            if (tag.contains("WhitelistEntityTypes", Tag.TAG_LIST)) {
                ListTag whitelistTypeTag = tag.getList("WhitelistEntityTypes", Tag.TAG_STRING);
                if (!whitelistTypeTag.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return ItemStack.EMPTY;
    }
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return false;
    }
}