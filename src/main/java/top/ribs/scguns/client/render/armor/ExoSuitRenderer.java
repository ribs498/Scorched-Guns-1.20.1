package top.ribs.scguns.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.ribs.scguns.common.exosuit.ExoSuitData;
import top.ribs.scguns.common.exosuit.ExoSuitUpgrade;
import top.ribs.scguns.common.exosuit.ExoSuitUpgradeManager;
import top.ribs.scguns.item.animated.ExoSuitItem;

public class ExoSuitRenderer extends GeoArmorRenderer<ExoSuitItem> {

    public ExoSuitRenderer() {
        super(new ExoSuitModel());
    }
    /**
     * Enum to define different armor component types and their rendering behavior
     */
    public enum ArmorComponentType {
        // Base components - the basic exosuit frame
        BASE(false),

        // Standard armor - medium coverage, replaces base
        STANDARD(true),

        // Heavy armor - full coverage, replaces base
        HEAVY(true),

        LIGHT(false),

        MEDIUM(true),

        // Utility components - attachments and accessories
        UTILITY(false),

        // Special/Future armor types
        STEALTH(false),
        TECH(false);

        private final boolean hidesBase;

        ArmorComponentType(boolean hidesBase) {
            this.hidesBase = hidesBase;
        }

        public boolean shouldHideBase() {
            return hidesBase;
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, ExoSuitItem animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, float red, float green,
                                  float blue, float alpha) {

        ItemStack liveArmorStack = this.getCurrentStack();
        if (this.currentEntity instanceof LivingEntity livingEntity && this.currentSlot != null) {
            liveArmorStack = livingEntity.getItemBySlot(this.currentSlot);
        }

        handleComponentVisibility(bone, liveArmorStack);

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);
    }

    private void handleComponentVisibility(GeoBone bone, ItemStack armorStack) {
        if (armorStack == null || !(armorStack.getItem() instanceof ExoSuitItem)) {
            return;
        }

        String boneName = bone.getName();
        boolean hasPlatingUpgrade = hasUpgradeInSlot(armorStack, 0);
        ArmorComponentType activeArmorType = determineActiveArmorType(armorStack);

        if (isBaseComponent(boneName)) {
            boolean shouldHideBase = activeArmorType.shouldHideBase() && hasPlatingUpgrade;
            bone.setHidden(shouldHideBase);
            return;
        }

        if (isStandardArmorComponent(boneName)) {
            boolean showStandard = hasPlatingUpgrade && activeArmorType == ArmorComponentType.STANDARD;
            bone.setHidden(!showStandard);
            return;
        }

        if (isHeavyArmorComponent(boneName)) {
            boolean showHeavy = hasPlatingUpgrade && activeArmorType == ArmorComponentType.HEAVY;
            bone.setHidden(!showHeavy);
            return;
        }

        if (isLightArmorComponent(boneName)) {
            boolean showLight = hasPlatingUpgrade && activeArmorType == ArmorComponentType.LIGHT;
            bone.setHidden(!showLight);
            return;
        }

        if (isNightVisionComponent(boneName)) {
            boolean showNightVision = hasSpecificHudUpgrade(armorStack, top.ribs.scguns.item.exosuit.NightVisionModuleItem.class);
            bone.setHidden(!showNightVision);
            return;
        }

        if (isTargetTrackerComponent(boneName)) {
            boolean showTargetTracker = hasSpecificHudUpgrade(armorStack, top.ribs.scguns.item.exosuit.TargetTrackerModuleItem.class);
            bone.setHidden(!showTargetTracker);
            return;
        }

        if (isBreathingComponent(boneName)) {
            boolean hasBreathingUpgrade = hasUpgradeOfType(armorStack, "breathing");

            if (!hasBreathingUpgrade) {
                bone.setHidden(true);
                return;
            }
            ItemStack breathingUpgrade = findBreathingUpgrade(armorStack);
            if (!breathingUpgrade.isEmpty()) {
                boolean isRebreather = breathingUpgrade.getItem() instanceof top.ribs.scguns.item.exosuit.RebreatherModuleItem;
                boolean isGasMask = breathingUpgrade.getItem() instanceof top.ribs.scguns.item.exosuit.GasMaskModuleItem;

                if (boneName.equals("breathing_unit_1")) {
                    bone.setHidden(!isRebreather);
                } else if (boneName.equals("breathing_unit_2")) {
                    bone.setHidden(!isGasMask);
                }
            } else {
                bone.setHidden(true);
            }
            return;
        }
        if (isJetpackComponent(boneName)) {
            boolean hasJetpackUpgrade = hasUpgradeOfType(armorStack, "jetpack") || hasJetpackUtilityUpgrade(armorStack);
            bone.setHidden(!hasJetpackUpgrade);
            return;
        }
        if (isPauldronComponent(boneName)) {
            boolean hasPauldronUpgrade = hasUpgradeOfType(armorStack, "pauldron");

            if (!hasPauldronUpgrade) {
                bone.setHidden(true);
                return;
            }

            ItemStack pauldronUpgrade = findPauldronUpgrade(armorStack);
            if (!pauldronUpgrade.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pauldronUpgrade);
                if (upgrade != null) {
                    String pauldronModel = upgrade.getDisplay().getModel();

                    if (boneName.contains("heavy_")) {
                        boolean showHeavyPauldron = pauldronModel.contains("heavy");
                        bone.setHidden(!showHeavyPauldron);
                    }
                    else if (boneName.contains("standard_")) {
                        boolean showStandardPauldron = pauldronModel.contains("standard") || !pauldronModel.contains("heavy");
                        bone.setHidden(!showStandardPauldron);
                    }
                    else {
                        bone.setHidden(false);
                    }
                } else {
                    bone.setHidden(boneName.contains("heavy_"));
                }
            } else {
                bone.setHidden(true);
            }
            return;
        }

