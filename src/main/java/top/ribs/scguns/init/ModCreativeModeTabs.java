package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.GunItem;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SCORCHED_GUNS_TAB = CREATIVE_MODE_TABS.register("scorched_guns_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.IRON_CARABINE.get()))
                    .title(Component.translatable("creativetab.scorched_guns_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FLINTLOCK_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BLUNDERBUSS.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.REPEATING_MUSKET.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COPPER_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COPPER_SMG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COPPER_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COPPER_SHOTGUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COPPER_MAGNUM.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DEFENDER_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.IRON_SMG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.IRON_CARABINE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COMBAT_SHOTGUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.IRON_SPEAR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GYROJET_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ROCKET_RIFLE.get());

                        pOutput.accept(ModItems.ANTHRALITE_PICKAXE.get());
                        pOutput.accept(ModItems.ANTHRALITE_AXE.get());
                        pOutput.accept(ModItems.ANTHRALITE_SHOVEL.get());
                        pOutput.accept(ModItems.ANTHRALITE_HOE.get());
                        pOutput.accept(ModItems.ANTHRALITE_SWORD.get());
                        pOutput.accept(ModItems.ANTHRALITE_HELMET.get());
                        pOutput.accept(ModItems.ANTHRALITE_CHESTPLATE.get());
                        pOutput.accept(ModItems.ANTHRALITE_LEGGINGS.get());
                        pOutput.accept(ModItems.ANTHRALITE_BOOTS.get());

                        //CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SCORCHER.get());
                        pOutput.accept(ModItems.PEBBLES.get());
                        pOutput.accept(ModItems.SULFUR_CHUNK.get());
                        pOutput.accept(ModItems.NITER_DUST.get());
                        pOutput.accept(ModItems.GUNPOWDER_DUST.get());
                        pOutput.accept(ModItems.GRIT.get());
                        pOutput.accept(ModItems.RAW_ANTHRALITE.get());
                        pOutput.accept(ModItems.ANTHRALITE_INGOT.get());
                        pOutput.accept(ModItems.ANTHRALITE_NUGGET.get());
                        pOutput.accept(ModItems.ANCIENT_BRASS.get());
                        pOutput.accept(ModItems.TREATED_BRASS_BLEND.get());
                        pOutput.accept(ModItems.TREATED_BRASS_INGOT.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_BLEND.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_INGOT.get());
                        pOutput.accept(ModItems.NETHERITE_SCRAP_CHUNK.get());
                        pOutput.accept(ModItems.SCORCHED_INGOT.get());

                        pOutput.accept(ModItems.SMALL_CASING_MOLD.get());
                        pOutput.accept(ModItems.MEDIUM_CASING_MOLD.get());
                        pOutput.accept(ModItems.LARGE_CASING_MOLD.get());
                        pOutput.accept(ModItems.GUN_PARTS_MOLD.get());

                        pOutput.accept(ModItems.SMALL_COPPER_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_COPPER_CASING.get());
                        pOutput.accept(ModItems.SMALL_IRON_CASING.get());
                        pOutput.accept(ModItems.LARGE_IRON_CASING.get());
                        pOutput.accept(ModItems.SMALL_BRASS_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_BRASS_CASING.get());
                        pOutput.accept(ModItems.LARGE_BRASS_CASING.get());
                        pOutput.accept(ModItems.EMPTY_SHELL.get());

                        pOutput.accept(ModItems.POWDER_AND_BALL.get());
                        pOutput.accept(ModItems.GRAPESHOT.get());
                        pOutput.accept(ModItems.COMPACT_COPPER_ROUND.get());
                        pOutput.accept(ModItems.STANDARD_COPPER_ROUND.get());
                        pOutput.accept(ModItems.CASULL_ROUND.get());
                        pOutput.accept(ModItems.COMPACT_ADVANCED_ROUND.get());
                        pOutput.accept(ModItems.ADVANCED_ROUND.get());
                        pOutput.accept(ModItems.HEAVY_ROUND.get());
                        pOutput.accept(ModItems.BEOWULF_ROUND.get());
                        pOutput.accept(ModItems.SHOTGUN_SHELL.get());
                        pOutput.accept(ModItems.BEARPACK_SHELL.get());
                        pOutput.accept(ModItems.MICROJET.get());
                        pOutput.accept(ModItems.ROCKET.get());
                        pOutput.accept(ModItems.OSBORNE_SLUG.get());;


                        pOutput.accept(ModItems.PISTOL_AMMO_BOX.get());
                        pOutput.accept(ModItems.RIFLE_AMMO_BOX.get());
                        pOutput.accept(ModItems.SHOTGUN_AMMO_BOX.get());
                        pOutput.accept(ModItems.MAGNUM_AMMO_BOX.get());

                        pOutput.accept(ModItems.COPPER_GUN_FRAME.get());
                        pOutput.accept(ModItems.COPPER_GUN_PARTS.get());
                        pOutput.accept(ModItems.IRON_GUN_FRAME.get());
                        pOutput.accept(ModItems.IRON_GUN_PARTS.get());
                        pOutput.accept(ModItems.GUN_BARREL.get());
                        pOutput.accept(ModItems.GUN_GRIP.get());
                        pOutput.accept(ModItems.GUN_MAGAZINE.get());






                        pOutput.accept(ModItems.GRENADE.get());
                        pOutput.accept(ModItems.STUN_GRENADE.get());
                        pOutput.accept(ModItems.MOLOTOV_COCKTAIL.get());
                        pOutput.accept(ModItems.CHOKE_BOMB.get());

                        pOutput.accept(ModItems.REFLEX_SIGHT.get());
                        pOutput.accept(ModItems.MEDIUM_SCOPE.get());
                        pOutput.accept(ModItems.LONG_SCOPE.get());
                        pOutput.accept(ModItems.LIGHT_STOCK.get());
                        pOutput.accept(ModItems.WEIGHTED_STOCK.get());
                        pOutput.accept(ModItems.WOODEN_STOCK.get());
                        pOutput.accept(ModItems.SILENCER.get());
                        pOutput.accept(ModItems.ADVANCED_SILENCER.get());
                        pOutput.accept(ModItems.MUZZLE_BRAKE.get());
                        pOutput.accept(ModItems.LIGHT_GRIP.get());
                        pOutput.accept(ModItems.VERTICAL_GRIP.get());
                        pOutput.accept(ModItems.IRON_BAYONET.get());
                        pOutput.accept(ModItems.DIAMOND_BAYONET.get());
                        pOutput.accept(ModItems.NETHERITE_BAYONET.get());
                        pOutput.accept(ModItems.EXTENDED_MAG.get());
                        pOutput.accept(ModItems.SPEED_MAG.get());
                        pOutput.accept(ModItems.PLUS_P_MAG.get());


                        pOutput.accept(ModBlocks.ANTHRALITE_ORE.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_ANTHRALITE_ORE.get());
                        pOutput.accept(ModBlocks.ANTHRALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_ANTHRALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.NETHER_SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.SULFUR_BLOCK.get());
                        pOutput.accept(ModBlocks.NITER_BLOCK.get());
                        pOutput.accept(ModBlocks.GEOTHERMAL_VENT.get());
                        pOutput.accept(ModBlocks.VENT_COLLECTOR.get());
                        pOutput.accept(ModBlocks.GUN_BENCH.get());
                        pOutput.accept(ModBlocks.MACERATOR.get());
                        pOutput.accept(ModBlocks.MECHANICAL_PRESS.get());





                        pOutput.accept(ModItems.REPAIR_KIT.get());
                        pOutput.accept(ModItems.COG_KNIGHT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.COG_MINION_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SKY_CARRIER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SUPPLY_SCAMP_SPAWN_EGG.get());
                        pOutput.accept(ModItems.DISSIDENT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.REDCOAT_SPAWN_EGG.get());



                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
    public static class CreativeTabHelper {
        public static void addItemWithFullAmmo(CreativeModeTab.Output output, Item item) {
            if (item instanceof GunItem gunItem) {
                ItemStack stack = new ItemStack(gunItem);
                stack.getOrCreateTag().putInt("AmmoCount", gunItem.getGun().getReloads().getMaxAmmo());
                output.accept(stack);
            } else {
                output.accept(item);
            }
        }
    }

}