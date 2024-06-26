package top.ribs.scguns.client;

import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.*;
import top.ribs.scguns.client.render.block.MaceratorRenderer;
import top.ribs.scguns.client.render.block.MechanicalPressRenderer;
import top.ribs.scguns.client.render.gun.ModelOverrides;
import top.ribs.scguns.client.render.gun.model.*;
import top.ribs.scguns.client.screen.*;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.debug.IEditorMenu;
import top.ribs.scguns.debug.client.screen.EditorScreen;
import top.ribs.scguns.entity.client.*;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.init.ModContainers;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageAttachments;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    private static Field mouseOptionsField;

    public static void registerClientHandlers(IEventBus bus) {
        FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
       // onRegisterCreativeTab(bus);
        bus.addListener(KeyBinds::registerKeyMappings);
        bus.addListener(CrosshairHandler::onConfigReload);
        bus.addListener(ClientHandler::onRegisterReloadListener);
        bus.addListener(ClientHandler::registerAdditional);
        bus.addListener(ClientHandler::onClientSetup);
        MinecraftForge.EVENT_BUS.register(HUDRenderHandler.class);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(ModBlockEntities.MACERATOR.get(), MaceratorRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MECHANICAL_PRESS.get(), MechanicalPressRenderer::new);
        registerAmmoCountProperty(ModItems.PISTOL_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.RIFLE_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SHOTGUN_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.MAGNUM_AMMO_BOX.get());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
        MenuScreens.register(ModMenuTypes.MACERATOR_MENU.get(), MaceratorScreen::new);
        MenuScreens.register(ModMenuTypes.MECHANICAL_PRESS_MENU.get(), MechanicalPressScreen::new);
        MenuScreens.register(ModMenuTypes.GUN_BENCH.get(), GunBenchScreen::new);
        EntityRenderers.register(ModEntities.COG_MINION.get(), CogMinionRenderer::new);
        EntityRenderers.register(ModEntities.COG_KNIGHT.get(), CogKnightRenderer::new);
        EntityRenderers.register(ModEntities.SKY_CARRIER.get(), SkyCarrierRenderer::new);
        EntityRenderers.register(ModEntities.SUPPLY_SCAMP.get(), SupplyScampRenderer::new);
        EntityRenderers.register(ModEntities.REDCOAT.get(), RedcoatRenderer::new);
        EntityRenderers.register(ModEntities.HIVE.get(), HiveRenderer::new);
        EntityRenderers.register(ModEntities.SWARM.get(), SwarmRenderer::new);
        EntityRenderers.register(ModEntities.DISSIDENT.get(), DissidentRenderer::new);
        EntityRenderers.register(ModEntities.BRASS_BOLT.get(), BrassBoltRenderer::new);

        event.enqueueWork(ClientHandler::setup);
    }

    public static void setup() {
        MinecraftForge.EVENT_BUS.register(AimingHandler.get());
        MinecraftForge.EVENT_BUS.register(BulletTrailRenderingHandler.get());
        MinecraftForge.EVENT_BUS.register(CrosshairHandler.get());
        MinecraftForge.EVENT_BUS.register(GunRenderingHandler.get());
        MinecraftForge.EVENT_BUS.register(RecoilHandler.get());
        MinecraftForge.EVENT_BUS.register(ReloadHandler.get());
        MinecraftForge.EVENT_BUS.register(ShootingHandler.get());
        MinecraftForge.EVENT_BUS.register(SoundHandler.get());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());

        if (ScorchedGuns.controllableLoaded) {
            ControllerHandler.init();
            GunButtonBindings.register();
        }
        setupRenderLayers();
        registerModelOverrides();
        registerScreenFactories();
    }

    private static void setupRenderLayers() {
        // Implement render layer setup here
    }
    private static void registerModelOverrides() {
        ModelOverrides.register(ModItems.FLINTLOCK_PISTOL.get(), new FlintlockPistolModel());
        ModelOverrides.register(ModItems.MUSKET.get(), new MusketModel());
        ModelOverrides.register(ModItems.BLUNDERBUSS.get(), new BlunderbussModel());
        ModelOverrides.register(ModItems.REPEATING_MUSKET.get(), new RepeatingMusketModel());
        ModelOverrides.register(ModItems.COPPER_PISTOL.get(), new CopperPistolModel());
        ModelOverrides.register(ModItems.COPPER_RIFLE.get(), new CopperRifleModel());
        ModelOverrides.register(ModItems.COPPER_SHOTGUN.get(), new CopperShotgunModel());
        ModelOverrides.register(ModItems.COPPER_SMG.get(), new CopperSmgModel());
        ModelOverrides.register(ModItems.COPPER_MAGNUM.get(), new CopperMagnumModel());
        ModelOverrides.register(ModItems.IRON_SPEAR.get(), new IronSpearModel());
        ModelOverrides.register(ModItems.IRON_CARABINE.get(), new IronCarabineModel());
        ModelOverrides.register(ModItems.IRON_SMG.get(), new IronSmgModel());
        ModelOverrides.register(ModItems.DEFENDER_PISTOL.get(), new DefenderPistolModel());
        ModelOverrides.register(ModItems.COMBAT_SHOTGUN.get(), new CombatShotgunModel());
        ModelOverrides.register(ModItems.GYROJET_PISTOL.get(), new GyrojetPistolModel());
        ModelOverrides.register(ModItems.ROCKET_RIFLE.get(), new RocketRifleModel());
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof MouseSettingsScreen) {
            MouseSettingsScreen screen = (MouseSettingsScreen) event.getScreen();
            if (mouseOptionsField == null) {
                mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
                mouseOptionsField.setAccessible(true);
            }
            try {
                OptionsList list = (OptionsList) mouseOptionsField.get(screen);
                //list.addSmall(GunOptions.ADS_SENSITIVITY, GunOptions.CROSSHAIR);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && event.getAction() == GLFW.GLFW_PRESS) {
            if (KeyBinds.KEY_ATTACHMENTS.isDown()) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
            }
        }
    }

    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            PropertyHelper.resetCache();
        });
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(Reference.MOD_ID, "special/test"));
    }