        if (isPouchesComponent(boneName)) {
            boolean showPouches = hasUpgradeOfType(armorStack, "pouches");
            if (!showPouches) {
                bone.setHidden(true);
                return;
            }
            ItemStack pouchesUpgrade = findPouchesUpgrade(armorStack);
            if (!pouchesUpgrade.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(pouchesUpgrade);
                if (upgrade != null) {
                    String pouchesModel = upgrade.getDisplay().getModel();

                    if (boneName.contains("heavy_")) {
                        boolean showHeavyPouches = pouchesModel.contains("heavy");
                        bone.setHidden(!showHeavyPouches);
                    } else if (boneName.contains("standard_")) {
                        boolean showStandardPouches = pouchesModel.contains("standard") || !pouchesModel.contains("heavy");
                        bone.setHidden(!showStandardPouches);
                    } else {
                        bone.setHidden(false);
                    }
                } else {
                    bone.setHidden(boneName.contains("heavy_"));
                }
            } else {
                bone.setHidden(true);
            }
            return;
        }
        if (isBackpackComponent(boneName)) {
            boolean showBackpack = hasUpgradeOfType(armorStack, "backpack") || hasUpgradeOfType(armorStack, "utility");
            bone.setHidden(!showBackpack);
            return;
        }

        if (isKneeGuardComponent(boneName)) {
            boolean hasKneeGuardUpgrade = hasUpgradeOfType(armorStack, "knee_guard");
            boolean hasPlatingInKneeGuardSlot = hasPlatingInKneeGuardSlot(armorStack);

            if (!hasKneeGuardUpgrade && !hasPlatingInKneeGuardSlot) {
                bone.setHidden(true);
                return;
            }

            ItemStack kneeGuardUpgrade = findKneeGuardOrPlatingUpgrade(armorStack);
            if (!kneeGuardUpgrade.isEmpty()) {
                ExoSuitUpgrade upgrade;
                if (hasPlatingInKneeGuardSlot && !hasKneeGuardUpgrade) {
                    upgrade = ExoSuitUpgradeManager.getUpgradeForItemInSlot(kneeGuardUpgrade, "knee_guard");
                } else {
                    upgrade = ExoSuitUpgradeManager.getUpgradeForItem(kneeGuardUpgrade);
                }

                if (upgrade != null) {
                    String kneeGuardModel = upgrade.getDisplay().getModel();

                    if (boneName.contains("heavy_")) {
                        boolean showHeavyKneeGuard = kneeGuardModel.contains("heavy");
                        bone.setHidden(!showHeavyKneeGuard);
                    } else if (boneName.contains("standard_")) {
                        boolean showStandardKneeGuard = kneeGuardModel.contains("standard") || !kneeGuardModel.contains("heavy");
                        bone.setHidden(!showStandardKneeGuard);
                    } else {
                        bone.setHidden(false);
                    }
                } else {
                    bone.setHidden(boneName.contains("heavy_"));
                }
            } else {
                bone.setHidden(true);
            }
            return;
        }
        if (isMobilityModuleComponent(boneName)) {
            boolean hasMobilityUpgrade = hasUpgradeOfType(armorStack, "mobility");

            if (!hasMobilityUpgrade) {
                bone.setHidden(true);
                return;
            }

            ItemStack mobilityUpgrade = findMobilityUpgrade(armorStack);
            if (!mobilityUpgrade.isEmpty()) {
                boolean isRabbitModule = mobilityUpgrade.getItem() instanceof top.ribs.scguns.item.exosuit.RabbitModuleItem;
                boolean isShockAbsorberModule = mobilityUpgrade.getItem().toString().contains("shock_absorber");

                if (boneName.contains("rabbit_")) {
                    bone.setHidden(!isRabbitModule);
                } else if (boneName.contains("shock_absorber_")) {
                    bone.setHidden(!isShockAbsorberModule);
                } else {
                    bone.setHidden(false);
                }
            } else {
                bone.setHidden(true);
            }
            return;
        }

