package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.GunModel;
import top.ribs.scguns.client.GunRenderType;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.render.gun.ModelOverrides;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.properties.SightAnimation;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GrenadeItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.item.attachment.IBarrel;
import top.ribs.scguns.item.attachment.impl.Scope;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class GunRenderingHandler {
    protected static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    private static GunRenderingHandler instance;

    public static GunRenderingHandler get() {
        if (instance == null) {
            instance = new GunRenderingHandler();
        }
        return instance;
    }

    private final Random random = new Random();
    private final Set<Integer> entityIdForMuzzleFlash = new HashSet<>();
    private final Set<Integer> entityIdForDrawnMuzzleFlash = new HashSet<>();
    private final Map<Integer, Float> entityIdToRandomValue = new HashMap<>();

    private int sprintTransition;
    private int prevSprintTransition;
    private int sprintCooldown;
    private float sprintIntensity;

    private float banzaiProgress;
    private float banzaiImpactProgress;
    private float prevBanzaiImpactProgress;
    private float sprintToBanzaiProgress;
    private float prevSprintToBanzaiProgress;
    private float offhandTranslate;
    private float prevOffhandTranslate;

    private Field equippedProgressMainHandField;
    private Field prevEquippedProgressMainHandField;

    private float immersiveRoll;
    private float prevImmersiveRoll;
    private float fallSway;
    private float prevFallSway;
    //melee
    private float meleeProgress;
    private float prevMeleeProgress;
    private boolean isMeleeAttacking;
    private long meleeStartTime;
    public static final float MELEE_DURATION = 400.0f;
    public float thirdPersonMeleeProgress;
    public float prevThirdPersonMeleeProgress;
    private boolean isThirdPersonMeleeAttacking;
    private long thirdPersonMeleeStartTime;
    public static final float THIRD_PERSON_MELEE_DURATION = 400.0f;
    @Nullable
    private ItemStack renderingWeapon;

    private GunRenderingHandler() {}

    @Nullable
    public ItemStack getRenderingWeapon() {
        return this.renderingWeapon;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        updateState();
        handleClientTick();
    }
    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            return;
        }

        handleRenderTick();
    }

    private void updateState() {
        this.updateSprinting();
        this.updateMuzzleFlash();
        this.updateOffhandTranslate();
        this.updateImmersiveCamera();
    }

    private void handleClientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive()) {
            return;
        }

        Player player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }

        if (heldItem.getItem() instanceof GunItem gunItem) {
//            if (gunItem.hasBayonetWithBanzai(heldItem)) {
//                logPlayerSpeed(player);
//            }

            if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND && heldItem.getItem() instanceof GrenadeItem) {
                return;
            }

            renderCooldownIndicator(mc, player, heldItem);
        }
        this.updateMelee();
    }

    private void handleRenderTick() {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive()) {
            return;
        }

        Player player = mc.player;
        if (player == null || Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.isEmpty() || (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND && heldItem.getItem() instanceof GrenadeItem && !((GrenadeItem) heldItem.getItem()).canCook())) {
            return;
        }

        if (heldItem.getItem() instanceof GunItem) {
            renderCooldownIndicator(mc, player, heldItem);
        }
    }

