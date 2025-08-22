package top.ribs.scguns.event;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.animated.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ArmorRenderHandler {

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getRenderer().getModel() instanceof PlayerModel<?> playerModel) {
            hideSecondLayerForCustomArmor(event.getEntity(), playerModel);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (event.getRenderer().getModel() instanceof PlayerModel<?> playerModel) {
            restoreSecondLayer(playerModel);
        }
    }

    private static void hideSecondLayerForCustomArmor(net.minecraft.world.entity.LivingEntity entity, PlayerModel<?> playerModel) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorStack = entity.getItemBySlot(slot);
                if (armorStack.getItem() instanceof AdrienArmorItem) {
                    hideSecondLayerForSlot(playerModel, slot);
                }
                if (armorStack.getItem() instanceof AnthraliteArmorItem) {
                    hideSecondLayerForSlot(playerModel, slot);
                }
                if (armorStack.getItem() instanceof AnthraliteGasMaskArmorItem) {
                    hideSecondLayerForSlot(playerModel, slot);
                }
                if (armorStack.getItem() instanceof NetheriteGasMaskArmorItem) {
                    hideSecondLayerForSlot(playerModel, slot);
                }
                if (armorStack.getItem() instanceof ExoSuitItem) {
                    hideSecondLayerForSlot(playerModel, slot);
                }
                if (armorStack.getItem() instanceof DiamondSteelArmorItem) {
                    hideSecondLayerForSlot(playerModel, slot);
                }
            }
        }
    }

    private static void hideSecondLayerForSlot(PlayerModel<?> playerModel, EquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                playerModel.hat.visible = false;
                break;
            case CHEST:
                playerModel.jacket.visible = false;
                playerModel.leftSleeve.visible = false;
                playerModel.rightSleeve.visible = false;
                break;
            case LEGS:
                playerModel.leftPants.visible = false;
                playerModel.rightPants.visible = false;
                break;
        }
    }

    private static void restoreSecondLayer(PlayerModel<?> playerModel) {
        playerModel.hat.visible = true;
        playerModel.jacket.visible = true;
        playerModel.leftSleeve.visible = true;
        playerModel.rightSleeve.visible = true;
        playerModel.leftPants.visible = true;
        playerModel.rightPants.visible = true;
    }
}