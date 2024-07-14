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


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);

    public static final RegistryObject<MenuType<MaceratorMenu>> MACERATOR_MENU =
            MENUS.register("macerator_menu", () -> IForgeMenuType.create(MaceratorMenu::new));

    public static final RegistryObject<MenuType<MechanicalPressMenu>> MECHANICAL_PRESS_MENU =
            MENUS.register("mechanical_press_menu", () -> IForgeMenuType.create(MechanicalPressMenu::new));
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
