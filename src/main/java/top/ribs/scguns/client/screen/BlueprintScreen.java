package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageSetBlueprintRecipe;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BlueprintScreen extends Screen {
    public static final ResourceLocation BLUEPRINT_TEXTURE = new ResourceLocation("scguns", "textures/gui/blueprint.png");

    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private static final int SLOT_SIZE = 16;
    private final List<RecipeSlot> recipeSlots = new ArrayList<>();
    private final InteractionHand hand;
    private final ItemStack[] recipeItems = new ItemStack[12];

    private int currentPage = 0;
    private int maxPages = 0;
    private BlueprintPageButton nextPageButton;
    private BlueprintPageButton prevPageButton;

    private final ItemStack blueprintStack;
    private final List<DisplayEntry> displayEntries = new ArrayList<>();

    private static final List<String> GUN_ORDER = new ArrayList<>(Arrays.asList(

            //ANTIQUE
            "flintlock_pistol", "handcannon", "musket", "blunderbuss", "doublet", "repeating_musket",
            "longarm", "fencer_carabine", "fencer_thumper",  "laser_musket", "plasmabuss", "teslock_rifle",

            //FRONTIER
            "pax", "winnie","winnie_millend", "red_raydar", "callwell", "callwell_conversion", "callwell_terminal", "saketini",
            "saketini_ironport", "kiln_gun", "big_bore",

            //COPPER
            "scrapper", "rusty_gnat", "umax_pistol", "makeshift_rifle", "boomstick", "bruiser",
            "llr_director", "birdfeeder", "whistler", "blooper", "arc_worker",

            //IRON
            "defender_pistol", "trenchur", "greaser_smg", "m3_carabine", "m3_marksman","combat_shotgun", "venturi",
            "iron_javelin", "iron_spear", "auvtomag", "pulsar", "gyrojet_pistol", "brawler",
            "crusader", "mk43_rifle", "triquetra", "rocket_rifle", "ultra_knight_hawk",

            //OCEAN
            "floundergat", "hyperbaria", "marlin", "bomb_lance", "hullbreaker", "sequoia", "spirulida",

            //WRECKER
            "mokova", "mak_mkii", "stilleto", "railworker", "stiletto",
            "turnpike", "killer_23", "homemaker", "kalaskah", "basker", "tl_runner", "stigg", "whizzbanger",

            //DIAMONDSTEEL
            "krauser", "soul_drummer", "uppercut", "micina", "valora", "prush_gun", "drill", "drill_conversion", "lockewood",
            "zilk_45", "rg_jigsaw","nailer", "inertial", "minksy","mas_55", "mas_peddler",
            "inquisitor", "plasgun", "truant", "cyclone", "shard_culler",

            //TREATEDBRASS
            "m22_waltz", "waltz_conversion", "osgood_50", "grandle_og", "grandle", "cogloader", "gale","jr_wristbreaker", "jackhammer",
            "howler", "howler_conversion", "gauss_rifle", "libertas", "niami", "hammer_gl","spitfire", "gattaler",
            "thunderhead", "scratches", "cr4k_mining_laser", "dozier_rl",

            //PIGLIN
            "empty_blasphemy", "blasphemy", "pyroclastic_flow", "freyr", "mangalitsa", "vulcanic_repeater", "trotters", "super_shotgun",

            //SCULK
            "whispers", "echoes_2", "sculk_resonator", "forlorn_hope",

            //END
            "carapice", "shellurker", "weevil", "dark_matter", "lone_wonder", "raygun",

            //SCORCHED
            "prima_materia", "rat_king_and_queen", "locust", "sterilizer", "newborn_cyst", "earths_corpse",
            "flayed_god", "nervepinch", "terra_incognita", "astella",

            //EXOSUIT
            "exo_suit_helmet", "exo_suit_chestplate", "exo_suit_leggings", "exo_suit_boots"
    ));

    private static final Map<ResourceLocation, List<String>> LORE_ONLY_ITEMS = new HashMap<>();

    static {
        LORE_ONLY_ITEMS.put(
                new ResourceLocation("scguns", "piglin_blueprint"),
                new ArrayList<>(Arrays.asList("blasphemy", "super_shotgun"))
        );
        LORE_ONLY_ITEMS.put(
                new ResourceLocation("scguns", "frontier_blueprint"),
                new ArrayList<>(Arrays.asList("kiln_gun"))
        );
    }

    /**
     * Register a gun item in the ordering list at the end.
     * Call this during mod initialization for addons.
     */
    public static void registerGunOrder(String itemName) {
        if (!GUN_ORDER.contains(itemName)) {
            GUN_ORDER.add(itemName);
        }
    }

    /**
     * Register multiple gun items in the ordering list at the end.
     */
    public static void registerGunOrder(List<String> itemNames) {
        for (String itemName : itemNames) {
            registerGunOrder(itemName);
        }
    }

    /**
     * Insert a gun item at a specific position in the ordering list.
     */
    public static void insertGunOrder(int index, String itemName) {
        if (!GUN_ORDER.contains(itemName)) {
            GUN_ORDER.add(Math.min(index, GUN_ORDER.size()), itemName);
        }
    }

    /**
     * Register a lore-only item for a specific blueprint type.
     */
    public static void registerLoreOnlyItem(ResourceLocation blueprintId, String itemName) {
        LORE_ONLY_ITEMS.computeIfAbsent(blueprintId, k -> new ArrayList<>()).add(itemName);
    }

    /**
     * Register multiple lore-only items for a specific blueprint type.
     */
    public static void registerLoreOnlyItems(ResourceLocation blueprintId, List<String> itemNames) {
        LORE_ONLY_ITEMS.computeIfAbsent(blueprintId, k -> new ArrayList<>()).addAll(itemNames);
    }

    /**
     * Get the current gun ordering list (for reference).
     */
    public static List<String> getGunOrder() {
        return new ArrayList<>(GUN_ORDER);
    }

    /**
     * Get lore-only items for a specific blueprint (for reference).
     */
    public static List<String> getLoreOnlyItems(ResourceLocation blueprintId) {
        return new ArrayList<>(LORE_ONLY_ITEMS.getOrDefault(blueprintId, Collections.emptyList()));
    }

    private record RecipeSlot(int x, int y, int index) {}

    private static class DisplayEntry {
        final GunBenchRecipe recipe;
        final ItemStack itemStack;
        final boolean hasRecipe;

        DisplayEntry(GunBenchRecipe recipe) {
            this.recipe = recipe;
            this.itemStack = null;
            this.hasRecipe = true;
        }

        DisplayEntry(ItemStack itemStack) {
            this.recipe = null;
            this.itemStack = itemStack;
            this.hasRecipe = false;
        }
    }

    public static class BlueprintPageButton extends Button {
        public BlueprintPageButton(int x, int y, Component text, OnPress onPress) {
            super(x, y, 20, 20, text, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            Minecraft.getInstance().player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        }
    }

    public BlueprintScreen(ItemStack blueprintStack, Player player, InteractionHand hand) {
        super(Component.translatable("screen.scguns.blueprint.title"));

        this.blueprintStack = blueprintStack;
        this.hand = hand;
        Arrays.fill(recipeItems, ItemStack.EMPTY);
        loadAvailableEntries();
        loadActiveRecipeAsCurrentPage();
    }

    private void loadActiveRecipeAsCurrentPage() {
        ResourceLocation activeRecipeId = getActiveRecipe(blueprintStack);
        if (activeRecipeId != null) {
            for (int i = 0; i < displayEntries.size(); i++) {
                DisplayEntry entry = displayEntries.get(i);
                if (entry.hasRecipe && entry.recipe.getId().equals(activeRecipeId)) {
                    currentPage = i;
                    loadCurrentPageRecipe();
                    return;
                }
            }
        }
        loadCurrentPageRecipe();
    }

    private void loadCurrentPageRecipe() {
        if (currentPage < displayEntries.size()) {
            DisplayEntry entry = displayEntries.get(currentPage);
            if (entry.hasRecipe) {
                loadRecipeIntoSlots(entry.recipe);
            } else {
                loadLoreItemIntoSlots(entry.itemStack);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.createMenuControls();
        this.setupRecipeSlots();
    }

    private void setupRecipeSlots() {
        recipeSlots.clear();

        int centerX = (this.width - IMAGE_WIDTH) / 2;
        int centerY = 2;

        recipeSlots.add(new RecipeSlot(centerX + 26, centerY + 17, 0));
        recipeSlots.add(new RecipeSlot(centerX + 44, centerY + 17, 1));
        recipeSlots.add(new RecipeSlot(centerX + 62, centerY + 17, 2));
        recipeSlots.add(new RecipeSlot(centerX + 80, centerY + 17, 3));
        recipeSlots.add(new RecipeSlot(centerX + 26, centerY + 35, 4));
        recipeSlots.add(new RecipeSlot(centerX + 44, centerY + 35, 5));
        recipeSlots.add(new RecipeSlot(centerX + 62, centerY + 35, 6));
        recipeSlots.add(new RecipeSlot(centerX + 80, centerY + 35, 7));
        recipeSlots.add(new RecipeSlot(centerX + 26, centerY + 53, 8));
        recipeSlots.add(new RecipeSlot(centerX + 62, centerY + 53, 9));
        recipeSlots.add(new RecipeSlot(centerX + 116, centerY + 17, 11));
        recipeSlots.add(new RecipeSlot(centerX + 140, centerY + 44, 10));
    }

    protected void createMenuControls() {
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose())
                        .bounds(this.width / 2 - 100, 196, 200, 20)
                        .build()
        );

        DisplayEntry currentEntry = currentPage < displayEntries.size() ? displayEntries.get(currentPage) : null;
        boolean canSetRecipe = currentEntry != null && currentEntry.hasRecipe;

        Button setRecipeButton = Button.builder(
                Component.translatable("screen.scguns.blueprint.button.set_recipe"),
                (button) -> this.setActiveRecipe()
        ).bounds(this.width / 2 - 100, 220, 200, 20).build();

        setRecipeButton.active = canSetRecipe;
        this.addRenderableWidget(setRecipeButton);

        createPageControls();
    }

    private void setActiveRecipe() {
        if (currentPage < displayEntries.size()) {
            DisplayEntry entry = displayEntries.get(currentPage);
            if (entry.hasRecipe) {
                PacketHandler.getPlayChannel().sendToServer(
                        new C2SMessageSetBlueprintRecipe(hand, entry.recipe.getId().toString())
                );
                saveActiveRecipe(blueprintStack, entry.recipe);
            }
        }
        this.onClose();
    }

    private void createPageControls() {
        int centerX = (this.width - IMAGE_WIDTH) / 2;
        int centerY = 2;

        int buttonY = centerY + IMAGE_HEIGHT - 40;
        int rightSide = centerX + IMAGE_WIDTH - 35;

        this.prevPageButton = this.addRenderableWidget(
                new BlueprintPageButton(rightSide - 40, buttonY, Component.literal("<"),
                        (button) -> this.previousPage())
        );

        this.nextPageButton = this.addRenderableWidget(
                new BlueprintPageButton(rightSide - 15, buttonY, Component.literal(">"),
                        (button) -> this.nextPage())
        );

        updatePageButtonStates();
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCurrentPageRecipe();
            updatePageButtonStates();
        }
    }

    private void nextPage() {
        if (currentPage < maxPages - 1) {
            currentPage++;
            loadCurrentPageRecipe();
            updatePageButtonStates();
        }
    }

    private void updatePageButtonStates() {
        if (prevPageButton != null) {
            prevPageButton.active = currentPage > 0;
            prevPageButton.visible = true;
        }
        if (nextPageButton != null) {
            nextPageButton.active = currentPage < maxPages - 1;
            nextPageButton.visible = true;
        }
    }

    private void loadAvailableEntries() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        displayEntries.clear();
        List<GunBenchRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(GunBenchRecipe.Type.INSTANCE);

        if (blueprintStack.isEmpty()) {
            for (GunBenchRecipe recipe : allRecipes) {
                displayEntries.add(new DisplayEntry(recipe));
            }
        } else {
            for (GunBenchRecipe recipe : allRecipes) {
                if (recipe.getBlueprint().test(blueprintStack)) {
                    displayEntries.add(new DisplayEntry(recipe));
                }
            }
            ResourceLocation blueprintItemId = ForgeRegistries.ITEMS.getKey(blueprintStack.getItem());
            if (blueprintItemId != null) {
                List<String> loreItems = LORE_ONLY_ITEMS.get(blueprintItemId);
                if (loreItems != null) {
                    for (String itemName : loreItems) {
                        ResourceLocation itemLocation = new ResourceLocation(blueprintItemId.getNamespace(), itemName);
                        net.minecraft.world.item.Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
                        if (item != null) {
                            displayEntries.add(new DisplayEntry(new ItemStack(item)));
                        }
                    }
                }
            }
        }

        sortEntriesByProgression();

        maxPages = Math.max(1, displayEntries.size());
        currentPage = 0;
    }

    private void sortEntriesByProgression() {
        displayEntries.sort((entry1, entry2) -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return 0;

            String item1Name = getItemNameFromEntry(entry1, level);
            String item2Name = getItemNameFromEntry(entry2, level);

            int index1 = getOrderIndex(item1Name);
            int index2 = getOrderIndex(item2Name);

            return Integer.compare(index1, index2);
        });
    }

    private String getItemNameFromEntry(DisplayEntry entry, Level level) {
        ItemStack resultItem = entry.hasRecipe ?
                entry.recipe.getResultItem(level.registryAccess()) :
                entry.itemStack;

        if (!resultItem.isEmpty()) {
            ResourceLocation itemLocation = ForgeRegistries.ITEMS.getKey(resultItem.getItem());
            if (itemLocation != null) {
                return itemLocation.getPath();
            }
        }
        return "";
    }

    private int getOrderIndex(String itemName) {
        int index = GUN_ORDER.indexOf(itemName);
        return index == -1 ? Integer.MAX_VALUE : index;
    }

    private void loadRecipeIntoSlots(GunBenchRecipe recipe) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        clearRecipeItems();

        for (int i = 0; i < recipe.getIngredients().size() && i < 10; i++) {
            Ingredient ingredient = recipe.getIngredients().get(i);
            if (!ingredient.isEmpty()) {
                ItemStack[] stacks = ingredient.getItems();
                if (stacks.length > 0) {
                    recipeItems[i] = stacks[0].copy();
                }
            }
        }

        if (!recipe.getBlueprint().isEmpty()) {
            ItemStack[] blueprintStacks = recipe.getBlueprint().getItems();
            if (blueprintStacks.length > 0) {
                recipeItems[11] = blueprintStacks[0].copy();
            }
        }

        recipeItems[10] = recipe.getResultItem(level.registryAccess()).copy();
    }

    private void loadLoreItemIntoSlots(ItemStack item) {
        clearRecipeItems();
        recipeItems[10] = item.copy();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = (this.width - IMAGE_WIDTH) / 2;
        int centerY = 2;
        guiGraphics.blit(BLUEPRINT_TEXTURE, centerX, centerY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        renderRecipeSlots(guiGraphics, mouseX, mouseY);
        renderGunInfo(guiGraphics, centerX, centerY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderItemTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderGunInfo(GuiGraphics guiGraphics, int centerX, int centerY) {
        if (currentPage < displayEntries.size()) {
            DisplayEntry entry = displayEntries.get(currentPage);
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            ItemStack resultItem = entry.hasRecipe ?
                    entry.recipe.getResultItem(level.registryAccess()) :
                    entry.itemStack;

            if (!resultItem.isEmpty()) {
                String gunName = Component.translatable(resultItem.getDescriptionId()).getString();
                PoseStack poseStack = guiGraphics.pose();

                int titleY = centerY + 77;

                poseStack.pushPose();
                poseStack.scale(1.1f, 1.1f, 1.0f);

                int scaledX = (int) ((centerX + 30) / 1.1f);
                int scaledY = (int) (titleY / 1.1f);

                guiGraphics.drawString(this.font, gunName, scaledX, scaledY, 0x212057, false);
                poseStack.popPose();

                int titleHeight = (int) (this.font.lineHeight * 1.1f);
                int descriptionY = titleY + titleHeight + 6;
                String gunRegistryName = resultItem.getItem().toString().replace("item.scguns.", "");
                String descriptionKey = "scguns.desc." + gunRegistryName;
                String description = Component.translatable(descriptionKey).getString();
                if (description.equals(descriptionKey)) {
                    description = Component.translatable("scguns.desc.unknown").getString();
                }

                int maxWidth = IMAGE_WIDTH - 30;
                List<String> wrappedLines = wrapText(description, maxWidth);

                poseStack.pushPose();
                poseStack.scale(0.9f, 0.9f, 1.0f);

                for (int i = 0; i < wrappedLines.size(); i++) {
                    int scaledDescX = (int) ((centerX + 22) / 0.9f);
                    int scaledDescY = (int) ((descriptionY + (i * this.font.lineHeight)) / 0.9f);

                    guiGraphics.drawString(this.font, wrappedLines.get(i),
                            scaledDescX, scaledDescY, 0x4A496B, false);
                }

                poseStack.popPose();
            }
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (this.font.width(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private void renderRecipeSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (RecipeSlot slot : recipeSlots) {
            boolean isHovered = isMouseOverSlot(slot, mouseX, mouseY);

            if (isHovered) {
                guiGraphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, 0x80FFFFFF);
            }

            ItemStack itemStack = recipeItems[slot.index];
            if (!itemStack.isEmpty()) {
                guiGraphics.renderItem(itemStack, slot.x, slot.y);
                guiGraphics.renderItemDecorations(this.font, itemStack, slot.x, slot.y);
            }
        }
    }

    private void renderItemTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (RecipeSlot slot : recipeSlots) {
            if (isMouseOverSlot(slot, mouseX, mouseY)) {
                ItemStack itemStack = recipeItems[slot.index];
                if (!itemStack.isEmpty()) {
                    guiGraphics.renderTooltip(this.font, itemStack, mouseX, mouseY);
                }
                break;
            }
        }
    }

    private boolean isMouseOverSlot(RecipeSlot slot, int mouseX, int mouseY) {
        return mouseX >= slot.x && mouseX < slot.x + SLOT_SIZE &&
                mouseY >= slot.y && mouseY < slot.y + SLOT_SIZE;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            return switch (keyCode) {
                case 266 -> {
                    if (this.prevPageButton.active) {
                        this.prevPageButton.onPress();
                    }
                    yield true;
                }
                case 267 -> {
                    if (this.nextPageButton.active) {
                        this.nextPageButton.onPress();
                    }
                    yield true;
                }
                default -> false;
            };
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void clearRecipeItems() {
        Arrays.fill(recipeItems, ItemStack.EMPTY);
    }

    private void saveActiveRecipe(ItemStack blueprint, GunBenchRecipe recipe) {
        blueprint.getOrCreateTag().putString("ActiveRecipe", recipe.getId().toString());
    }

    public static ResourceLocation getActiveRecipe(ItemStack blueprint) {
        if (blueprint.hasTag()) {
            assert blueprint.getTag() != null;
            if (blueprint.getTag().contains("ActiveRecipe")) {
                String recipeIdString = blueprint.getTag().getString("ActiveRecipe");
                return new ResourceLocation(recipeIdString);
            }
        }
        return null;
    }

    public static String getActiveRecipeName(ItemStack blueprint) {
        ResourceLocation recipeId = getActiveRecipe(blueprint);
        if (recipeId == null) return null;

        Level level = Minecraft.getInstance().level;
        if (level == null) return null;

        Optional<GunBenchRecipe> recipe = level.getRecipeManager()
                .getAllRecipesFor(GunBenchRecipe.Type.INSTANCE)
                .stream()
                .filter(r -> r.getId().equals(recipeId))
                .findFirst();

        return recipe.map(gunBenchRecipe -> gunBenchRecipe.getResultItem(level.registryAccess()).getDisplayName().getString()).orElse(null);
    }
}