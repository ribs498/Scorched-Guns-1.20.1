package top.ribs.scguns.config;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.ai.AIType;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.entity.player.GunTier;
import top.ribs.scguns.entity.player.PlayerGunProgression;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunCurseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunnerMobSpawner {
    public static final UUID GUN_FOLLOW_RANGE_MODIFIER_UUID = UUID.randomUUID();

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!GunMobValues.enabled) {
            return;
        }

        LivingEntity entity = event.getEntity();

        if (!(entity instanceof PathfinderMob mob)) {
            return;
        }

        if (!entity.getType().is(ModTags.Entities.GUNNER)) {
            return;
        }

        GunnerMobConfig.MobGunnerData gunnerData = GunnerMobConfig.getGunnerData(entity.getType());
        if (gunnerData != null) {
            if (entity.getRandom().nextFloat() >= gunnerData.spawnChance()) {
                return;
            }
            mob.addTag("MobGunner");
            mob.addTag("ThematicGunner");
            return;
        }

        Player nearestPlayer = entity.level().getNearestPlayer(entity, 64.0);
        if (nearestPlayer != null) {
            PlayerGunProgression progression = PlayerGunProgression.get(nearestPlayer);
            List<GunTier> availableTiers = progression.getAvailableMobTiers();

            boolean hasValidTiers = false;
            for (GunTier tier : availableTiers) {
                if (TieredWeaponConfig.hasTierWeapons(tier)) {
                    hasValidTiers = true;
                    break;
                }
            }

            double spawnChance = GunMobValues.getGunnerSpawnChance(entity.level().getDifficulty());
            if (hasValidTiers && entity.getRandom().nextFloat() < spawnChance) {
                mob.addTag("MobGunner");
                mob.addTag("ProgressionGunner");
            }
        }
    }

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }

        ItemStack heldItem = mob.getMainHandItem();

        if (heldItem.getItem() instanceof GunItem) {
            reassessWeaponGoal(mob);
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }

        if (mob instanceof AbstractPiglin abstractPiglin && abstractPiglin.level().dimension() == Level.OVERWORLD) {
            if (abstractPiglin.tickCount % 20 == 0) {
                ItemStack helmet = abstractPiglin.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.is(top.ribs.scguns.init.ModTags.Items.GAS_MASK)) {
                    abstractPiglin.setImmuneToZombification(true);
                } else {
                    abstractPiglin.setImmuneToZombification(false);
                }
            }
        }

        if (mob.tickCount >= 2) {
            return;
        }

        ItemStack heldItem = mob.getMainHandItem();

        if (!GunMobValues.enabled) {
            return;
        }

        if (mob.getTags().contains("MobGunner") && !(heldItem.getItem() instanceof GunItem)) {
            if (mob.getTags().contains("ThematicGunner")) {
                equipThematicGun(mob);
            } else if (mob.getTags().contains("ProgressionGunner")) {
                equipProgressionGun(mob);
            }
        }

        if (heldItem.getItem() instanceof GunItem) {
            reassessWeaponGoal(mob);
        }
    }

    private static void equipThematicGun(PathfinderMob mob) {
        GunnerMobConfig.MobGunnerData gunnerData = GunnerMobConfig.getGunnerData(mob.getType());

        if (gunnerData != null) {
            Item gun = gunnerData.getRandomWeapon(mob.getRandom());
            if (gun == null) {
                return;
            }

            AIType aiType = AIType.values()[mob.getRandom().nextInt(AIType.values().length)];
            boolean elite = (mob.getRandom().nextFloat() < GunMobValues.eliteChance && GunMobValues.elitesEnabled);
            int aiLevel = gunnerData.aiDifficulty() + (elite ? 1 : 0);

            if (elite) {
                mob.addTag("EliteGunner");
                mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            }

            for (GunnerMobConfig.ArmorPiece armorPiece : gunnerData.allowedArmor()) {
                if (mob.getRandom().nextFloat() < armorPiece.spawnChance()) {
                    EquipmentSlot slot = switch (armorPiece.slot()) {
                        case "head" -> EquipmentSlot.HEAD;
                        case "chest" -> EquipmentSlot.CHEST;
                        case "legs" -> EquipmentSlot.LEGS;
                        case "feet" -> EquipmentSlot.FEET;
                        default -> null;
                    };
                    if (slot != null) {
                        mob.setItemSlot(slot, new ItemStack(armorPiece.item()));
                    }
                }
            }

            if (!mob.level().isClientSide && !hasGunAttackGoal(mob)) {
                ItemStack modifiedGun = createModifiedGun(mob, gun);
                mob.goalSelector.addGoal(2, new GunAttackGoal<>(mob, modifiedGun, 1.2F, aiType, aiLevel));
                mob.addTag("GunAttackAssigned");
            }

            ItemStack modifiedGun = createModifiedGun(mob, gun);
            GunCurseUtil.applyCurseIfRoll(modifiedGun, mob.getRandom());
            mob.setItemSlot(EquipmentSlot.MAINHAND, modifiedGun);

            extendFollowRange(mob);
        }
    }

    private static void equipProgressionGun(PathfinderMob mob) {
        Player nearestPlayer = mob.level().getNearestPlayer(mob, 64.0);
        if (nearestPlayer == null) {
            return;
        }

        PlayerGunProgression progression = PlayerGunProgression.get(nearestPlayer);
        List<GunTier> availableTiers = progression.getAvailableMobTiers();

        if (availableTiers.isEmpty()) {
            return;
        }

        boolean isElite = (mob.getRandom().nextFloat() < GunMobValues.eliteChance && GunMobValues.elitesEnabled);

        List<GunTier> validTiers = new ArrayList<>();
        for (GunTier tier : availableTiers) {
            if (isElite) {
                if (EliteTierConfig.hasEliteData(tier)) {
                    validTiers.add(tier);
                }
            } else {
                if (TieredWeaponConfig.hasTierWeapons(tier)) {
                    validTiers.add(tier);
                }
            }
        }

        if (validTiers.isEmpty()) {
            return;
        }

        GunTier selectedTier;
        float rand = mob.getRandom().nextFloat();

        if (rand < 0.6f) {
            int index = mob.getRandom().nextInt(Math.max(1, validTiers.size() / 2));
            selectedTier = validTiers.get(index);
        } else if (rand < 0.9f && validTiers.size() > 1) {
            int midStart = validTiers.size() / 3;
            int midEnd = (validTiers.size() * 2) / 3;
            if (midEnd <= midStart) midEnd = midStart + 1;
            if (midEnd >= validTiers.size()) midEnd = validTiers.size() - 1;
            int index = midStart + mob.getRandom().nextInt(midEnd - midStart + 1);
            selectedTier = validTiers.get(index);
        } else {
            int index = Math.max(0, validTiers.size() - 1 - mob.getRandom().nextInt(Math.max(1, validTiers.size() / 3)));
            selectedTier = validTiers.get(index);
        }

        Item gun;
        if (isElite) {
            EliteTierConfig.EliteData eliteData = EliteTierConfig.getEliteData(selectedTier);
            if (eliteData == null) {
                return;
            }
            gun = eliteData.getRandomWeapon(mob.getRandom());
            if (gun == null) {
                return;
            }

            mob.addTag("EliteGunner");
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);

            for (EliteTierConfig.ArmorPiece armorPiece : eliteData.armor()) {
                if (mob.getRandom().nextFloat() < armorPiece.chance()) {
                    EquipmentSlot slot = switch (armorPiece.slot()) {
                        case "head" -> EquipmentSlot.HEAD;
                        case "chest" -> EquipmentSlot.CHEST;
                        case "legs" -> EquipmentSlot.LEGS;
                        case "feet" -> EquipmentSlot.FEET;
                        default -> null;
                    };
                    if (slot != null) {
                        mob.setItemSlot(slot, new ItemStack(armorPiece.item()));
                    }
                }
            }
        } else {
            gun = TieredWeaponConfig.getRandomWeaponForTier(selectedTier, mob.getRandom());
            if (gun == null) {
                return;
            }
        }

        AIType aiType = AIType.values()[mob.getRandom().nextInt(AIType.values().length)];
        int aiLevel = 2 + (isElite ? 1 : 0);

        if (!mob.level().isClientSide && !hasGunAttackGoal(mob)) {
            ItemStack modifiedGun = createModifiedGun(mob, gun);
            mob.goalSelector.addGoal(2, new GunAttackGoal<>(mob, modifiedGun, 1.2F, aiType, aiLevel));
            mob.addTag("GunAttackAssigned");
        }

        ItemStack modifiedGun = createModifiedGun(mob, gun);
        GunCurseUtil.applyCurseIfRoll(modifiedGun, mob.getRandom());
        mob.setItemSlot(EquipmentSlot.MAINHAND, modifiedGun);

        extendFollowRange(mob);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!GunMobValues.enabled) {
            return;
        }

        if (!(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }

        mob.removeTag("GunAttackAssigned");

        ItemStack heldItem = mob.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            reassessWeaponGoal(mob);
        } else {
            resetFollowRange(mob);
        }
    }

    public static boolean hasGunAttackGoal(PathfinderMob mob) {
        return mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof GunAttackGoal<?>);
    }

    public static void reassessWeaponGoal(PathfinderMob mob) {
        if (mob.level().isClientSide || hasGunAttackGoal(mob)) {
            return;
        }
        for (String tag : mob.getTags()) {
            if (tag.startsWith("RaidMember_")) {
                return;
            }
        }

        AIType aiType = AIType.values()[mob.getRandom().nextInt(AIType.values().length)];
        int aiDifficulty = mob.getRandom().nextInt(4) + 1;

        ItemStack heldItem = mob.getMainHandItem();
        mob.goalSelector.addGoal(2, new GunAttackGoal<>(mob, heldItem, 1.2F, aiType, aiDifficulty));
        mob.addTag("GunAttackAssigned");
        extendFollowRange(mob);
    }

    private static ItemStack createModifiedGun(PathfinderMob mob, Item gun) {
        ItemStack gunStack = new ItemStack(gun);
        if (gun instanceof GunItem gunItem && gunStack.getTag() != null) {
            Gun gunModified = gunItem.getModifiedGun(gunStack);
            gunStack.getTag().putInt("AmmoCount", mob.getRandom().nextInt(gunModified.getReloads().getMaxAmmo()));
        }
        return gunStack;
    }

    public static void extendFollowRange(PathfinderMob mob) {
        if (mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            double additionalRange = 64 - mob.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue();
            AttributeModifier modifier = new AttributeModifier(
                    GUN_FOLLOW_RANGE_MODIFIER_UUID,
                    "Gun follow range modifier",
                    additionalRange,
                    AttributeModifier.Operation.ADDITION
            );
            if (!mob.getAttribute(Attributes.FOLLOW_RANGE).hasModifier(modifier)) {
                mob.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(modifier);
            }
        }
    }

    public static void resetFollowRange(PathfinderMob mob) {
        if (mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            mob.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(GUN_FOLLOW_RANGE_MODIFIER_UUID);
        }
    }
}