//    public static void onRegisterCreativeTab(IEventBus bus) {
//        DeferredRegister<CreativeModeTab> register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);
//        CreativeModeTab.Builder builder = CreativeModeTab.builder();
//        builder.title(Component.translatable("itemGroup." + Reference.MOD_ID));
//        builder.icon(() -> {
//            ItemStack stack = new ItemStack(ModItems.IRON_CARABINE.get());
//            stack.getOrCreateTag().putBoolean("IgnoreAmmo", true);
//            return stack;
//        });
//        builder.displayItems((flags, output) -> {
//            ModItems.REGISTER.getEntries().forEach(registryObject -> {
//                if (registryObject.get() instanceof GunItem item) {
//                    ItemStack stack = new ItemStack(item);
//                    stack.getOrCreateTag().putInt("AmmoCount", item.getGun().getReloads().getMaxAmmo());
//                    output.accept(stack);
//                    return;
//                }
//                output.accept(registryObject.get());
//            });
//            CustomGunManager.fill(output);
//            for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
//                if (enchantment.category == EnchantmentTypes.GUN || enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
//                    output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchantment.getMaxLevel())), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
//                }
//            }
//        });
//        register.register("creative_tab", builder::build);
//        register.register(bus);
//    }

    public static Screen createEditorScreen(IEditorMenu menu) {
        return new EditorScreen(Minecraft.getInstance().screen, menu);
    }

    private static void registerAmmoCountProperty(Item item) {
        ItemProperties.register(item, new ResourceLocation("ammo_count"),
                (stack, world, entity, seed) -> {
                    int totalItemCount = AmmoBoxItem.getTotalItemCount(stack);
                    int maxItemCount = AmmoBoxItem.getMaxItemCount(stack);
                    if (totalItemCount == 0) {
                        return 0.0f;
                    } else if (totalItemCount <= maxItemCount / 3) {
                        return 0.33f;
                    } else if (totalItemCount <= 2 * maxItemCount / 3) {
                        return 0.66f;
                    } else {
                        return 1.0f;
                    }
                });
    }
}
