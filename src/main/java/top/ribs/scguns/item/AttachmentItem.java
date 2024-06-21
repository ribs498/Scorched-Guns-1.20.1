package top.ribs.scguns.item;

import top.ribs.scguns.client.handler.GunRenderingHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import top.ribs.scguns.init.ModItems;

/**
 * Author: MrCrayfish
 */
public class AttachmentItem extends Item implements IMeta
{
    public AttachmentItem(Properties properties)
    {
        super(properties);
    }

    /* Dirty hack to apply enchant effect to attachments if gun is enchanted */
    @Override
    public boolean isFoil(ItemStack stack)
    {
        if(FMLEnvironment.dist == Dist.CLIENT)
        {
            ItemStack weapon = GunRenderingHandler.get().getRenderingWeapon();
            if(weapon != null)
            {
                return weapon.getItem().isFoil(weapon);
            }
        }
        return super.isFoil(stack);
    }

    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(ModItems.REPAIR_KIT.get());
    }

}
