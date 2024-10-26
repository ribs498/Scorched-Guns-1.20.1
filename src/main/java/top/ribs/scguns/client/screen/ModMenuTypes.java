package top.ribs.scguns.client.screen;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.widget.ThermolithMenu;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);
    public static final RegistryObject<MenuType<AmmoBoxMenu>> AMMO_BOX =
            registerMenuType("ammo_box", AmmoBoxMenu::new);
    public static final RegistryObject<MenuType<ShellCatcherModuleMenu>> SHELL_CATCHER_MODULE =
            registerMenuType("shell_catcher_module", ShellCatcherModuleMenu::new);
    public static final RegistryObject<MenuType<AmmoModuleMenu>> AMMO_MODULE =
            registerMenuType("ammo_module", AmmoModuleMenu::new);
    public static final RegistryObject<MenuType<CryoniterMenu>> CRYONITER_MENU =
            MENUS.register("cryoniter_menu", () -> IForgeMenuType.create(CryoniterMenu::new));
    public static final RegistryObject<MenuType<VentCollectorMenu>> VENT_COLLECTOR_MENU =
            MENUS.register("vent_collector_menu", () -> IForgeMenuType.create(VentCollectorMenu::new));
    public static final RegistryObject<MenuType<ThermolithMenu>> THERMOLITH_MENU =
            MENUS.register("thermolith_menu", () -> IForgeMenuType.create(ThermolithMenu::new));
    public static final RegistryObject<MenuType<PolarGeneratorMenu>> POLAR_GENERATOR_MENU =
            MENUS.register("polar_generator_menu", () -> IForgeMenuType.create(PolarGeneratorMenu::new));
    public static final RegistryObject<MenuType<MaceratorMenu>> MACERATOR_MENU =
            MENUS.register("macerator_menu", () -> IForgeMenuType.create(MaceratorMenu::new));
    public static final RegistryObject<MenuType<PoweredMaceratorMenu>> POWERED_MACERATOR_MENU =
            MENUS.register("powered_macerator_menu", () -> IForgeMenuType.create(PoweredMaceratorMenu::new));
public static final RegistryObject<MenuType<BasicTurretMenu>> BASIC_TURRET_MENU =
            MENUS.register("basic_turret_menu", () -> IForgeMenuType.create(BasicTurretMenu::new));
    public static final RegistryObject<MenuType<ShotgunTurretMenu>> SHOTGUN_TURRET_MENU =
            MENUS.register("shotgun_turret_menu", () -> IForgeMenuType.create(ShotgunTurretMenu::new));
    public static final RegistryObject<MenuType<AutoTurretMenu>> AUTO_TURRET_MENU =
            MENUS.register("auto_turret_menu", () -> IForgeMenuType.create(AutoTurretMenu::new));
    public static final RegistryObject<MenuType<LightningBatteryMenu>> LIGHTING_BATTERY_MENU =
            MENUS.register("lightning_battery_menu", () -> IForgeMenuType.create(LightningBatteryMenu::new));
    public static final RegistryObject<MenuType<MechanicalPressMenu>> MECHANICAL_PRESS_MENU =
            MENUS.register("mechanical_press_menu", () -> IForgeMenuType.create(MechanicalPressMenu::new));
    public static final RegistryObject<MenuType<PoweredMechanicalPressMenu>> POWERED_MECHANICAL_PRESS_MENU =
            MENUS.register("powered_mechanical_press_menu", () -> IForgeMenuType.create(PoweredMechanicalPressMenu::new));

    public static final RegistryObject<MenuType<GunBenchMenu>> GUN_BENCH
            = registerMenuType("gun_bench", GunBenchMenu::new);

    public static final RegistryObject<MenuType<ChestMenu>> SUPPLY_SCAMP_MENU =
            registerMenuType("supply_scamp_menu", (id, playerInventory, buffer) ->
                    new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, new SimpleContainer(27), 3));

    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