        if (isFutureUpgradeBone(boneName)) {
            bone.setHidden(true);
        }
    }

    private ArmorComponentType determineActiveArmorType(ItemStack armorStack) {
        if (hasUpgradeInSlot(armorStack, 0)) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, 0);

            ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
            if (upgrade != null) {
                String modelType = upgrade.getDisplay().getModel();

                return switch (modelType) {
                    case "heavy_plating", "heavy" -> ArmorComponentType.HEAVY;
                    case "standard_plating", "standard" -> ArmorComponentType.STANDARD;
                    case "light_plating", "light" -> ArmorComponentType.LIGHT;
                    case "medium_plating", "medium" -> ArmorComponentType.MEDIUM;
                    default -> ArmorComponentType.BASE;
                };
            }
        }

        return ArmorComponentType.BASE;
    }

    private boolean hasUpgradeInSlot(ItemStack armorStack, int slot) {
        try {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            boolean hasUpgrade = !upgradeItem.isEmpty();

            if (hasUpgrade) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                return upgrade != null;
            }

            return false;
        } catch (Exception e) {
            System.err.println("ExoSuit: Error checking upgrade slot " + slot + ": " + e.getMessage());
            return false;
        }
    }

    private boolean hasUpgradeOfType(ItemStack armorStack, String upgradeType) {
        try {
            for (int slot = 0; slot < 4; slot++) {
                ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                if (!upgradeItem.isEmpty()) {
                    ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                    if (upgrade != null && upgrade.getType().equals(upgradeType)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean hasPlatingInKneeGuardSlot(ItemStack armorStack) {
        try {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, 1);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                return upgrade != null && upgrade.getType().equals("plating");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    private ItemStack findKneeGuardOrPlatingUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null) {
                    if (upgrade.getType().equals("knee_guard")) {
                        return upgradeItem;
                    }
                    if (slot == 1 && upgrade.getType().equals("plating")) {
                        return upgradeItem;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private boolean hasSpecificHudUpgrade(ItemStack armorStack, Class<?> upgradeClass) {
        try {
            for (int slot = 0; slot < 4; slot++) {
                ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
                if (!upgradeItem.isEmpty()) {
                    ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                    if (upgrade != null && upgrade.getType().equals("hud") &&
                            upgradeClass.isInstance(upgradeItem.getItem())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack findBreathingUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("breathing")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findPauldronUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pauldron")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }
    private ItemStack findPouchesUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("pouches")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findMobilityUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("mobility")) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }



    private boolean isBaseComponent(String boneName) {
        return boneName.equals("base_helmet") ||
                boneName.equals("base_torso") ||
                boneName.equals("base_right_arm") ||
                boneName.equals("base_left_arm") ||
                boneName.equals("base_right_leg") ||
                boneName.equals("base_left_leg") ||
                boneName.equals("base_right_boot") ||
                boneName.equals("base_left_boot");
    }

    private boolean isStandardArmorComponent(String boneName) {
        return boneName.equals("standard_helmet") ||
                boneName.equals("standard_torso") ||
                boneName.equals("standard_right_arm") ||
                boneName.equals("standard_left_arm") ||
                boneName.equals("standard_right_leg") ||
                boneName.equals("standard_left_leg") ||
                boneName.equals("standard_right_boot") ||
                boneName.equals("standard_left_boot");
    }

    private boolean isHeavyArmorComponent(String boneName) {
        return boneName.equals("heavy_helmet") ||
                boneName.equals("heavy_torso") ||
                boneName.equals("heavy_right_arm") ||
                boneName.equals("heavy_left_arm") ||
                boneName.equals("heavy_right_leg") ||
                boneName.equals("heavy_left_leg") ||
                boneName.equals("heavy_right_boot") ||
                boneName.equals("heavy_left_boot");
    }

    private boolean isLightArmorComponent(String boneName) {
        return boneName.equals("light_helmet") ||
                boneName.equals("light_torso") ||
                boneName.equals("light_right_arm") ||
                boneName.equals("light_left_arm") ||
                boneName.equals("light_right_leg") ||
                boneName.equals("light_left_leg") ||
                boneName.equals("light_right_boot") ||
                boneName.equals("light_left_boot");
    }
    private boolean isJetpackComponent(String boneName) {
        return boneName.equals("jetpack");
    }

    /**
     * Checks if there's a jetpack-specific utility upgrade
     */
    private boolean hasJetpackUtilityUpgrade(ItemStack armorStack) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(armorStack, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility")) {
                    String model = upgrade.getDisplay().getModel();
                    return model != null && model.contains("jetpack");
                }
            }
        }
        return false;
    }
    private boolean isNightVisionComponent(String boneName) {
        return boneName.equals("night_vision");
    }

    private boolean isTargetTrackerComponent(String boneName) {
        return boneName.equals("target_tracker");
    }

    private boolean isBreathingComponent(String boneName) {
        return boneName.equals("breathing_unit_1") || boneName.equals("breathing_unit_2");
    }

    private boolean isPauldronComponent(String boneName) {
        return boneName.equals("heavy_right_pauldron") ||
                boneName.equals("heavy_left_pauldron") ||
                boneName.equals("standard_right_pauldron") ||
                boneName.equals("standard_left_pauldron");
    }

    private boolean isPouchesComponent(String boneName) {
        return boneName.equals("standard_pouches") ||
                boneName.equals("heavy_pouches");
    }

    private boolean isBackpackComponent(String boneName) {
        return boneName.equals("backpack");
    }

    // NEW: Check for knee guard components
    private boolean isKneeGuardComponent(String boneName) {
        return boneName.equals("standard_left_knee_guard") ||
                boneName.equals("standard_right_knee_guard") ||
                boneName.equals("heavy_left_knee_guard") ||
                boneName.equals("heavy_right_knee_guard");
    }

    // NEW: Check for mobility module components (rabbit and shock absorber)
    private boolean isMobilityModuleComponent(String boneName) {
        return boneName.equals("rabbit_left_module") ||
                boneName.equals("rabbit_right_module") ||
                boneName.equals("shock_absorber_left_module") ||
                boneName.equals("shock_absorber_right_module");
    }

    private boolean isFutureUpgradeBone(String boneName) {
        if (boneName.equals("night_vision") || boneName.equals("target_tracker") ||
                boneName.equals("breathing_unit_1") || boneName.equals("breathing_unit_2")) {
            return false;
        }

        return boneName.contains("plating_") ||
                boneName.contains("upgrade_") ||
                boneName.contains("power_") ||
                boneName.contains("enhancement_") ||
                boneName.contains("stealth_") ||
                boneName.contains("tech_") ||
                boneName.contains("utility_");
    }
}