//    private void logPlayerSpeed(Player player) {
//        double speed = Math.sqrt(player.getDeltaMovement().x * player.getDeltaMovement().x + player.getDeltaMovement().z * player.getDeltaMovement().z);
//        speed *= 10;
//        //System.out.printf("Player speed: %.3f%n", speed);
//    }

    private void renderCooldownIndicator(Minecraft mc, Player player, ItemStack heldItem) {
        if (Config.CLIENT.display.cooldownIndicator.get()) {
            Gun gun = ((GunItem) heldItem.getItem()).getGun();
            if (!gun.getGeneral().isAuto()) {
                float coolDown = player.getCooldowns().getCooldownPercent(heldItem.getItem(), mc.getFrameTime());
                if (coolDown > 0.0F) {
                    float scale = 3;
                    Window window = mc.getWindow();
                    int i = (int) ((window.getGuiScaledHeight() / 2 - 7 - 60) / scale);
                    int j = (int) Math.ceil(((double) window.getGuiScaledWidth() / 2 - 8 * scale) / scale);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);

                    PoseStack stack = new PoseStack();
                    stack.scale(scale, scale, scale);
                    GuiGraphics guiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
                    int progress = (int) Math.ceil((coolDown + 0.05) * 17.0F) - 1;
                    guiGraphics.blit(GUI_ICONS_LOCATION, j, i, 36, 94, 16, 4, 256, 256);
                    guiGraphics.blit(GUI_ICONS_LOCATION, j, i, 52, 94, progress, 4, 256, 256);

                    RenderSystem.disableBlend();
                }
            }
        }
    }

    private void updateSprinting() {
        this.prevSprintTransition = this.sprintTransition;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isSprinting() && !ModSyncedDataKeys.SHOOTING.getValue(mc.player) && !ModSyncedDataKeys.RELOADING.getValue(mc.player) && !AimingHandler.get().isAiming() && this.sprintCooldown == 0) {
            if (this.sprintTransition < 5) {
                this.sprintTransition++;
            }
        } else if (this.sprintTransition > 0) {
            this.sprintTransition--;
        }

        if (this.sprintCooldown > 0) {
            this.sprintCooldown--;
        }
    }

    private void updateMuzzleFlash() {
        this.entityIdForMuzzleFlash.removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdToRandomValue.keySet().removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdForDrawnMuzzleFlash.clear();
        this.entityIdForDrawnMuzzleFlash.addAll(this.entityIdForMuzzleFlash);
    }

    private void updateOffhandTranslate() {
        this.prevOffhandTranslate = this.offhandTranslate;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        boolean down = false;
        ItemStack heldItem = mc.player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            down = (!modifiedGun.getGeneral().getGripType().getHeldAnimation().canRenderOffhandItem() || ModSyncedDataKeys.RELOADING.getValue(mc.player));
        }
        float direction = down ? -0.6F : 0.6F;
        this.offhandTranslate = Mth.clamp(this.offhandTranslate + direction, -1.0F, 1.0F);
    }

    private void updateImmersiveCamera() {
        this.prevImmersiveRoll = this.immersiveRoll;
        this.prevFallSway = this.fallSway;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();
        float targetAngle = heldItem.getItem() instanceof GunItem || !Config.CLIENT.display.restrictCameraRollToWeapons.get() ? mc.player.input.leftImpulse : 0F;
        float speed = mc.player.input.leftImpulse != 0 ? 0.1F : 0.15F;
        this.immersiveRoll = Mth.lerp(speed, this.immersiveRoll, targetAngle);

        float deltaY = (float) Mth.clamp((mc.player.yo - mc.player.getY()), -1.0, 1.0);
        deltaY *= (float) (1.0 - AimingHandler.get().getNormalisedAdsProgress());
        deltaY *= (float) (1.0 - (Mth.abs(mc.player.getXRot()) / 90.0F));
        this.fallSway = Mth.approach(this.fallSway, deltaY * 60F * Config.CLIENT.display.swaySensitivity.get().floatValue(), 10.0F);

        float intensity = mc.player.isSprinting() ? 0.75F : 1.0F;
        this.sprintIntensity = Mth.approach(this.sprintIntensity, intensity, 0.1F);
    }

    private void updateMelee() {
        float prevBanzaiProgress = banzaiProgress;
        prevSprintToBanzaiProgress = sprintToBanzaiProgress;
        prevBanzaiImpactProgress = banzaiImpactProgress;

        if (MeleeAttackHandler.isBanzaiActive()) {
            banzaiProgress = Mth.clamp(banzaiProgress + 0.3f, 0.0f, 1.0f);
            sprintToBanzaiProgress = Mth.clamp(sprintToBanzaiProgress + 0.2f, 0.0f, 1.0f);
        } else {
            banzaiProgress = Mth.clamp(banzaiProgress - 0.3f, 0.0f, 1.0f);
            sprintToBanzaiProgress = Mth.clamp(sprintToBanzaiProgress - 0.2f, 0.0f, 1.0f);
        }

        long currentTime = System.currentTimeMillis();

        // Handle melee attacking progress
        if (isMeleeAttacking) {
            long elapsed = currentTime - meleeStartTime;
            prevMeleeProgress = meleeProgress;
            meleeProgress = Math.min(elapsed / MELEE_DURATION, 1.0f);
            if (meleeProgress >= 1.0f) {
                isMeleeAttacking = false;
                meleeProgress = 0;
                prevMeleeProgress = 0;
                ModSyncedDataKeys.MELEE.setValue(Minecraft.getInstance().player, false);
            }
        } else {
            meleeProgress = 0;
            prevMeleeProgress = 0;
        }

        // Handle third person melee attacking progress
        if (isThirdPersonMeleeAttacking) {
            long elapsed = currentTime - thirdPersonMeleeStartTime;
            prevThirdPersonMeleeProgress = thirdPersonMeleeProgress;
            thirdPersonMeleeProgress = Math.min(elapsed / THIRD_PERSON_MELEE_DURATION, 1.0f);
            if (thirdPersonMeleeProgress >= 1.0f) {
                isThirdPersonMeleeAttacking = false;
                thirdPersonMeleeProgress = 0;
                prevThirdPersonMeleeProgress = 0;
                ModSyncedDataKeys.MELEE.setValue(Minecraft.getInstance().player, false);
            }
        } else {
            thirdPersonMeleeProgress = 0;
            prevThirdPersonMeleeProgress = 0;
        }

        if (banzaiImpactProgress > 0.0f) {
            banzaiImpactProgress = Mth.clamp(banzaiImpactProgress - 0.1f, 0.0f, 1.0f);
        }
    }
    private void applySprintingTransforms(Gun modifiedGun, HumanoidArm hand, PoseStack poseStack, float partialTicks) {
        if (Config.CLIENT.display.sprintAnimation.get() && modifiedGun.getGeneral().getGripType().getHeldAnimation().canApplySprintingAnimation()) {
            float leftHanded = hand == HumanoidArm.LEFT ? -1 : 1;
            float transition = (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
            transition = (float) Math.sin((transition * Math.PI) / 2);
            float sprintToBanzai = Mth.lerp(partialTicks, prevSprintToBanzaiProgress, sprintToBanzaiProgress);
            transition *= (1.0f - sprintToBanzai);
            poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(45F * leftHanded * transition));
            poseStack.mulPose(Axis.XP.rotationDegrees(-25F * transition));
        }
    }

    private void applyBanzaiTransforms(PoseStack poseStack, float partialTicks) {
        float sprintToBanzai = Mth.lerp(partialTicks, prevSprintToBanzaiProgress, sprintToBanzaiProgress);
        if (sprintToBanzai > 0.0f) {
            poseStack.translate(0, -0.1 * sprintToBanzai, 0.2 * sprintToBanzai);
            poseStack.mulPose(Axis.XP.rotationDegrees(10F * sprintToBanzai));
        }

        float impactProgress = Mth.lerp(partialTicks, prevBanzaiImpactProgress, banzaiImpactProgress);
        if (impactProgress > 0.0f) {
            poseStack.translate(0, 0, 0.1 * impactProgress);
        }
    }
    public void triggerBanzaiImpact() {
        this.banzaiImpactProgress = 1.0f;
    }
    public void startBayonetStabAnimation() {
        this.isMeleeAttacking = true;
        this.meleeStartTime = System.currentTimeMillis();
        this.meleeProgress = 0;
        this.prevMeleeProgress = 0;
        ModSyncedDataKeys.MELEE.setValue(Minecraft.getInstance().player, true);
    }
    public void startThirdPersonMeleeAnimation() {
        this.isThirdPersonMeleeAttacking = true;
        this.thirdPersonMeleeStartTime = System.currentTimeMillis();
        this.thirdPersonMeleeProgress = 0;
        this.prevThirdPersonMeleeProgress = 0;
        ModSyncedDataKeys.MELEE.setValue(Minecraft.getInstance().player, true);
    }

    public boolean isThirdPersonMeleeAttacking() {
        return this.isThirdPersonMeleeAttacking;
    }
    private void applyMeleeTransforms(PoseStack poseStack, float partialTicks) {
        if (isMeleeAttacking) {
            float progress = Mth.lerp(partialTicks, prevMeleeProgress, meleeProgress);
            assert Minecraft.getInstance().player != null;
            ItemStack heldItem = Minecraft.getInstance().player.getMainHandItem();
            boolean isBayonetEquipped = heldItem.getItem() instanceof GunItem && ((GunItem) heldItem.getItem()).hasBayonet(heldItem);

            if (isBayonetEquipped) {
                if (progress < 0.4f) {
                    // Quicker stab part
                    float stabProgress = progress / 0.4f;
                    poseStack.translate(0, 0, -0.3 * stabProgress);
                    poseStack.mulPose(Axis.XP.rotationDegrees(15F * stabProgress));
                } else {
                    // Longer return part
                    float returnProgress = (progress - 0.4f) / 0.6f;
                    poseStack.translate(0, 0, -0.3 * (1 - returnProgress));
                    poseStack.mulPose(Axis.XP.rotationDegrees(15F * (1 - returnProgress)));
                }
            } else {
                if (progress < 0.33f) {
                    float raiseProgress = progress / 0.33f;
                    poseStack.translate(0, 0.35 * raiseProgress, 0);
                    poseStack.translate(0, 0, 0.1 * raiseProgress);
                    poseStack.mulPose(Axis.XP.rotationDegrees(35F * raiseProgress));
                } else if (progress < 0.66f) {
                    float swingProgress = (progress - 0.33f) / 0.33f;
                    poseStack.translate(0, 0.35 - 0.7 * swingProgress, 0);
                    poseStack.translate(0, 0.1 - 0.2 * swingProgress, 0);
                    poseStack.mulPose(Axis.XP.rotationDegrees(35F - 70F * swingProgress));
                } else {
                    float returnProgress = (progress - 0.66f) / 0.34f;
                    poseStack.translate(0, -0.35 * (1 - returnProgress), 0);
                    poseStack.translate(0, 0, -0.1 * (1 - returnProgress));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-35F * (1 - returnProgress)));
                }
            }
        }
    }
    public void startMeleeAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - meleeStartTime >= MELEE_DURATION) {
            this.isMeleeAttacking = true;
            this.meleeStartTime = currentTime;
            this.meleeProgress = 0;
            this.prevMeleeProgress = 0;
            ModSyncedDataKeys.MELEE.setValue(Minecraft.getInstance().player, true);
        }
    }
    @SubscribeEvent
    public void onRenderOverlay(RenderHandEvent event) {
        PoseStack poseStack = event.getPoseStack();

        boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? event.getHand() == InteractionHand.MAIN_HAND : event.getHand() == InteractionHand.OFF_HAND;
        HumanoidArm hand = right ? HumanoidArm.RIGHT : HumanoidArm.LEFT;

        ItemStack heldItem = event.getItemStack();
        if (event.getHand() == InteractionHand.OFF_HAND) {
            if (heldItem.getItem() instanceof GunItem) {
                event.setCanceled(true);
                return;
            }

            float offhand = 1.0F - Mth.lerp(event.getPartialTick(), this.prevOffhandTranslate, this.offhandTranslate);
            poseStack.translate(0, offhand * -0.6F, 0);

            Player player = Minecraft.getInstance().player;
            if (player != null && player.getMainHandItem().getItem() instanceof GunItem) {
                Gun modifiedGun = ((GunItem) player.getMainHandItem().getItem()).getModifiedGun(player.getMainHandItem());
                if (!modifiedGun.getGeneral().getGripType().getHeldAnimation().canRenderOffhandItem()) {
                    return;
                }
            }

            poseStack.translate(0, -1 * AimingHandler.get().getNormalisedAdsProgress(), 0);
        }

        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }

        event.setCanceled(true);

        ItemStack overrideModel = ItemStack.EMPTY;
        if (heldItem.getTag() != null) {
            if (heldItem.getTag().contains("Model", Tag.TAG_COMPOUND)) {
                overrideModel = ItemStack.of(heldItem.getTag().getCompound("Model"));
            }
        }

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(overrideModel.isEmpty() ? heldItem : overrideModel, player.level(), player, 0);
        float scaleX = model.getTransforms().firstPersonRightHand.scale.x();
        float scaleY = model.getTransforms().firstPersonRightHand.scale.y();
        float scaleZ = model.getTransforms().firstPersonRightHand.scale.z();
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        float translateY = model.getTransforms().firstPersonRightHand.translation.y();
        float translateZ = model.getTransforms().firstPersonRightHand.translation.z();

        poseStack.pushPose();

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if (AimingHandler.get().getNormalisedAdsProgress() > 0 && modifiedGun.canAimDownSight()) {
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                double xOffset = translateX;
                double yOffset = translateY;
                double zOffset = translateZ;

                xOffset -= 0.5 * scaleX;
                yOffset -= 0.5 * scaleY;
                zOffset -= 0.5 * scaleZ;

                Vec3 gunOrigin = PropertyHelper.getModelOrigin(heldItem, PropertyHelper.GUN_DEFAULT_ORIGIN);
                xOffset += gunOrigin.x * 0.0625 * scaleX;
                yOffset += gunOrigin.y * 0.0625 * scaleY;
                zOffset += gunOrigin.z * 0.0625 * scaleZ;

                Scope scope = Gun.getScope(heldItem);
                if (modifiedGun.canAttachType(IAttachment.Type.SCOPE) && scope != null) {
                    Vec3 scopePosition = PropertyHelper.getAttachmentPosition(heldItem, modifiedGun, IAttachment.Type.SCOPE).subtract(gunOrigin);
                    xOffset += scopePosition.x * 0.0625 * scaleX;
                    yOffset += scopePosition.y * 0.0625 * scaleY;
                    zOffset += scopePosition.z * 0.0625 * scaleZ;

                    ItemStack scopeStack = Gun.getScopeStack(heldItem);
                    Vec3 scopeOrigin = PropertyHelper.getModelOrigin(scopeStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                    Vec3 scopeCamera = PropertyHelper.getScopeCamera(scopeStack).subtract(scopeOrigin);
                    Vec3 scopeScale = PropertyHelper.getAttachmentScale(heldItem, modifiedGun, IAttachment.Type.SCOPE);
                    xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
                    yOffset += scopeCamera.y * 0.0625 * scaleY * scopeScale.y;
                    zOffset += scopeCamera.z * 0.0625 * scaleZ * scopeScale.z;
                } else {
                    Vec3 ironSightCamera = PropertyHelper.getIronSightCamera(heldItem, modifiedGun, gunOrigin).subtract(gunOrigin);
                    xOffset += ironSightCamera.x * 0.0625 * scaleX;
                    yOffset += ironSightCamera.y * 0.0625 * scaleY;
                    zOffset += ironSightCamera.z * 0.0625 * scaleZ;

                    if (PropertyHelper.isLegacyIronSight(heldItem)) {
                        zOffset += 0.72;
                    }
                }

                float side = right ? 1.0F : -1.0F;
                double time = AimingHandler.get().getNormalisedAdsProgress();
                double transition = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getSightCurve().apply(time);

                poseStack.translate(-0.56 * side * transition, 0.52 * transition, 0.72 * transition);

                poseStack.translate(-xOffset * side * transition, -yOffset * transition, -zOffset * transition);
            }
        }

        this.applyBobbingTransforms(poseStack, event.getPartialTick());
        this.applyBanzaiTransforms(poseStack, event.getPartialTick());

        float equipProgress = this.getEquipProgress(event.getPartialTick());
        poseStack.mulPose(Axis.XP.rotationDegrees(equipProgress * -50F));

        this.renderReloadArm(poseStack, event.getMultiBufferSource(), event.getPackedLight(), modifiedGun, heldItem, hand, translateX);

        int offset = right ? 1 : -1;
        poseStack.translate(0.56 * offset, -0.52, -0.72);

        this.applyAimingTransforms(poseStack, heldItem, modifiedGun, translateX, translateY, translateZ, offset);
        this.applySwayTransforms(poseStack, modifiedGun, player, translateX, translateY, translateZ, event.getPartialTick());
        this.applySprintingTransforms(modifiedGun, hand, poseStack, event.getPartialTick());
        this.applyRecoilTransforms(poseStack, heldItem, modifiedGun);
        this.applyReloadTransforms(poseStack, event.getPartialTick());
        this.applyShieldTransforms(poseStack, player, modifiedGun, event.getPartialTick());

        this.applyMeleeTransforms(poseStack, event.getPartialTick());

        int blockLight = player.isOnFire() ? 15 : player.level().getBrightness(LightLayer.BLOCK, BlockPos.containing(player.getEyePosition(event.getPartialTick())));
        blockLight += (this.entityIdForMuzzleFlash.contains(player.getId()) ? 3 : 0);
        blockLight = Math.min(blockLight, 15);
        int packedLight = LightTexture.pack(blockLight, player.level().getBrightness(LightLayer.SKY, BlockPos.containing(player.getEyePosition(event.getPartialTick()))));

        poseStack.pushPose();
        modifiedGun.getGeneral().getGripType().getHeldAnimation().renderFirstPersonArms(Minecraft.getInstance().player, hand, heldItem, poseStack, event.getMultiBufferSource(), packedLight, event.getPartialTick());
        poseStack.popPose();

        ItemDisplayContext display = right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        this.renderWeapon(Minecraft.getInstance().player, heldItem, display, event.getPoseStack(), event.getMultiBufferSource(), packedLight, event.getPartialTick());

        poseStack.popPose();
    }
    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event) {
        if (!event.isClient()) return;

        this.sprintTransition = 0;
        this.sprintCooldown = 20;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);

        // Ensure the shooter is a player
        if (event.getShooter() instanceof Player) {
            if (modifiedGun.getDisplay().getFlash() != null) {
                this.showMuzzleFlashForPlayer(event.getShooter().getId());
            }
        }
    }

    public void showMuzzleFlashForPlayer(int entityId) {
        this.entityIdForMuzzleFlash.add(entityId);
        this.entityIdToRandomValue.put(entityId, this.random.nextFloat());
    }



    @SubscribeEvent
    public void onComputeFov(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov())
            return;

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if (!modifiedGun.canAimDownSight())
            return;

        if (AimingHandler.get().getNormalisedAdsProgress() <= 0)
            return;

        double time = AimingHandler.get().getNormalisedAdsProgress();
        SightAnimation sightAnimation = PropertyHelper.getSightAnimations(heldItem, modifiedGun);
        time = sightAnimation.getViewportCurve().apply(time);

        double viewportFov = PropertyHelper.getViewportFov(heldItem, modifiedGun);
        double newFov = viewportFov > 0 ? viewportFov : event.getFOV();
        event.setFOV(Mth.lerp(time, event.getFOV(), newFov));
    }

    private void applyBobbingTransforms(PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.bobView().get() && mc.getCameraEntity() instanceof Player player) {
            float deltaDistanceWalked = player.walkDist - player.walkDistO;
            float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
            float bobbing = Mth.lerp(partialTicks, player.oBob, player.bob);

            poseStack.mulPose(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F)));
            poseStack.translate(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 0.5F), -(-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * bobbing)), 0.0D);

            bobbing *= (float) (player.isSprinting() ? 8.0 : 4.0);
            bobbing *= Config.CLIENT.display.bobbingIntensity.get();

            double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * this.sprintIntensity;
            poseStack.mulPose(Axis.ZP.rotationDegrees((Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F) * (float) invertZoomProgress));
            poseStack.mulPose(Axis.XP.rotationDegrees((Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F) * (float) invertZoomProgress));
        }
    }

    private void applyAimingTransforms(PoseStack poseStack, ItemStack heldItem, Gun modifiedGun, float x, float y, float z, int offset) {
        if (!Config.CLIENT.display.oldAnimations.get()) {
            poseStack.translate(x * offset, y, z);
            poseStack.translate(0, -0.25, 0.25);
            float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180F));
            aiming = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getAimTransformCurve().apply(aiming);
            poseStack.mulPose(Axis.ZP.rotationDegrees(aiming * 10F * offset));
            poseStack.mulPose(Axis.XP.rotationDegrees(aiming * 5F));
            poseStack.mulPose(Axis.YP.rotationDegrees(aiming * 5F * offset));
            poseStack.translate(0, 0.25, -0.25);
            poseStack.translate(-x * offset, -y, -z);
        }
    }

    private void applySwayTransforms(PoseStack poseStack, Gun modifiedGun, LocalPlayer player, float x, float y, float z, float partialTicks)
    {
        if(Config.CLIENT.display.weaponSway.get() && player != null)
        {
            poseStack.translate(x, y, z);

            double zOffset = modifiedGun.getGeneral().getGripType().getHeldAnimation().getFallSwayZOffset();
            poseStack.translate(0, -0.25, zOffset);
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevFallSway, this.fallSway)));
            poseStack.translate(0, 0.25, -zOffset);

            float bobPitch = Mth.rotLerp(partialTicks, player.xBobO, player.xBob);
            float headPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
            float swayPitch = headPitch - bobPitch;
            swayPitch *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getPitchRotation().rotationDegrees(swayPitch * Config.CLIENT.display.swaySensitivity.get().floatValue()));

            float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
            float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
            float swayYaw = headYaw - bobYaw;
            swayYaw *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getYawRotation().rotationDegrees(swayYaw * Config.CLIENT.display.swaySensitivity.get().floatValue()));

            poseStack.translate(-x, -y, -z);
        }
    }

    private void applyReloadTransforms(PoseStack poseStack, float partialTicks) {
        float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
        poseStack.translate(0, -0.35 * reloadProgress, 0);
        poseStack.translate(0, 0, -0.1 * reloadProgress);
        poseStack.mulPose(Axis.XP.rotationDegrees(-35F * reloadProgress));
    }
    private void applyRecoilTransforms(PoseStack poseStack, ItemStack item, Gun gun)
    {
        double recoilNormal = RecoilHandler.get().getGunRecoilNormal();
        if(Gun.hasAttachmentEquipped(item, gun, IAttachment.Type.SCOPE))
        {
            recoilNormal -= recoilNormal * (0.5 * AimingHandler.get().getNormalisedAdsProgress());
        }
        float kickReduction = 1.0F - GunModifierHelper.getKickReduction(item);
        float recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(item);
        double kick = gun.getGeneral().getRecoilKick() * 0.0625 * recoilNormal * RecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilLift = (float) (gun.getGeneral().getRecoilAngle() * recoilNormal) * (float) RecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilSwayAmount = (float) (2F + 1F * (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
        float recoilSway = (float) ((RecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2F) * recoilNormal);
        poseStack.translate(0, 0, kick * kickReduction);
        poseStack.translate(0, 0, 0.15);
        poseStack.mulPose(Axis.YP.rotationDegrees(recoilSway * recoilReduction));
        poseStack.mulPose(Axis.ZP.rotationDegrees(recoilSway * recoilReduction));
        poseStack.mulPose(Axis.XP.rotationDegrees(recoilLift * recoilReduction));
        poseStack.translate(0, 0, -0.15);
    }

    private void applyShieldTransforms(PoseStack poseStack, LocalPlayer player, Gun modifiedGun, float partialTick) {
        if(player.isUsingItem() && player.getOffhandItem().getItem() == Items.SHIELD && (modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED || modifiedGun.getGeneral().getGripType() == GripType.TWO_HANDED_ONE_HANDED)){
            double time = Mth.clamp((player.getTicksUsingItem() + partialTick), 0.0, 4.0) / 4.0;
            poseStack.translate(0, 0.35 * time, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(45F * (float) time));
        }
    }

    public void applyWeaponScale(ItemStack heldItem, PoseStack stack) {
        if (heldItem.getTag() != null) {
            CompoundTag compound = heldItem.getTag();
            if (compound.contains("Scale", Tag.TAG_FLOAT)) {
                float scale = compound.getFloat("Scale");
                stack.scale(scale, scale, scale);
            }
        }
    }

    public boolean renderWeapon(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks) {
        if (stack.getItem() instanceof GunItem) {
            poseStack.pushPose();

            ItemStack model = ItemStack.EMPTY;
            if (stack.getTag() != null && stack.getTag().contains("Model", Tag.TAG_COMPOUND)) {
                model = ItemStack.of(stack.getTag().getCompound("Model"));
            }

            RenderUtil.applyTransformType(stack, poseStack, display, entity);

            this.renderingWeapon = stack;
            this.renderGun(entity, display, model.isEmpty() ? stack : model, poseStack, renderTypeBuffer, light, partialTicks);
            this.renderAttachments(entity, display, stack, poseStack, renderTypeBuffer, light, partialTicks);
            this.renderingWeapon = null;
            renderMuzzleFlash(entity, poseStack, renderTypeBuffer, stack, display, partialTicks);
            poseStack.popPose();
            return true;
        }
        return false;
    }

    private void renderGun(@Nullable LivingEntity entity, ItemDisplayContext display, ItemStack stack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks) {
        if (ModelOverrides.hasModel(stack)) {
            IOverrideModel model = ModelOverrides.getModel(stack);
            if (model != null) {
                model.render(partialTicks, display, stack, ItemStack.EMPTY, entity, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
            }
        } else {
            Level level = entity != null ? entity.level() : null;
            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, 0);
            Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, bakedModel);
        }
    }

    private void renderAttachments(@Nullable LivingEntity entity, ItemDisplayContext display, ItemStack stack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks) {
        if (stack.getItem() instanceof GunItem) {
            Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            CompoundTag gunTag = stack.getOrCreateTag();
            CompoundTag attachments = gunTag.getCompound("Attachments");

            Set<Item> customAttachments = Set.of(
                    ModItems.SILENCER.get(),
                    ModItems.ADVANCED_SILENCER.get(),
                    ModItems.MUZZLE_BRAKE.get(),
                    ModItems.VERTICAL_GRIP.get(),
                    ModItems.LIGHT_GRIP.get(),
                    ModItems.IRON_BAYONET.get(),
                    ModItems.DIAMOND_BAYONET.get(),
                    ModItems.NETHERITE_BAYONET.get()
            );

            for (String tagKey : attachments.getAllKeys()) {
                IAttachment.Type type = IAttachment.Type.byTagKey(tagKey);
                if (type != null && modifiedGun.canAttachType(type)) {
                    ItemStack attachmentStack = Gun.getAttachment(type, stack);
                    if (customAttachments.contains(attachmentStack.getItem())) {
                        continue;
                    }
                    if (!attachmentStack.isEmpty()) {
                        poseStack.pushPose();
                        Vec3 origin = PropertyHelper.getModelOrigin(attachmentStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                        poseStack.translate(-origin.x * 0.0625, -origin.y * 0.0625, -origin.z * 0.0625);
                        Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
                        poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625, gunOrigin.z * 0.0625);
                        Vec3 translation = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
                        poseStack.translate(translation.x * 0.0625, translation.y * 0.0625, translation.z * 0.0625);
                        Vec3 scale = PropertyHelper.getAttachmentScale(stack, modifiedGun, type);
                        Vec3 center = origin.subtract(8, 8, 8).scale(0.0625);
                        poseStack.translate(center.x, center.y, center.z);
                        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
                        poseStack.translate(-center.x, -center.y, -center.z);
                        IOverrideModel model = ModelOverrides.getModel(attachmentStack);
                        if (model != null) {
                            model.render(partialTicks, display, attachmentStack, stack, entity, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
                        } else {
                            Level level = entity != null ? entity.level() : null;
                            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(attachmentStack, level, entity, 0);
                            Minecraft.getInstance().getItemRenderer().render(attachmentStack, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, GunModel.wrap(bakedModel));
                        }

                        poseStack.popPose();
                    }
                }
            }
        }
    }
    private void renderMuzzleFlash(@Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, ItemStack weapon, ItemDisplayContext display, float partialTicks) {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if (modifiedGun.getDisplay().getFlash() == null) return;

        // Ensure the muzzle flash is not rendered for non-player entities
        if (!(entity instanceof Player)) return;

        if (display != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && display != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND && display != ItemDisplayContext.FIRST_PERSON_LEFT_HAND && display != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) return;

        if (entity == null || !this.entityIdForMuzzleFlash.contains(entity.getId())) return;

        float randomValue = this.entityIdToRandomValue.get(entity.getId());
        String flashType = modifiedGun.getDisplay().getMuzzleFlashType();
        ResourceLocation flashTexture = getFlashTexture(flashType);

        this.drawMuzzleFlash(weapon, modifiedGun, randomValue, randomValue >= 0.5F, poseStack, buffer, partialTicks, flashTexture);

        // Spawn smoke particles for black powder weapons
        if ("flash_type_2".equals(flashType)) {
            spawnSmokeParticles(entity);
        }
    }

    private void spawnSmokeParticles(LivingEntity entity) {
        ClientLevel world = (ClientLevel) entity.level();
        double posX = entity.getX();
        double posY = entity.getY() + entity.getEyeHeight();
        double posZ = entity.getZ();
        RandomSource random = entity.getRandom();

        for (int i = 0; i < 5; i++) { // Adjust the number of particles as needed
            double offsetX = random.nextGaussian() * 0.1;
            double offsetY = random.nextGaussian() * 0.1;
            double offsetZ = random.nextGaussian() * 0.1;
            world.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, offsetX, offsetY, offsetZ);
        }
    }
    private ResourceLocation getFlashTexture(String flashType) {
        if (flashType == null) {
            return new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash_1.png");
        }
        return switch (flashType) {
            case "flash_type_2" -> new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash_2.png");
            case "flash_type_3" -> new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash_3.png");
            default -> new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash_1.png");
        };
    }

    private void drawMuzzleFlash(ItemStack weapon, Gun modifiedGun, float random, boolean flip, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, ResourceLocation flashTexture) {
        if (!PropertyHelper.hasMuzzleFlash(weapon, modifiedGun))
            return;

        poseStack.pushPose();

        Vec3 weaponOrigin = PropertyHelper.getModelOrigin(weapon, PropertyHelper.GUN_DEFAULT_ORIGIN);
        Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(weapon, modifiedGun).subtract(weaponOrigin);
        poseStack.translate(weaponOrigin.x * 0.0625, weaponOrigin.y * 0.0625, weaponOrigin.z * 0.0625);
        poseStack.translate(flashPosition.x * 0.0625, flashPosition.y * 0.0625, flashPosition.z * 0.0625);
        poseStack.translate(-0.5, -0.5, -0.5);

        ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, weapon);
        if (!barrelStack.isEmpty() && barrelStack.getItem() instanceof IBarrel barrel && !PropertyHelper.isUsingBarrelMuzzleFlash(barrelStack)) {
            Vec3 scale = PropertyHelper.getAttachmentScale(weapon, modifiedGun, IAttachment.Type.BARREL);
            double length = barrel.getProperties().getLength();
            poseStack.translate(0, 0, -length * 0.0625 * scale.z);
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(360F * random));
        poseStack.mulPose(Axis.XP.rotationDegrees(flip ? 180F : 0F));

        Vec3 flashScale = PropertyHelper.getMuzzleFlashScale(weapon, modifiedGun);
        float scaleX = ((float) flashScale.x / 2F) - ((float) flashScale.x / 2F) * (1.0F - partialTicks);
        float scaleY = ((float) flashScale.y / 2F) - ((float) flashScale.y / 2F) * (1.0F - partialTicks);
        poseStack.scale(scaleX, scaleY, 1.0F);

        float scaleModifier = (float) GunModifierHelper.getMuzzleFlashScale(weapon, 1.0);
        poseStack.scale(scaleModifier, scaleModifier, 1.0F);

        poseStack.translate(-0.5, -0.5, 0);

        float minU = weapon.isEnchanted() ? 0.5F : 0.0F;
        float maxU = weapon.isEnchanted() ? 1.0F : 0.5F;
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer builder = buffer.getBuffer(GunRenderType.getMuzzleFlash(flashTexture));
        builder.vertex(matrix, 0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, 1.0F).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, 1.0F).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, 0).uv2(15728880).endVertex();
        builder.vertex(matrix, 0, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, 0).uv2(15728880).endVertex();

        poseStack.popPose();
    }

    private void renderReloadArm(PoseStack poseStack, MultiBufferSource buffer, int light, Gun modifiedGun, ItemStack stack, HumanoidArm hand, float translateX) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.tickCount < ReloadHandler.get().getStartReloadTick() || ReloadHandler.get().getReloadTimer() != 5)
            return;

        poseStack.pushPose();

        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(translateX * side, 0, 0);

        float interval = GunEnchantmentHelper.getRealReloadSpeed(stack);
        float reload = ((mc.player.tickCount - ReloadHandler.get().getStartReloadTick() + mc.getFrameTime()) % interval) / interval;
        float percent = 1.0F - reload;
        if (percent >= 0.5F) {
            percent = 1.0F - percent;
        }
        percent *= 2F;
        percent = percent < 0.5 ? 2 * percent * percent : -1 + (4 - 2 * percent) * percent;

        poseStack.translate(0, 0.35 * (1.0 - percent), 0);

        poseStack.mulPose(Axis.XP.rotationDegrees(30F * percent));

        poseStack.popPose();
    }

    private float getEquipProgress(float partialTicks) {
        if (this.equippedProgressMainHandField == null) {
            this.equippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109302_");
            this.equippedProgressMainHandField.setAccessible(true);
        }
        if (this.prevEquippedProgressMainHandField == null) {
            this.prevEquippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109303_");
            this.prevEquippedProgressMainHandField.setAccessible(true);
        }
        ItemInHandRenderer firstPersonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        try {
            float equippedProgressMainHand = (float) this.equippedProgressMainHandField.get(firstPersonRenderer);
            float prevEquippedProgressMainHand = (float) this.prevEquippedProgressMainHandField.get(firstPersonRenderer);
            return 1.0F - Mth.lerp(partialTicks, prevEquippedProgressMainHand, equippedProgressMainHand);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0.0F;
    }
    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (Config.CLIENT.display.cameraRollEffect.get()) {
            float roll = (float) Mth.lerp(event.getPartialTick(), this.prevImmersiveRoll, this.immersiveRoll);
            roll = (float) Math.sin((roll * Math.PI) / 2.0);
            roll *= Config.CLIENT.display.cameraRollAngle.get().floatValue();
            event.setRoll(-roll);
        }
    }

    public float getThirdPersonMeleeProgress() {
        return this.thirdPersonMeleeProgress;
    }
}