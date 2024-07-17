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
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.M3_CARABINE.get()))
                    .title(Component.translatable("creativetab.scorched_guns_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FLINTLOCK_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HANDCANNON.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BLUNDERBUSS.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.REPEATING_MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LASER_MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FLOUNDERGAT.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SCRAPPER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RUSTY_GNAT.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MAKESHIFT_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BOOMSTICK.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BRUISER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LLR_DIRECTOR.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DEFENDER_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GREASER_SMG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.M3_CARABINE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COMBAT_SHOTGUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.IRON_SPEAR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.AUVTOMAG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GYROJET_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ROCKET_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MARLIN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BOMB_LANCE.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.KRAUSER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.UPPERCUT.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PRUSH_GUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LOCKEWOOD.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.INERTIAL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MAS_55.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PLASGUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CYCLONE.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.M22_WALTZ.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.OSGOOD_50.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GAUSS_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COGLOADER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.JACKHAMMER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HOWLER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HOWLER_CONVERSION.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SPITFIRE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GATTALER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DOZIER_RL.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BLASPHEMY.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PYROCLASTIC_FLOW.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FREYR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.VULCANIC_REPEATER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SUPER_SHOTGUN.get());


                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RAYGUN.get());

                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RAT_KING_AND_QUEEN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LOCUST.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.NEWBORN_CYST.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.EARTHS_CORPSE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ASTELLA.get());

                        pOutput.accept(ModItems.ANTHRALITE_PICKAXE.get());
                        pOutput.accept(ModItems.ANTHRALITE_AXE.get());
                        pOutput.accept(ModItems.ANTHRALITE_SHOVEL.get());
                        pOutput.accept(ModItems.ANTHRALITE_HOE.get());
                        pOutput.accept(ModItems.ANTHRALITE_SWORD.get());
                        if (ModCompat.isFarmersDelightLoaded()) {
                            pOutput.accept(ModItems.ANTHRALITE_KNIFE.get());
                        }
                        pOutput.accept(ModItems.ANTHRALITE_HELMET.get());
                        pOutput.accept(ModItems.ANTHRALITE_CHESTPLATE.get());
                        pOutput.accept(ModItems.ANTHRALITE_LEGGINGS.get());
                        pOutput.accept(ModItems.ANTHRALITE_BOOTS.get());
                        pOutput.accept(ModItems.BRASS_MASK.get());
                        pOutput.accept(ModItems.REPAIR_KIT.get());


                        pOutput.accept(ModItems.PEBBLES.get());
                        pOutput.accept(ModItems.SULFUR_CHUNK.get());
                        pOutput.accept(ModItems.NITER_DUST.get());
                        pOutput.accept(ModItems.GUNPOWDER_DUST.get());
                        pOutput.accept(ModItems.SHEOL.get());
                        pOutput.accept(ModItems.SHEOL_DUST.get());
                        pOutput.accept(ModItems.BUCKSHOT.get());
                        pOutput.accept(ModItems.VEHEMENT_COAL.get());
                        pOutput.accept(ModItems.NITRO_POWDER.get());
                        pOutput.accept(ModItems.SCORCHED_BLEND.get());
                        pOutput.accept(ModItems.SCORCHED_INGOT.get());
                        pOutput.accept(ModItems.RAW_ANTHRALITE.get());
                        if (ModCompat.isCreateLoaded()) {
                            pOutput.accept(ModItems.CRUSHED_RAW_ANTHRALITE.get());
                        }
                        if (ModCompat.isIELoaded()) {
                            pOutput.accept(ModItems.ANTHRALITE_DUST.get());
                        }
                        pOutput.accept(ModItems.ANTHRALITE_INGOT.get());
                        pOutput.accept(ModItems.ANTHRALITE_NUGGET.get());
                        pOutput.accept(ModItems.ANCIENT_BRASS.get());
                        pOutput.accept(ModItems.TREATED_BRASS_BLEND.get());
                        pOutput.accept(ModItems.TREATED_BRASS_INGOT.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_BLEND.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_INGOT.get());
                        pOutput.accept(ModItems.NETHERITE_SCRAP_CHUNK.get());
                        pOutput.accept(ModItems.NETHER_STAR_FRAGMENT.get());

                        pOutput.accept(ModItems.BLANK_MOLD.get());
                        pOutput.accept(ModItems.SMALL_CASING_MOLD.get());
                        pOutput.accept(ModItems.MEDIUM_CASING_MOLD.get());
                        pOutput.accept(ModItems.LARGE_CASING_MOLD.get());
                        pOutput.accept(ModItems.GUN_PARTS_MOLD.get());
                        pOutput.accept(ModItems.COPPER_BLUEPRINT.get());
                        pOutput.accept(ModItems.IRON_BLUEPRINT.get());
                        pOutput.accept(ModItems.TREATED_BRASS_BLUEPRINT.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_BLUEPRINT.get());
                        pOutput.accept(ModItems.OCEAN_BLUEPRINT.get());
                        pOutput.accept(ModItems.PIGLIN_BLUEPRINT.get());
                        pOutput.accept(ModItems.DEEP_DARK_BLUEPRINT.get());
                        pOutput.accept(ModItems.END_BLUEPRINT.get());
                        pOutput.accept(ModItems.SCORCHED_BLUEPRINT.get());

                        pOutput.accept(ModItems.SMALL_COPPER_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_COPPER_CASING.get());
                        pOutput.accept(ModItems.SMALL_IRON_CASING.get());
                        pOutput.accept(ModItems.LARGE_IRON_CASING.get());
                        pOutput.accept(ModItems.SMALL_DIAMOND_STEEL_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_DIAMOND_STEEL_CASING.get());
                        pOutput.accept(ModItems.SMALL_BRASS_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_BRASS_CASING.get());
                        pOutput.accept(ModItems.LARGE_BRASS_CASING.get());

                        pOutput.accept(ModItems.POWDER_AND_BALL.get());
                        pOutput.accept(ModItems.GRAPESHOT.get());
                        pOutput.accept(ModItems.COMPACT_COPPER_ROUND.get());
                        pOutput.accept(ModItems.STANDARD_COPPER_ROUND.get());
                        pOutput.accept(ModItems.RAMROD_ROUND.get());
                        pOutput.accept(ModItems.HOG_ROUND.get());
                        pOutput.accept(ModItems.COMPACT_ADVANCED_ROUND.get());
                        pOutput.accept(ModItems.ADVANCED_ROUND.get());
                        pOutput.accept(ModItems.KRAHG_ROUND.get());
                        pOutput.accept(ModItems.BEOWULF_ROUND.get());
                        pOutput.accept(ModItems.GIBBS_ROUND.get());
                        pOutput.accept(ModItems.SHOTGUN_SHELL.get());
                        pOutput.accept(ModItems.BLAZE_FUEL.get());
                        pOutput.accept(ModItems.BEARPACK_SHELL.get());
                        pOutput.accept(ModItems.ENERGY_CELL.get());
                        pOutput.accept(ModItems.MICROJET.get());
                        pOutput.accept(ModItems.ROCKET.get());
                        //pOutput.accept(ModItems.OSBORNE_SLUG.get());;


                        pOutput.accept(ModItems.PISTOL_AMMO_BOX.get());
                        pOutput.accept(ModItems.RIFLE_AMMO_BOX.get());
                        pOutput.accept(ModItems.SHOTGUN_AMMO_BOX.get());
                        pOutput.accept(ModItems.MAGNUM_AMMO_BOX.get());
                        pOutput.accept(ModItems.ROCKET_AMMO_BOX.get());
                        pOutput.accept(ModItems.SPECIAL_AMMO_BOX.get());



                        pOutput.accept(ModItems.COPPER_GUN_FRAME.get());
                        pOutput.accept(ModItems.IRON_GUN_FRAME.get());
                        pOutput.accept(ModItems.TREATED_BRASS_GUN_FRAME.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_GUN_FRAME.get());
                        pOutput.accept(ModItems.SCORCHED_GUN_FRAME.get());
                        pOutput.accept(ModItems.GUN_PARTS.get());
                        pOutput.accept(ModItems.HEAVY_GUN_PARTS.get());
                        pOutput.accept(ModItems.FIRING_UNIT.get());
                        pOutput.accept(ModItems.RAPID_FIRING_UNIT.get());
                        pOutput.accept(ModItems.STONE_GUN_BARREL.get());
                        pOutput.accept(ModItems.GUN_BARREL.get());
                        pOutput.accept(ModItems.HEAVY_GUN_BARREL.get());
                        pOutput.accept(ModItems.GUN_GRIP.get());
                        pOutput.accept(ModItems.GUN_MAGAZINE.get());
                        pOutput.accept(ModItems.PLASMA_CORE.get());
                        pOutput.accept(ModItems.EMPTY_BLASPHEMY.get());

                        pOutput.accept(ModItems.GRENADE.get());
                        pOutput.accept(ModItems.STUN_GRENADE.get());
                        pOutput.accept(ModItems.MOLOTOV_COCKTAIL.get());
                        pOutput.accept(ModItems.CHOKE_BOMB.get());
                        pOutput.accept(ModItems.SWARM_BOMB.get());

                        pOutput.accept(ModItems.BASIC_POULTICE.get());
                        pOutput.accept(ModItems.HONEY_SULFUR_POULTICE.get());
                        pOutput.accept(ModItems.ENCHANTED_BANDAGE.get());
                        pOutput.accept(ModItems.DRAGON_SALVE.get());
                        pOutput.accept(ModItems.COLD_PACK.get());

                        pOutput.accept(ModItems.REFLEX_SIGHT.get());
                        pOutput.accept(ModItems.LASER_SIGHT.get());
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
                        pOutput.accept(ModItems.ANTHRALITE_BAYONET.get());
                        pOutput.accept(ModItems.DIAMOND_BAYONET.get());
                        pOutput.accept(ModItems.NETHERITE_BAYONET.get());
                        pOutput.accept(ModItems.EXTENDED_MAG.get());
                        pOutput.accept(ModItems.SPEED_MAG.get());
                       // pOutput.accept(ModItems.PLUS_P_MAG.get());


                        pOutput.accept(ModBlocks.ANTHRALITE_ORE.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_ANTHRALITE_ORE.get());
                        pOutput.accept(ModBlocks.ANTHRALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_ANTHRALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.NETHER_SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.VEHEMENT_COAL_ORE.get());
                        pOutput.accept(ModBlocks.SULFUR_BLOCK.get());
                        pOutput.accept(ModBlocks.ANCIENT_BRASS_BLOCK.get());
                        pOutput.accept(ModBlocks.NITER_BLOCK.get());
                        pOutput.accept(ModBlocks.NITER_GLASS.get());

                        pOutput.accept(ModBlocks.WHITE_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.LIGHT_GRAY_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.GRAY_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.BLACK_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.BROWN_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.RED_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.ORANGE_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.YELLOW_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.LIME_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.GREEN_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.CYAN_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.LIGHT_BLUE_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.BLUE_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.PURPLE_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.MAGENTA_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.PINK_NITER_GLASS.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICKS.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICK_STAIRS.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICK_SLAB.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICK_WALL.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_PILLAR.get());
                        pOutput.accept(ModBlocks.CRACKED_ASGHARIAN_BRICKS.get());
                        pOutput.accept(ModBlocks.SANDBAG.get());
                        pOutput.accept(ModBlocks.SUPPLY_CRATE.get());




                        pOutput.accept(ModBlocks.GEOTHERMAL_VENT.get());
                        pOutput.accept(ModBlocks.VENT_COLLECTOR.get());
                        pOutput.accept(ModBlocks.GUN_BENCH.get());
                        pOutput.accept(ModBlocks.MACERATOR.get());
                        pOutput.accept(ModBlocks.MECHANICAL_PRESS.get());






                        pOutput.accept(ModItems.COG_KNIGHT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.COG_MINION_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SKY_CARRIER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SUPPLY_SCAMP_SPAWN_EGG.get());
                        pOutput.accept(ModItems.DISSIDENT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.REDCOAT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.BLUNDERER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.HORNLIN_SPAWN_EGG.get());
                        pOutput.accept(ModItems.HIVE_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SWARM_SPAWN_EGG.get());



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