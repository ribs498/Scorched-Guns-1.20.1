package top.ribs.scguns.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.AimingHandler;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;

/**
 * Author: MrCrayfish
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
    @ModifyVariable(method = "turnPlayer()V", at = @At(value = "STORE", opcode = Opcodes.DSTORE), ordinal = 2)
    private double sensitivity(double original)
    {
        float additionalAdsSensitivity = 1.0F;
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && !mc.player.getMainHandItem().isEmpty() && mc.options.getCameraType() == CameraType.FIRST_PERSON)
        {
            ItemStack heldItem = mc.player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                if(AimingHandler.get().isAiming() && !ModSyncedDataKeys.RELOADING.getValue(mc.player))
                {
                    Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                    if(modifiedGun.getModules().getZoom() != null)
                    {
                        float modifier = Gun.getFovModifier(heldItem, modifiedGun);
                        additionalAdsSensitivity = Mth.clamp(1.0F - (1.0F / modifier) / 10F, 0.0F, 1.0F);
                    }
                }
            }
        }
        double adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
        return original * (1.0 - (1.0 - adsSensitivity) * AimingHandler.get().getNormalisedAdsProgress()) * additionalAdsSensitivity;
    }
}
