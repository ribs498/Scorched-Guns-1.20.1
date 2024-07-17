package top.ribs.scguns.client;

import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
import top.ribs.scguns.init.*;
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
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WHITE_NITER_GLASS.get(), RenderType.translucent());;
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.RED_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GREEN_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLUE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.YELLOW_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ORANGE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PURPLE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLACK_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PINK_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BROWN_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CYAN_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIGHT_BLUE_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIME_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MAGENTA_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GRAY_NITER_GLASS.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIGHT_GRAY_NITER_GLASS.get(), RenderType.translucent());
        registerAmmoCountProperty(ModItems.PISTOL_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.RIFLE_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SHOTGUN_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.MAGNUM_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.ROCKET_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SPECIAL_AMMO_BOX.get());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
        MenuScreens.register(ModMenuTypes.MACERATOR_MENU.get(), MaceratorScreen::new);
        MenuScreens.register(ModMenuTypes.SUPPLY_SCAMP_MENU.get(), SupplyScampScreen::new);
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
        EntityRenderers.register(ModEntities.HORNLIN.get(), HornlinRenderer::new);
        EntityRenderers.register(ModEntities.BLUNDERER.get(), BlundererRenderer::new);
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
        ModelOverrides.register(ModItems.HANDCANNON.get(), new HandcannonPistolModel());
        ModelOverrides.register(ModItems.MUSKET.get(), new MusketModel());
        ModelOverrides.register(ModItems.BLUNDERBUSS.get(), new BlunderbussModel());
        ModelOverrides.register(ModItems.REPEATING_MUSKET.get(), new RepeatingMusketModel());
        ModelOverrides.register(ModItems.LASER_MUSKET.get(), new LaserMusketModel());
        ModelOverrides.register(ModItems.FLOUNDERGAT.get(), new FloundergatModel());
        ModelOverrides.register(ModItems.SCRAPPER.get(), new ScrapperModel());
        ModelOverrides.register(ModItems.MAKESHIFT_RIFLE.get(), new MakeshiftRifleModel());
        ModelOverrides.register(ModItems.BOOMSTICK.get(), new BoomstickModel());
        ModelOverrides.register(ModItems.RUSTY_GNAT.get(), new RustyGnatModel());
        ModelOverrides.register(ModItems.BRUISER.get(), new BruisedMagnumModel());
        ModelOverrides.register(ModItems.LLR_DIRECTOR.get(), new LlrDirectorModel());
        ModelOverrides.register(ModItems.MARLIN.get(), new MarlinModel());
        ModelOverrides.register(ModItems.IRON_SPEAR.get(), new IronSpearModel());
        ModelOverrides.register(ModItems.M3_CARABINE.get(), new M3CarabineModel());
        ModelOverrides.register(ModItems.LOCKEWOOD.get(), new LockewoodModel());
        ModelOverrides.register(ModItems.GREASER_SMG.get(), new GreaserSmgModel());
        ModelOverrides.register(ModItems.DEFENDER_PISTOL.get(), new DefenderPistolModel());
        ModelOverrides.register(ModItems.COMBAT_SHOTGUN.get(), new CombatShotgunModel());
        ModelOverrides.register(ModItems.AUVTOMAG.get(), new AuvtomagModel());
        ModelOverrides.register(ModItems.EARTHS_CORPSE.get(), new EarthsCorpseModel());
        ModelOverrides.register(ModItems.ASTELLA.get(), new AstellaModel());
        ModelOverrides.register(ModItems.RAT_KING_AND_QUEEN.get(), new RatKingAndQueenModel());
        ModelOverrides.register(ModItems.LOCUST.get(), new LocustModel());
        ModelOverrides.register(ModItems.GYROJET_PISTOL.get(), new GyrojetPistolModel());
        ModelOverrides.register(ModItems.ROCKET_RIFLE.get(), new RocketRifleModel());
        ModelOverrides.register(ModItems.PRUSH_GUN.get(), new PrushGunModel());
        ModelOverrides.register(ModItems.INERTIAL.get(), new InertialModel());
        ModelOverrides.register(ModItems.COGLOADER.get(), new CogloaderModel());
        ModelOverrides.register(ModItems.PLASGUN.get(), new PlasgunModel());
        ModelOverrides.register(ModItems.GAUSS_RIFLE.get(), new GaussRifleModel());
        ModelOverrides.register(ModItems.OSGOOD_50.get(), new Osgood50Model());
        ModelOverrides.register(ModItems.NEWBORN_CYST.get(), new NewbornCystModel());

        ModelOverrides.register(ModItems.JACKHAMMER.get(), new JackhammerModel());
        ModelOverrides.register(ModItems.GATTALER.get(), new GattalerModel());
        ModelOverrides.register(ModItems.KRAUSER.get(), new KrauserModel());
        ModelOverrides.register(ModItems.HOWLER.get(), new HowlerModel());
        ModelOverrides.register(ModItems.HOWLER_CONVERSION.get(), new HowlerConversionModel());
        ModelOverrides.register(ModItems.M22_WALTZ.get(), new M22WaltzModel());
        ModelOverrides.register(ModItems.UPPERCUT.get(), new UppercutModel());
        ModelOverrides.register(ModItems.MAS_55.get(), new Mas55Model());
        ModelOverrides.register(ModItems.DOZIER_RL.get(), new DozierRLModel());
        ModelOverrides.register(ModItems.SPITFIRE.get(), new SpitfireModel());
        ModelOverrides.register(ModItems.CYCLONE.get(), new CycloneModel());
        ModelOverrides.register(ModItems.BLASPHEMY.get(), new BlasphemyModel());
        ModelOverrides.register(ModItems.PYROCLASTIC_FLOW.get(), new PyroclasticFlowModel());
        ModelOverrides.register(ModItems.RAYGUN.get(), new RaygunModel());
        ModelOverrides.register(ModItems.SUPER_SHOTGUN.get(), new SuperShotgunModel());
        ModelOverrides.register(ModItems.FREYR.get(), new FreyrModel());
        ModelOverrides.register(ModItems.VULCANIC_REPEATER.get(), new VulcanicRepeaterModel());
        ModelOverrides.register(ModItems.BOMB_LANCE.get(), new BombLanceModel());
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
        //event.register(new ResourceLocation(Reference.MOD_ID, "special/test"));
    }

//    public static void onRegisterCreativeTab(IEventBus bus) {
//        DeferredRegister<CreativeModeTab> register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);
//        CreativeModeTab.Builder builder = CreativeModeTab.builder();
//        builder.title(Component.translatable("itemGroup." + Reference.MOD_ID));
//        builder.icon(() -> {
//            ItemStack stack = new ItemStack(ModItems.M3_CARABINE.get());
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
