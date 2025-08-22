package top.ribs.scguns.client.render.gun.animated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtils;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.GunRenderType;
import top.ribs.scguns.client.handler.*;
import top.ribs.scguns.client.render.gun.IOverrideModel;
import top.ribs.scguns.client.render.gun.ModelOverrides;
import top.ribs.scguns.client.screen.AttachmentScreen;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.item.attachment.IBarrel;
import top.ribs.scguns.item.attachment.impl.Scope;
import top.ribs.scguns.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.*;

public class AnimatedGunRenderer extends GeoItemRenderer<AnimatedGunItem> implements GeoRenderer<AnimatedGunItem> {
    private static final ResourceLocation custom_path = null;
    private static AnimatedGunRenderer instance;
    private final AttachmentRenderer attachmentRenderer = new AttachmentRenderer(this);
    private MultiBufferSource bufferSource;
    private ItemDisplayContext currentDisplayContext;
    private ItemStack currentRenderStack;
    private float sprintIntensity;
    private float immersiveRoll;
    private float fallSway;
    private float prevFallSway;
    private static final long PARTICLE_COOLDOWN_MS = 100;
    private long lastParticleSpawnTime = 0;
    private final Map<Integer, Integer> entityShotCount = new HashMap<>();
    private GeoBone currentLaserOriginBone = null;

    public GeoBone getCurrentLaserOriginBone() {
        return currentLaserOriginBone;
    }

    public AnimatedGunRenderer(ResourceLocation path) {
        super(new AnimatedGunModel(path));
        instance = this;
    }

