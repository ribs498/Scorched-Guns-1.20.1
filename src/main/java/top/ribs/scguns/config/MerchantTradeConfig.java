package top.ribs.scguns.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = "scguns")
public class MerchantTradeConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, List<TradeData>> TRADE_SECTIONS = new HashMap<>();
    private static final Map<String, Integer> SECTION_LIMITS = new HashMap<>();
    private static int maxTotalOffers = 9; // Default max total offers
    private static final ResourceLocation CONFIG_LOCATION = new ResourceLocation("scguns", "entity/merchant_trades.json");

    public static class TradeData {
        public final ItemStack buyItem;
        public final ItemStack buyItem2; // Optional second buy item
        public final ItemStack sellItem;
        public final int maxUses;
        public final int xpReward;
        public final float priceMultiplier;
        public final String section;

        public TradeData(ItemStack buyItem, ItemStack buyItem2, ItemStack sellItem, int maxUses, int xpReward, float priceMultiplier, String section) {
            this.buyItem = buyItem;
            this.buyItem2 = buyItem2;
            this.sellItem = sellItem;
            this.maxUses = maxUses;
            this.xpReward = xpReward;
            this.priceMultiplier = priceMultiplier;
            this.section = section;
        }

        public MerchantOffer toMerchantOffer() {
            if (buyItem2.isEmpty()) {
                return new MerchantOffer(buyItem, sellItem, maxUses, xpReward, priceMultiplier);
            } else {
                return new MerchantOffer(buyItem, buyItem2, sellItem, maxUses, xpReward, priceMultiplier);
            }
        }
    }

    public static void loadConfig(ResourceManager resourceManager) {
        TRADE_SECTIONS.clear();
        SECTION_LIMITS.clear();
        try {
            Resource resource = resourceManager.getResource(CONFIG_LOCATION).orElse(null);
            if (resource != null) {
                try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    // Load general settings
                    if (json.has("max_total_offers")) {
                        maxTotalOffers = json.get("max_total_offers").getAsInt();
                    }

                    // Load section limits
                    if (json.has("section_limits")) {
                        JsonObject limitsObj = json.getAsJsonObject("section_limits");
                        for (Map.Entry<String, JsonElement> entry : limitsObj.entrySet()) {
                            SECTION_LIMITS.put(entry.getKey(), entry.getValue().getAsInt());
                        }
                    }

                    // Load trade sections
                    if (json.has("sections")) {
                        JsonObject sectionsObj = json.getAsJsonObject("sections");
                        for (Map.Entry<String, JsonElement> sectionEntry : sectionsObj.entrySet()) {
                            String sectionName = sectionEntry.getKey();
                            JsonArray tradesArray = sectionEntry.getValue().getAsJsonArray();

                            List<TradeData> sectionTrades = new ArrayList<>();
                            for (JsonElement element : tradesArray) {
                                JsonObject tradeObj = element.getAsJsonObject();
                                TradeData trade = parseTradeData(tradeObj, sectionName);
                                if (trade != null) {
                                    sectionTrades.add(trade);
                                }
                            }
                            TRADE_SECTIONS.put(sectionName, sectionTrades);
                        }
                    }

                    LOGGER.info("Loaded merchant trade config: {} sections with {} max offers",
                            TRADE_SECTIONS.size(), maxTotalOffers);
                    for (Map.Entry<String, List<TradeData>> entry : TRADE_SECTIONS.entrySet()) {
                        LOGGER.info("Section '{}': {} trades (limit: {})",
                                entry.getKey(), entry.getValue().size(),
                                SECTION_LIMITS.getOrDefault(entry.getKey(), 999));
                    }
                }
            } else {
                LOGGER.warn("Merchant trades config not found at {}, using defaults", CONFIG_LOCATION);
                loadDefaultTrades();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load merchant trades config at {}, using defaults", CONFIG_LOCATION, e);
            loadDefaultTrades();
        }
    }

    private static TradeData parseTradeData(JsonObject tradeObj, String section) {
        try {
            // Parse buy item (required)
            JsonObject buyItemObj = tradeObj.getAsJsonObject("buy_item");
            ItemStack buyItem = parseItemStack(buyItemObj);
            if (buyItem.isEmpty()) {
                LOGGER.warn("Invalid buy_item in trade config for section {}", section);
                return null;
            }

            // Parse optional second buy item
            ItemStack buyItem2 = ItemStack.EMPTY;
            if (tradeObj.has("buy_item_2")) {
                JsonObject buyItem2Obj = tradeObj.getAsJsonObject("buy_item_2");
                buyItem2 = parseItemStack(buyItem2Obj);
            }

            // Parse sell item (required)
            JsonObject sellItemObj = tradeObj.getAsJsonObject("sell_item");
            ItemStack sellItem = parseItemStack(sellItemObj);
            if (sellItem.isEmpty()) {
                LOGGER.warn("Invalid sell_item in trade config for section {}", section);
                return null;
            }

            // Parse trade properties
            int maxUses = tradeObj.has("max_uses") ? tradeObj.get("max_uses").getAsInt() : 2;
            int xpReward = tradeObj.has("xp_reward") ? tradeObj.get("xp_reward").getAsInt() : 5;
            float priceMultiplier = tradeObj.has("price_multiplier") ? tradeObj.get("price_multiplier").getAsFloat() : 0.05f;

            return new TradeData(buyItem, buyItem2, sellItem, maxUses, xpReward, priceMultiplier, section);

        } catch (Exception e) {
            LOGGER.error("Error parsing trade data for section {}", section, e);
            return null;
        }
    }

    private static ItemStack parseItemStack(JsonObject itemObj) {
        try {
            String itemId = itemObj.get("item").getAsString();
            int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;

            ResourceLocation itemLocation = new ResourceLocation(itemId);
            Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

            if (item == null || item == Items.AIR) {
                LOGGER.warn("Unknown item: {}", itemId);
                return ItemStack.EMPTY;
            }

            return new ItemStack(item, count);
        } catch (Exception e) {
            LOGGER.error("Error parsing item stack", e);
            return ItemStack.EMPTY;
        }
    }

    private static void loadDefaultTrades() {

    }

    public static MerchantOffers createMerchantOffers() {
        return createRandomizedOffers(new Random());
    }

    public static MerchantOffers createRandomizedOffers(Random random) {
        MerchantOffers offers = new MerchantOffers();
        List<TradeData> selectedTrades = new ArrayList<>();

        for (Map.Entry<String, List<TradeData>> sectionEntry : TRADE_SECTIONS.entrySet()) {
            String sectionName = sectionEntry.getKey();
            List<TradeData> sectionTrades = sectionEntry.getValue();
            int sectionLimit = SECTION_LIMITS.getOrDefault(sectionName, Math.min(3, sectionTrades.size()));

            if (!sectionTrades.isEmpty()) {
                List<TradeData> shuffledTrades = new ArrayList<>(sectionTrades);
                Collections.shuffle(shuffledTrades, random);

                int tradesToAdd = Math.min(sectionLimit, shuffledTrades.size());
                selectedTrades.addAll(shuffledTrades.subList(0, tradesToAdd));
            }
        }

        if (selectedTrades.size() > maxTotalOffers) {
            Collections.shuffle(selectedTrades, random);
            selectedTrades = selectedTrades.subList(0, maxTotalOffers);
        }

        // Convert to merchant offers
        for (TradeData trade : selectedTrades) {
            offers.add(trade.toMerchantOffer());
        }

        return offers;
    }

    public static int getMaxTotalOffers() {
        return maxTotalOffers;
    }

    public static Map<String, Integer> getSectionLimits() {
        return new HashMap<>(SECTION_LIMITS);
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                loadConfig(resourceManager);
            }
        });
    }
}