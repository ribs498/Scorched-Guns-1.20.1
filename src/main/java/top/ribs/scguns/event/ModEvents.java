package top.ribs.scguns.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModVillagers;

import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {

        if(event.getType() == ModVillagers.GUNSMITH.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            // Level 1 Trades
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 8),
                    new ItemStack(ModItems.GUN_GRIP.get(), 1),
                    10, 2, 0.02f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 9),
                    new ItemStack(ModItems.GUN_BARREL.get(), 1),
                    10, 2, 0.02f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.SMALL_COPPER_CASING.get(), 6),
                    new ItemStack(Items.EMERALD, 1),
                    10, 2, 0.02f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.IRON_INGOT, 4),
                    new ItemStack(Items.EMERALD, 1),
                    12, 5, 0.02f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.BUCKSHOT.get(), 4),
                    new ItemStack(Items.EMERALD, 1),
                    16, 5, 0.02f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 6),
                    new ItemStack(ModItems.SMALL_CASING_MOLD.get(), 1),
                    3, 10, 0.05f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    new ItemStack(ModItems.SMALL_IRON_CASING.get(), 8),
                    10, 10, 0.05f));
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.ANTHRALITE_INGOT.get(), 3),
                    new ItemStack(Items.EMERALD, 1),
                    12, 10, 0.05f));

            // Level 2 Trades
            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 25),
                    new ItemStack(ModItems.COPPER_BLUEPRINT.get(), 1),
                    2, 12, 0.05f));

            // Level 3 Trades
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 9),
                    new ItemStack(ModItems.LASER_SIGHT.get(), 1),
                    6, 15, 0.05f));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 8),
                    new ItemStack(ModItems.RIFLE_AMMO_BOX.get(), 1),
                    6, 15, 0.05f));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.GUN_PARTS.get(), 1),
                    new ItemStack(Items.EMERALD, 4),
                    10, 15, 0.05f));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.GUNPOWDER, 8),
                    new ItemStack(Items.EMERALD, 1),
                    16, 15, 0.05f));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 8),
                    new ItemStack(ModItems.EXTENDED_MAG.get(), 1),
                    10, 10, 0.05f));
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 15),
                    new ItemStack(ModItems.MUSKET.get(), 1),
                    2, 12, 0.05f));

            // Level 4 Trades
            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 13),
                    new ItemStack(ModItems.SPEED_MAG.get(), 1),
                    4, 20, 0.05f));
            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 16),
                    new ItemStack(ModItems.LONG_SCOPE.get(), 1),
                    4, 20, 0.05f));
            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.MEDIUM_BRASS_CASING.get(), 6),
                    new ItemStack(Items.EMERALD, 1),
                    10, 20, 0.05f));
            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.BLAZE_POWDER, 4),
                    new ItemStack(Items.EMERALD, 1),
                    12, 20, 0.05f));
            trades.get(4).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 1),
                    new ItemStack(ModItems.LARGE_CASING_MOLD.get(), 1),
                    5, 20, 0.05f));

            // Level 5 Trades
            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 25),
                    new ItemStack(ModItems.BOOMSTICK.get(), 1),
                    1, 25, 0.05f));
            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 32),
                    new ItemStack(ModItems.MAKESHIFT_RIFLE.get(), 1),
                    1, 25, 0.05f));
            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 25),
                    new ItemStack(ModItems.IRON_BLUEPRINT.get(), 1),
                    1, 30, 0.05f));
            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.HEAVY_GUN_PARTS.get(), 2),
                    new ItemStack(Items.EMERALD, 1),
                    8, 25, 0.05f));
            trades.get(5).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(ModItems.NITRO_POWDER.get(), 1),
                    new ItemStack(Items.EMERALD, 14),
                    8, 25, 0.05f));
        }
    }

    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();
        List<VillagerTrades.ItemListing> trades = event.getGenericTrades();

        trades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 24),
                new ItemStack(ModItems.TREATED_BRASS_BLUEPRINT.get(), 1),
                1, 12, 0.15f));
        trades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 24),
                new ItemStack(ModItems.DIAMOND_STEEL_BLUEPRINT.get(), 1),
                1, 12, 0.15f));
        rareTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 23),
                new ItemStack(ModItems.VEHEMENT_COAL.get(), 1),
                1, 12, 0.15f));
    }
}