    public static AnimatedGunRenderer get() {
        if (instance == null) {
            instance = new AnimatedGunRenderer(custom_path);
        }
        return instance;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.updateImmersiveCamera();
        }
    }

    private void updateImmersiveCamera() {
        this.prevFallSway = this.fallSway;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack heldItem = mc.player.getMainHandItem();
            float targetAngle = !(heldItem.getItem() instanceof GunItem) && Config.CLIENT.display.restrictCameraRollToWeapons.get() ? 0.0F : mc.player.input.leftImpulse;
            float speed = mc.player.input.leftImpulse != 0.0F ? 0.1F : 0.15F;
            this.immersiveRoll = Mth.lerp(speed, this.immersiveRoll, targetAngle);
            float deltaY = (float) Mth.clamp(mc.player.yo - mc.player.getY(), -1.0, 1.0);
            deltaY = (float) ((double) deltaY * (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
            deltaY = (float) ((double) deltaY * (1.0 - (double) (Mth.abs(mc.player.getXRot()) / 90.0F)));
            this.fallSway = Mth.approach(this.fallSway, deltaY * 60.0F * Config.CLIENT.display.swaySensitivity.get().floatValue(), 10.0F);
            float intensity = mc.player.isSprinting() ? 0.75F : 1.0F;
            this.sprintIntensity = Mth.approach(this.sprintIntensity, intensity, 0.1F);
        }
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event) {
        if (event.isClient()) {
            ItemStack heldItem = event.getStack();
            GunItem gunItem = (GunItem) heldItem.getItem();
            gunItem.getModifiedGun(heldItem);
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {


        this.currentDisplayContext = transformType;
        this.currentRenderStack = stack;
        this.bufferSource = bufferSource;



        if (stack.getItem() instanceof AnimatedGunItem) {
            if (transformType == ItemDisplayContext.NONE &&
                    Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
                this.currentDisplayContext = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
            }
        }
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? player.getUsedItemHand() == InteractionHand.MAIN_HAND : player.getUsedItemHand() == InteractionHand.OFF_HAND;
        ItemStack overrideModel = ItemStack.EMPTY;
        if (stack.getTag() != null && stack.getTag().contains("Model", 10)) {
            overrideModel = ItemStack.of(stack.getTag().getCompound("Model"));
        }

        LocalPlayer localPlayer = Objects.requireNonNull(Minecraft.getInstance().player);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(overrideModel.isEmpty() ? stack : overrideModel, player.level(), player, 0);
        float scaleX = model.getTransforms().firstPersonRightHand.scale.x();
        float scaleY = model.getTransforms().firstPersonRightHand.scale.y();
        float scaleZ = model.getTransforms().firstPersonRightHand.scale.z();
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        float translateY = model.getTransforms().firstPersonRightHand.translation.y();
        float translateZ = model.getTransforms().firstPersonRightHand.translation.z();
//        if (stack.getItem() instanceof AnimatedGunItem && transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
//            poseStack.translate(0.0, -0.5, 0.0);
//        }

        if (stack.getItem() instanceof AnimatedGunItem && transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            if (AimingHandler.get().getNormalisedAdsProgress() > 0.0 && modifiedGun.canAimDownSight()) {
                double xOffset = translateX;
                double yOffset = translateY;
                double zOffset = translateZ;
                xOffset -= 0.5 * (double) scaleX;
                yOffset -= 0.5 * (double) scaleY;
                zOffset -= 0.5 * (double) scaleZ;
                Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
                xOffset += gunOrigin.x * 0.0625 * (double) scaleX;
                yOffset += gunOrigin.y * 0.0625 * (double) scaleY;
                zOffset += gunOrigin.z * 0.0625 * (double) scaleZ;
                Scope scope = Gun.getScope(stack);
                Vec3 ironSightCamera;
                if (modifiedGun.canAttachType(IAttachment.Type.SCOPE) && scope != null) {
                    ironSightCamera = PropertyHelper.getAttachmentPosition(stack, modifiedGun, IAttachment.Type.SCOPE).subtract(gunOrigin);
                    xOffset += ironSightCamera.x * 0.0625 * (double) scaleX;
                    yOffset += ironSightCamera.y * 0.0625 * (double) scaleY;
                    zOffset += ironSightCamera.z * 0.0625 * (double) scaleZ;
                    ItemStack scopeStack = Gun.getScopeStack(stack);
                    Vec3 scopeOrigin = PropertyHelper.getModelOrigin(scopeStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                    Vec3 scopeCamera = PropertyHelper.getScopeCamera(scopeStack).subtract(scopeOrigin);
                    Vec3 scopeScale = PropertyHelper.getAttachmentScale(stack, modifiedGun, IAttachment.Type.SCOPE);
                    xOffset += scopeCamera.x * 0.0625 * (double) scaleX * scopeScale.x;
                    yOffset += (scopeCamera.y * 0.0625 * (double) scaleY + 0.54) * scopeScale.y;
                    zOffset += (scopeCamera.z * 0.0625 * (double) scaleZ - 0.16) * scopeScale.z;
                } else {
                    ironSightCamera = PropertyHelper.getIronSightCamera(stack, modifiedGun, gunOrigin).subtract(gunOrigin);
                    xOffset += ironSightCamera.x * 0.0625 * (double) scaleX;
                    yOffset += ironSightCamera.y * 0.0625 * (double) scaleY + 0.6059;
                    zOffset += ironSightCamera.z * 0.0625 * (double) scaleZ - 0.16;
                    if (PropertyHelper.isLegacyIronSight(stack)) {
                        zOffset += 0.72;
                    }
                }

                float side;
                if (Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT) {
                    player.getOffhandItem().getItem();
                }

                if (Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT) {
                    player.getOffhandItem().getItem();
                }

                side = 1.0F;
                double time = AimingHandler.get().getNormalisedAdsProgress();
                double transition = PropertyHelper.getSightAnimations(stack, modifiedGun).getSightCurve().apply(time);
                poseStack.translate(-0.56 * (double) side * transition, 0.52 * transition, 0.72 * transition);
                poseStack.translate(-xOffset * (double) side * transition, -yOffset * transition, -zOffset * transition);
            }

            this.applyBobbingTransforms(poseStack, Minecraft.getInstance().getPartialTick());
            int offset = right ? 1 : -1;
            this.applyRecoilTransforms(poseStack, stack, modifiedGun);
            this.applyAimingTransforms(poseStack, stack, modifiedGun, translateX, translateY, translateZ, offset);
            this.applySwayTransforms(poseStack, modifiedGun, stack, localPlayer, translateX, translateY, translateZ, Minecraft.getInstance().getPartialTick());
            if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(stack)) {
                this.renderMuzzleFlash(Minecraft.getInstance().player, poseStack, bufferSource, stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, Minecraft.getInstance().getPartialTick());
            }
        }

        int blockLight = player.isOnFire() ? 15 : player.level().getBrightness(LightLayer.BLOCK, BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getPartialTick())));
        if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(stack)) {
            blockLight += GunRenderingHandler.entityIdForMuzzleFlash.contains(player.getId()) ? 3 : 0;
        }

        blockLight = Math.min(blockLight, 15);
        if (transformType == ItemDisplayContext.GUI) {
            packedLight = LightTexture.pack(12, 12);
        } else {
            packedLight = LightTexture.pack(blockLight, player.level().getBrightness(LightLayer.SKY, BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getPartialTick()))));
        }

        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private boolean shouldRenderArms() {
        if (!Config.CLIENT.display.renderArms.get()) return false;
        if (this.currentRenderStack == null) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (mc.screen instanceof AttachmentScreen) return false;
        if (this.currentDisplayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND &&
                this.currentDisplayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return false;
        }
        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) return false;
        return !mc.player.isSpectator() && mc.player.isAlive();
    }
    private boolean isMagazineBone(GeoBone bone) {
        String name = bone.getName();
        return name.contains("_mag") || name.contains("magazine");
    }
    @Override
    public void renderRecursively(PoseStack poseStack, AnimatedGunItem animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, float red, float green,
                                  float blue, float alpha) {

//        if (this.currentDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ||
//                this.currentDisplayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
//            return;
//        }

        Minecraft client = Minecraft.getInstance();
        boolean shouldRenderCustomArms = false;
        if (bone.getName().equals("laser_origin")) {
            this.currentLaserOriginBone = bone;
        }

        poseStack.pushPose();

        switch (bone.getName()) {
            case "cogloader_magazine" -> {
                float pivotX = bone.getPivotX() * 0.0625f;
                float pivotY = bone.getPivotY() * 0.0625f;
                float pivotZ = bone.getPivotZ() * 0.0625f;
                poseStack.translate(pivotX, pivotY, pivotZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getRotationHandler().getCurrentMagazineRotation()));
                poseStack.translate(-pivotX, -pivotY, -pivotZ);
            }
            case "cylinder_magazine" -> {
                float pivotX = bone.getPivotX() * 0.0625f;
                float pivotY = bone.getPivotY() * 0.0625f;
                float pivotZ = bone.getPivotZ() * 0.0625f;
                poseStack.translate(pivotX, pivotY, pivotZ);
                poseStack.mulPose(Axis.ZP.rotationDegrees(animatable.getRotationHandler().getCurrentCylinderRotation()));
                poseStack.translate(-pivotX, -pivotY, -pivotZ);
            }
            case "sliding_magazine" -> {
                CompoundTag tag = this.currentRenderStack.getOrCreateTag();
                if (tag.contains("MagazineOverride") && tag.getBoolean("MagazineOverride")) {
                    float storedPosition = tag.getFloat("MagazinePosition");
                    poseStack.translate(storedPosition * 0.25f, 0, 0);
                } else {
                    float maxAmmo = Gun.getMaxAmmo(this.currentRenderStack);
                    float currentAmmo = Gun.getAmmoCount(this.currentRenderStack);
                    float slidePosition = Math.min((maxAmmo - currentAmmo) / maxAmmo, 1.0f);
                    poseStack.translate(slidePosition * 0.25f, 0, 0);
                }
            }
        }
        if (isArmBone(bone)) {
            if (this.currentDisplayContext != ItemDisplayContext.GUI) {
                boolean shouldRender = shouldRenderArms();
                bone.setHidden(true);
                bone.setChildrenHidden(true);
                if (shouldRender) {
                    shouldRenderCustomArms = true;
                }
            } else {
                bone.setHidden(true);
                bone.setChildrenHidden(true);
            }
        } else if (bone.getName().matches("seal")) {
            bone.setHidden(!Config.CLIENT.display.puritySeals.get());
            bone.setChildrenHidden(!Config.CLIENT.display.puritySeals.get());
        } else {
            handleBoneVisibility(bone);
        }

        if (shouldRenderCustomArms && client.player != null) {
            renderPlayerArms(client, poseStack, bone, animatable, packedLight, packedOverlay);
        }

        if (bone.getName().matches("attachment_bone")) {
            renderAttachments(bone, this.currentRenderStack, poseStack, renderType, buffer,
                    packedLight, client.getPartialTick(), packedOverlay);
        }
        if (bone.getName().matches("glow") || bone.getName().matches("glow_2") || bone.getName().matches("glow_3")) {
            packedLight = 15728880;
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);

        poseStack.popPose();
    }

    private boolean isArmBone(GeoBone bone) {
        return bone.getName().equals("left_arm") ||
                bone.getName().equals("right_arm") ||
                bone.getName().equals("fake_left_arm") ||
                bone.getName().equals("fake_right_arm");
    }

    private void handleBoneVisibility(GeoBone bone) {
        if (isArmBone(bone)) {
            bone.setHidden(true);
            return;
        }

        if (bone.getName().equals("left_arm") || bone.getName().equals("right_arm") ||
                bone.getName().equals("fake_left_arm") || bone.getName().equals("fake_right_arm")) {
            bone.setHidden(true);
            bone.setChildrenHidden(true);
            return;
        }
        switch (bone.getName()) {

            case "sights":
                bone.setHidden(Gun.getScope(this.currentItemStack) != null);
                break;

            case "no_sights":
                bone.setHidden(Gun.getScope(this.currentItemStack) == null);
                break;
            case "wooden_stock":
            case "light_stock":
            case "weighted_stock":
                handleStockVisibility(bone);
                break;
            case "standard_barrel":
            case "extended_barrel":
            case "silencer":
            case "advanced_silencer":
            case "muzzle_brake":
            case "standard_barrel1":
            case "extended_barrel1":
            case "silencer1":
            case "advanced_silencer1":
            case "muzzle_brake1":
                handleBarrelVisibility(bone);
                break;
            case "light_grip":
            case "vertical_grip":
            case "iron_bayonet":
            case "anthralite_bayonet":
            case "diamond_bayonet":
            case "netherite_bayonet":
                handleUnderBarrelVisibility(bone);
                break;
            case "standard_mag":
            case "default_mag":
            case "standard_mag_2":
            case "default_mag_2":
            case "extended_mag":
            case "extended_mag_2":
            case "speed_mag":
            case "speed_mag_2":
                handleMagazineVisibility(bone);
                break;
            case "standard_stock":
            case "standard_grip":
                ItemStack stockAttachment = Gun.getAttachment(IAttachment.Type.STOCK, this.currentItemStack);
                bone.setHidden(!stockAttachment.isEmpty());
                break;
        }

    }

    private void handleStockVisibility(GeoBone bone) {
        ItemStack stockAttachment = Gun.getAttachment(IAttachment.Type.STOCK, this.currentItemStack);
        switch (bone.getName()) {
            case "wooden_stock":
                bone.setHidden(stockAttachment.getItem() != ModItems.WOODEN_STOCK.get());
                break;
            case "light_stock":
                bone.setHidden(stockAttachment.getItem() != ModItems.LIGHT_STOCK.get());
                break;
            case "weighted_stock":
                bone.setHidden(stockAttachment.getItem() != ModItems.WEIGHTED_STOCK.get());
                break;
        }
    }

    private void handleBarrelVisibility(GeoBone bone) {
        ItemStack barrelAttachment = Gun.getAttachment(IAttachment.Type.BARREL, this.currentItemStack);
        switch (bone.getName()) {
            case "standard_barrel":
            case "standard_barrel1":
                bone.setHidden(barrelAttachment.getItem() == ModItems.EXTENDED_BARREL.get());
                break;
            case "extended_barrel":
            case "extended_barrel1":
                bone.setHidden(barrelAttachment.getItem() != ModItems.EXTENDED_BARREL.get());
                break;
            case "silencer":
            case "silencer1":
                bone.setHidden(barrelAttachment.getItem() != ModItems.SILENCER.get());
                break;
            case "advanced_silencer":
            case "advanced_silencer1":
                bone.setHidden(barrelAttachment.getItem() != ModItems.ADVANCED_SILENCER.get());
                break;
            case "muzzle_brake":
            case "muzzle_brake1":
                bone.setHidden(barrelAttachment.getItem() != ModItems.MUZZLE_BRAKE.get());
                break;
        }
    }

    private void handleUnderBarrelVisibility(GeoBone bone) {
        ItemStack underBarrelAttachment = Gun.getAttachment(IAttachment.Type.UNDER_BARREL, this.currentItemStack);
        Item attachmentItem = underBarrelAttachment.getItem();

        switch (bone.getName()) {
            case "light_grip":
                bone.setHidden(attachmentItem != ModItems.LIGHT_GRIP.get());
                break;
            case "vertical_grip":
                bone.setHidden(attachmentItem != ModItems.VERTICAL_GRIP.get());
                break;
            case "iron_bayonet":
                bone.setHidden(attachmentItem != ModItems.IRON_BAYONET.get());
                break;
            case "anthralite_bayonet":
                bone.setHidden(attachmentItem != ModItems.ANTHRALITE_BAYONET.get());
                break;
            case "diamond_bayonet":
                bone.setHidden(attachmentItem != ModItems.DIAMOND_BAYONET.get());
                break;
            case "netherite_bayonet":
                bone.setHidden(attachmentItem != ModItems.NETHERITE_BAYONET.get());
                break;
        }
    }
    private void handleMagazineVisibility(GeoBone bone) {
        ItemStack magazineAttachment = Gun.getAttachment(IAttachment.Type.MAGAZINE, this.currentItemStack);

        switch (bone.getName()) {
            case "standard_mag":
            case "default_mag":
                bone.setHidden(!magazineAttachment.isEmpty());
                break;
            case "standard_mag_2":
            case "default_mag_2":
                // Show the correct fake standard/default mag based on attachment
                bone.setHidden(!magazineAttachment.isEmpty());
                break;
            case "extended_mag":
                bone.setHidden(magazineAttachment.getItem() != ModItems.EXTENDED_MAG.get());
                break;
            case "extended_mag_2":
                // Show fake extended mag only when extended mag is equipped
                bone.setHidden(magazineAttachment.getItem() != ModItems.EXTENDED_MAG.get());
                break;
            case "speed_mag":
                bone.setHidden(magazineAttachment.getItem() != ModItems.SPEED_MAG.get());
                break;
            case "speed_mag_2":
                // Show fake speed mag only when speed mag is equipped
                bone.setHidden(magazineAttachment.getItem() != ModItems.SPEED_MAG.get());
                break;
        }
    }

    private void renderPlayerArms(Minecraft client, PoseStack poseStack, GeoBone bone,
                                  AnimatedGunItem animatable, int packedLight, int packedOverlay) {
        if (client.player == null) return;

        PlayerRenderer playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher()
                .getRenderer(client.player);
        PlayerModel<AbstractClientPlayer> playerEntityModel = playerEntityRenderer.getModel();

        setupArmTransforms(poseStack, bone);

        ResourceLocation playerSkin = client.player.getSkinTextureLocation();
        VertexConsumer arm = this.bufferSource.getBuffer(RenderType.entitySolid(playerSkin));
        VertexConsumer sleeve = this.bufferSource.getBuffer(RenderType.entityTranslucent(playerSkin));

        if (isRightArm(bone)) {
            renderRightArm(poseStack, playerEntityModel, arm, sleeve, bone, packedLight, packedOverlay);
        } else {
            renderLeftArm(poseStack, playerEntityModel, arm, sleeve, bone, packedLight, packedOverlay,
                    client.player, animatable);
        }
    }

    private void setupArmTransforms(PoseStack poseStack, GeoBone bone) {
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);
        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);
    }

    private boolean isRightArm(GeoBone bone) {
        return !bone.getName().equals("left_arm") && !bone.getName().equals("fake_left_arm") &&
                (bone.getName().equals("right_arm") || bone.getName().equals("fake_right_arm"));
    }

    private void renderRightArm(PoseStack poseStack, PlayerModel<AbstractClientPlayer> playerEntityModel,
                                VertexConsumer arm, VertexConsumer sleeve, GeoBone bone,
                                int packedLight, int packedOverlay) {
        poseStack.scale(0.66F, 0.78F, 0.66F);
        poseStack.translate(0.25, -0.1, 0.1625);

        playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightArm.setRotation(0.0F, 0.0F, 0.0F);
        playerEntityModel.rightArm.render(poseStack, arm, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.rightSleeve.setRotation(0.0F, 0.0F, 0.0F);
        playerEntityModel.rightSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderLeftArm(PoseStack poseStack, PlayerModel<AbstractClientPlayer> playerEntityModel,
                               VertexConsumer arm, VertexConsumer sleeve, GeoBone bone,
                               int packedLight, int packedOverlay, Player player, AnimatedGunItem animatable) {
        Gun modifiedGun = ((GunItem) this.currentItemStack.getItem()).getModifiedGun(this.currentItemStack);
        modifiedGun.determineGripType(this.currentItemStack);

        long id = GeoItem.getId(player.getMainHandItem());
        AnimationController<GeoAnimatable> animationController =
                animatable.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

        checkAndHandleAnimations(animationController);

        poseStack.scale(0.66F, 0.79F, 0.66F);
        poseStack.translate(-0.25, -0.1, 0.1625);

        playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftArm.setRotation(0.0F, 0.0F, 0.0F);
        playerEntityModel.leftArm.render(poseStack, arm, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        playerEntityModel.leftSleeve.setRotation(0.0F, 0.0F, 0.0F);
        playerEntityModel.leftSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void checkAndHandleAnimations(AnimationController<GeoAnimatable> animationController) {
        if (animationController != null && animationController.getCurrentAnimation() != null) {
            String animName = animationController.getCurrentAnimation().animation().name();
            if (!animName.matches("draw") && !animName.matches("reload") &&
                    !animName.matches("reload_start") && !animName.matches("reload_loop")) {
                animationController.getCurrentAnimation();
            }
        }
    }
    private void applyRecoilTransforms(PoseStack poseStack, ItemStack item, Gun gun) {
        double recoilNormal = GunRecoilHandler.get().getGunRecoilNormal();
        if (Gun.hasAttachmentEquipped(item, gun, IAttachment.Type.SCOPE)) {
            recoilNormal -= recoilNormal * 0.5 * AimingHandler.get().getNormalisedAdsProgress();
        }

        Minecraft mc = Minecraft.getInstance();
        float kickReduction, recoilReduction;

        if (mc.player != null) {
            kickReduction = 1.0F - GunModifierHelper.getKickReduction(mc.player, item);
            recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(mc.player, item);
        } else {
            kickReduction = 1.0F - GunModifierHelper.getKickReduction(item);
            recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(item);
        }

        double kick = (double) gun.getGeneral().getRecoilKick() * 0.0625 * recoilNormal * GunRecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilLift = (float) ((double) gun.getGeneral().getRecoilAngle() * recoilNormal) * (float) GunRecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilSwayAmount = (float) (2.0 + (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
        float recoilSway = (float) ((double) (GunRecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2.0F) * recoilNormal);

        poseStack.translate(0.0, 0.0, kick * (double) kickReduction);
        poseStack.translate(0.0, 0.0, 0.15);
        poseStack.mulPose(Axis.YP.rotationDegrees(recoilSway * recoilReduction / 5.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(recoilSway * recoilReduction / 5.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(recoilLift * recoilReduction / 5.0F));
        poseStack.translate(0.0, 0.0, -0.15);
    }

    private void applyBobbingTransforms(PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.bobView().get()) {
            Entity var5 = mc.getCameraEntity();
            if (var5 instanceof Player player) {
                float deltaDistanceWalked = player.walkDist - player.walkDistO;
                float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
                float bobbing = Mth.lerp(partialTicks, player.oBob, player.bob);
                poseStack.mulPose(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * 3.1415927F - 0.2F) * bobbing) * 5.0F)));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.sin(distanceWalked * 3.1415927F) * bobbing * 3.0F)));
                poseStack.translate(-(Mth.sin(distanceWalked * 3.1415927F) * bobbing * 0.5F), Math.abs(Mth.cos(distanceWalked * 3.1415927F) * bobbing), 0.0);
                bobbing = (float) ((double) bobbing * (player.isSprinting() ? 8.0 : 4.0));
                bobbing = (float) ((double) bobbing * Config.CLIENT.display.bobbingIntensity.get());
                double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * (double) this.sprintIntensity;
                if (!AimingHandler.get().isAiming()) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(distanceWalked * 3.1415927F - 0.2F) * bobbing) * -2.0F * (float) invertZoomProgress));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(distanceWalked * 3.1415927F) * bobbing * 3.0F * (float) invertZoomProgress));
                }
            }
        }

    }

    private void applyAimingTransforms(PoseStack poseStack, ItemStack stack, Gun modifiedGun, float x, float y, float z, int offset) {
        poseStack.translate(x * (float) offset, y, z);
        poseStack.translate(0.0, -0.25, 0.25);
        float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180.0));
        aiming = PropertyHelper.getSightAnimations(stack, modifiedGun).getAimTransformCurve().apply(aiming / 2.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(aiming * 10.0F * (float) offset));
        poseStack.mulPose(Axis.XP.rotationDegrees(aiming * 8.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(aiming * 8.0F * (float) offset));
        poseStack.translate(0.0, 0.25, -0.25);
        poseStack.translate(-x * (float) offset, -y, -z);
    }

    private void applySwayTransforms(PoseStack poseStack, Gun modifiedGun, ItemStack stack, LocalPlayer player, float x, float y, float z, float partialTicks) {
        if (!Config.CLIENT.display.weaponSway.get() || player == null) {
            return;
        }

        poseStack.pushPose();
        {
            poseStack.translate(x, y, z);
            double zOffset = modifiedGun.determineGripType(stack).heldAnimation().getFallSwayZOffset();
            poseStack.translate(0, -0.25, zOffset);
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevFallSway, this.fallSway)));
            poseStack.translate(0, 0.25, -zOffset);
            float bobPitch = Mth.rotLerp(partialTicks, player.xBobO, player.xBob);
            float headPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
            float swayPitch = headPitch - bobPitch;
            float adsProgress = (float) AimingHandler.get().getNormalisedAdsProgress();
            swayPitch *= (float) (1.0 - 0.5 * adsProgress);
            float pitchSensitivity = Config.CLIENT.display.swaySensitivity.get().floatValue();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getPitchRotation()
                    .rotationDegrees(swayPitch * pitchSensitivity));
            float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
            float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
            float swayYaw = headYaw - bobYaw;
            swayYaw *= (float) (1.0 - 0.5 * adsProgress);
            float yawSensitivity = Config.CLIENT.display.swaySensitivity.get().floatValue();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getYawRotation()
                    .rotationDegrees(swayYaw * yawSensitivity));

            poseStack.translate(-x, -y, -z);
        }
        poseStack.popPose();
    }

    private void renderAttachments(GeoBone bone, ItemStack stack, PoseStack poseStack, RenderType renderType, VertexConsumer renderTypeBuffer, int light, float partialTicks, int packedOverlay) {
        if (!(stack.getItem() instanceof GunItem)) {
            return;
        }

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
                ModItems.ANTHRALITE_BAYONET.get(),
                ModItems.DIAMOND_BAYONET.get(),
                ModItems.NETHERITE_BAYONET.get(),
                ModItems.EXTENDED_BARREL.get(),
                ModItems.WOODEN_STOCK.get(),
                ModItems.LIGHT_STOCK.get(),
                ModItems.WEIGHTED_STOCK.get(),
                ModItems.EXTENDED_MAG.get(),
                ModItems.SPEED_MAG.get()

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
                    Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
                    Vec3 translation = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
                    Vec3 scale = PropertyHelper.getAttachmentScale(stack, modifiedGun, type);
                    Vec3 center = origin.subtract(8.0, 8.0, 8.0).scale(0.0625);
                    poseStack.translate(-origin.x * 0.0625, -origin.y * 0.0625, -origin.z * 0.0625);
                    poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625, gunOrigin.z * 0.0625);
                    poseStack.translate(translation.x * 0.0625, translation.y * 0.0625, translation.z * 0.0625);
                    poseStack.translate(center.x, center.y, center.z);
                    poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
                    poseStack.translate(-center.x, -center.y, -center.z);
                    IOverrideModel overrideModel = ModelOverrides.getModel(attachmentStack);
                    if (overrideModel != null) {
                        overrideModel.render(partialTicks, ItemDisplayContext.NONE, attachmentStack, stack,
                                Minecraft.getInstance().player, poseStack, this.bufferSource, light, OverlayTexture.NO_OVERLAY);
                    } else {
                        this.attachmentRenderer.updateAttachment(attachmentStack);
                        this.attachmentRenderer.renderForBone(poseStack, this.animatable, bone, renderType,
                                this.bufferSource, renderTypeBuffer, partialTicks, light, packedOverlay);
                    }

                    poseStack.popPose();
                }
            }
        }
    }

    private void renderMuzzleFlash(@Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, ItemStack weapon, ItemDisplayContext display, float partialTicks) {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        Gun.Display.Flash flash = modifiedGun.getDisplay().getFlash();
        if (flash == null) return;
        if (display != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND &&
                display != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND &&
                display != ItemDisplayContext.FIRST_PERSON_LEFT_HAND &&
                display != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            return;
        }
        if (!(entity instanceof Player)) return;
        boolean isBeamActive = BeamHandler.activeBeams.containsKey(entity.getUUID());
        if (!isBeamActive && !GunRenderingHandler.entityIdForMuzzleFlash.contains(entity.getId())) {
            return;
        }
        float randomValue = GunRenderingHandler.entityIdToRandomValue.getOrDefault(entity.getId(), 0f);
        boolean mirror = this.entityShotCount.getOrDefault(entity.getId(), 0) % 2 == 1 && flash.hasAlternateMuzzleFlash();
        ResourceLocation flashTexture = new ResourceLocation(Reference.MOD_ID, "textures/effect/" + flash.getTextureLocation() + ".png");
        drawMuzzleFlash(weapon, modifiedGun, randomValue, mirror, poseStack, buffer, partialTicks, flashTexture, entity);
        if (flash.shouldSpawnParticles() && flash.getParticleType() != null) {
            spawnParticles(flash, entity);
        }
    }

    private void spawnParticles(Gun.Display.Flash flash, LivingEntity entity) {
        if (entity == null || !entity.level().isClientSide())
            return;

        ClientLevel world = (ClientLevel) entity.level();
        RandomSource random = entity.getRandom();
        double posX = entity.getX();
        double posY = entity.getY() + entity.getEyeHeight();
        double posZ = entity.getZ();
        SimpleParticleType particleType = flash.getParticleType();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastParticleSpawnTime >= PARTICLE_COOLDOWN_MS) {
            lastParticleSpawnTime = currentTime;

            double radius = flash.getParticleRingRadius();
            if (radius > 0) {
                for (int i = 0; i < flash.getParticleCount(); i++) {
                    double angle = random.nextDouble() * 2 * Math.PI;

                    double randomizedRadius = radius * (0.8 + random.nextDouble() * 0.4);

                    double offsetX = Math.cos(angle) * randomizedRadius;
                    double offsetZ = Math.sin(angle) * randomizedRadius;

                    double spread = flash.getParticleSpread();
                    double offsetY = (random.nextDouble() - 0.5) * spread;

                    offsetX += (random.nextDouble() - 0.5) * spread * 0.5;
                    offsetZ += (random.nextDouble() - 0.5) * spread * 0.5;

                    double motionStrength = 0.03 + random.nextDouble() * 0.02;
                    double motionX = offsetX * motionStrength;
                    double motionY = offsetY * motionStrength + 0.01;
                    double motionZ = offsetZ * motionStrength;

                    world.addParticle(particleType,
                            posX + offsetX,
                            posY + offsetY,
                            posZ + offsetZ,
                            motionX, motionY, motionZ);
                }
            } else {
                for (int i = 0; i < flash.getParticleCount(); i++) {
                    double spread = flash.getParticleSpread();
                    double offsetX = (random.nextDouble() - 0.5) * spread;
                    double offsetY = (random.nextDouble() - 0.5) * spread;
                    double offsetZ = (random.nextDouble() - 0.5) * spread;
                    world.addParticle(particleType,
                            posX + offsetX,
                            posY + offsetY,
                            posZ + offsetZ,
                            offsetX * 0.1,
                            offsetY * 0.1,
                            offsetZ * 0.1);
                }
            }
        }
    }

    private void drawMuzzleFlash(ItemStack weapon, Gun modifiedGun, float random, boolean mirror, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, ResourceLocation flashTexture, LivingEntity entity) {
        if (!PropertyHelper.hasMuzzleFlash(weapon, modifiedGun)) {
            return;
        }

        Gun.Display.Flash flash = modifiedGun.getDisplay().getFlash();
        if (flash == null) {
            return;
        }

        poseStack.pushPose();

        Vec3 weaponOrigin = PropertyHelper.getModelOrigin(weapon, PropertyHelper.GUN_DEFAULT_ORIGIN);
        Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(weapon, modifiedGun).subtract(weaponOrigin);

        poseStack.translate(weaponOrigin.x * 0.0625, weaponOrigin.y * 0.0625, weaponOrigin.z * 0.0625);
        poseStack.translate(flashPosition.x * 0.0625 + 0.575, flashPosition.y * 0.0625 + 1.08, flashPosition.z * 0.0625);

        if (AimingHandler.get().isAiming()) {
            poseStack.translate(-0.075, 0.0, 0.0);
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, weapon);
        if (!barrelStack.isEmpty() && barrelStack.getItem() instanceof IBarrel barrel) {
            if (!PropertyHelper.isUsingBarrelMuzzleFlash(barrelStack)) {
                Vec3 scale = PropertyHelper.getAttachmentScale(weapon, modifiedGun, IAttachment.Type.BARREL);
                double length = barrel.getProperties().getLength();
                poseStack.translate(0.0, 0.0, -length * 0.0625 * scale.z);
            }
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(360F * random));
        if (mirror) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180F));
        }
        Vec3 flashScale = PropertyHelper.getMuzzleFlashScale(weapon, modifiedGun);
        float scaleX = ((float) flashScale.x / 2F) - ((float) flashScale.x / 2F) * (1.0F - partialTicks);
        float scaleY = ((float) flashScale.y / 2F) - ((float) flashScale.y / 2F) * (1.0F - partialTicks);
        poseStack.scale(scaleX * 2.0F, scaleY * 2.0F, 1.0F);

        float scaleModifier = (float) GunModifierHelper.getMuzzleFlashScale(weapon, 1.0);
        poseStack.scale(scaleModifier, scaleModifier, 1.0F);

        poseStack.translate(-0.5, -0.5, 0);
        renderFlashQuad(poseStack, buffer, weapon, flashTexture);

        poseStack.popPose();
    }

    private void renderFlashQuad(PoseStack poseStack, MultiBufferSource buffer, ItemStack weapon, ResourceLocation flashTexture) {
        float minU = weapon.isEnchanted() ? 0.5F : 0.0F;
        float maxU = weapon.isEnchanted() ? 1.0F : 0.5F;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer builder = buffer.getBuffer(GunRenderType.getMuzzleFlash(flashTexture));

        builder.vertex(matrix, 0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, 1.0F).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, 1.0F).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, 0).uv2(15728880).endVertex();
        builder.vertex(matrix, 0, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, 0).uv2(15728880).endVertex();
    }
}