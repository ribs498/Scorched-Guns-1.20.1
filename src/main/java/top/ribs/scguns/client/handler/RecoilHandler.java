package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
import top.ribs.scguns.util.ExoSuitRecoilHelper;

import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class RecoilHandler {
    private static RecoilHandler instance;

    public static RecoilHandler get() {
        if(instance == null) {
            instance = new RecoilHandler();
        }
        return instance;
    }

    private final Random random = new Random();
    private double gunRecoilNormal;
    private double gunRecoilAngle;
    private float gunRecoilRandom;
    private float cameraRecoil;
    private float progressCameraRecoil;
    private boolean enableRecoil = true;
    private RecoilHandler() {}

    private static int recoilRand;

    public void updateConfig() {
        try {
            if(Config.SERVER != null && Config.SERVER.enableCameraRecoil != null) {
                this.enableRecoil = Config.SERVER.enableCameraRecoil.get();
            }
        } catch(IllegalStateException e) {
            // Config not ready yet
        }
    }

    @SubscribeEvent
    public void preShoot(GunFireEvent.Pre event) {
        if(!event.isClient())
            return;
        if(!this.enableRecoil)
            return;

        recoilRand = random.nextInt(2);
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event) {
        if(!event.isClient())
            return;

        if(!this.enableRecoil)
            return;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);

        if(Minecraft.getInstance().player == null) return;

        float baseRecoilAngle = modifiedGun.getGeneral().getRecoilAngle();
        float exoSuitModifiedRecoil = ExoSuitRecoilHelper.getModifiedRecoilAngle(Minecraft.getInstance().player, baseRecoilAngle);

        float recoilModifier = 1.0F - GunModifierHelper.getRecoilModifier(heldItem);
        if (Minecraft.getInstance().player != null) {
            float enchantmentMultiplier = GunEnchantmentHelper.getRecoilModifier(Minecraft.getInstance().player, heldItem);
            recoilModifier *= enchantmentMultiplier;
        }
        recoilModifier *= (float) this.getAdsRecoilReduction(modifiedGun);

        this.cameraRecoil = exoSuitModifiedRecoil * recoilModifier;
        this.progressCameraRecoil = 0F;
        this.gunRecoilRandom = random.nextFloat();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase != TickEvent.Phase.END || this.cameraRecoil <= 0)
            return;

        if(!this.enableRecoil)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        float recoilAmount = this.cameraRecoil * mc.getDeltaFrameTime() * 0.15F;
        float startProgress = this.progressCameraRecoil / this.cameraRecoil;
        float endProgress = (this.progressCameraRecoil + recoilAmount) / this.cameraRecoil;

        float pitch = mc.player.getXRot();
        float yaw = mc.player.getYRot();

        if(startProgress < 0.2F) {
            mc.player.setXRot(pitch - ((endProgress - startProgress) / 0.2F) * this.cameraRecoil);
            if(recoilRand == 1)
                mc.player.setYRot(yaw - ((endProgress - startProgress) / 0.2F) * this.cameraRecoil / 2);
            else
                mc.player.setYRot(yaw + ((endProgress - startProgress) / 0.2F) * this.cameraRecoil / 2);
        } else {
            mc.player.setXRot(pitch + ((endProgress - startProgress) / 0.8F) * this.cameraRecoil);
            if(recoilRand == 1)
                mc.player.setYRot(yaw + ((endProgress - startProgress) / 0.8F) * this.cameraRecoil / 2);
            else
                mc.player.setYRot(yaw - ((endProgress - startProgress) / 0.8F) * this.cameraRecoil / 2);
        }

        this.progressCameraRecoil += recoilAmount;

        if(this.progressCameraRecoil >= this.cameraRecoil) {
            this.cameraRecoil = 0;
            this.progressCameraRecoil = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderOverlay(RenderHandEvent event) {
        if(event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack heldItem = event.getItemStack();
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        assert Minecraft.getInstance().player != null;
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(gunItem, Minecraft.getInstance().getFrameTime());
        cooldown = cooldown >= modifiedGun.getGeneral().getRecoilDurationOffset() ? (cooldown - modifiedGun.getGeneral().getRecoilDurationOffset()) / (1.0F - modifiedGun.getGeneral().getRecoilDurationOffset()) : 0.0F;

        if(cooldown >= 0.8) {
            float amount = ((1.0F - cooldown) / 0.2F);
            this.gunRecoilNormal = 1 - (--amount) * amount * amount * amount;
        } else {
            float amount = (cooldown / 0.8F);
            this.gunRecoilNormal = amount < 0.5 ? 2 * amount * amount : -1 + (4 - 2 * amount) * amount;
        }

        float baseRecoilAngle = modifiedGun.getGeneral().getRecoilAngle();
        if(Minecraft.getInstance().player != null) {
            float exoSuitModifiedRecoil = ExoSuitRecoilHelper.getModifiedRecoilAngle(Minecraft.getInstance().player, baseRecoilAngle);

            float recoilModifier = 1.0F - GunModifierHelper.getRecoilModifier(heldItem);
            float enchantmentMultiplier = GunEnchantmentHelper.getRecoilModifier(Minecraft.getInstance().player, heldItem);
            recoilModifier *= enchantmentMultiplier;

            this.gunRecoilAngle = exoSuitModifiedRecoil * recoilModifier;
        } else {
            this.gunRecoilAngle = baseRecoilAngle;
        }
    }

    public double getAdsRecoilReduction(Gun gun) {
        return 1.0 - gun.getGeneral().getRecoilAdsReduction() * AimingHandler.get().getNormalisedAdsProgress();
    }

    public double getGunRecoilNormal() {
        return this.gunRecoilNormal;
    }

    public double getGunRecoilAngle() {
        return this.gunRecoilAngle;
    }

    public float getGunRecoilRandom() {
        return this.gunRecoilRandom;
    }
}