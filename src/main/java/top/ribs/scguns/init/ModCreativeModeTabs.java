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
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.item.EnergyGunItem;
import top.ribs.scguns.item.GunItem;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SCORCHED_GUNS_TAB = CREATIVE_MODE_TABS.register("scorched_guns_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.M3_CARABINE.get()))
                    .title(Component.translatable("creativetab.scorched_guns_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        ///ANTIQUE
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FLINTLOCK_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HANDCANNON.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BLUNDERBUSS.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DOUBLET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.REPEATING_MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PAX.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.WINNIE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CALLWELL_CONVERSION.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CALLWELL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CALLWELL_TERMINAL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SAKETINI.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SAKETINI_IRONPORT.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BIG_BORE.get());
                        CreativeTabHelper.addGunOrEnergyWeaponWithFullResources(pOutput, ModItems.LASER_MUSKET.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PLASMABUSS.get());

                        /// COPPER
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SCRAPPER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RUSTY_GNAT.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.UMAX_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MAKESHIFT_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BOOMSTICK.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BRUISER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LLR_DIRECTOR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BIRDFEEDER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ARC_WORKER.get());

                        //IRON
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DEFENDER_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.TRENCHUR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GREASER_SMG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.M3_CARABINE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.M3_MARKSMAN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COMBAT_SHOTGUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.VENTURI.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.IRON_JAVELIN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.IRON_SPEAR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.AUVTOMAG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PULSAR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GYROJET_PISTOL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BRAWLER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CRUSADER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MK43_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ROCKET_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ULTRA_KNIGHT_HAWK.get());

                        //OCEAN
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MARLIN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BOMB_LANCE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HULLBREAKER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SEQUOIA.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FLOUNDERGAT.get());

                        //WRECKER
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MOKOVA.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MAK_MKII.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.STILETTO.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.TURNPIKE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.KILLER_23.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HOMEMAKER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.KALASKAH.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BASKER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.TL_RUNNER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.STIGG.get());



                        /// DIAMOND STEEL
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.KRAUSER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SOUL_DRUMMER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.VALORA.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.UPPERCUT.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MICINA.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PRUSH_GUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LOCKEWOOD.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RG_JIGSAW.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.INERTIAL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MAS_55.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PLASGUN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CYCLONE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SHARD_CULLER.get());


                        /// TREATED BRASS
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.M22_WALTZ.get());
                        CreativeTabHelper.addGunOrEnergyWeaponWithFullResources(pOutput, ModItems.WALTZ_CONVERSION.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.OSGOOD_50.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GRANDLE_OG.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GRANDLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.COGLOADER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GALE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.JACKHAMMER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HOWLER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.HOWLER_CONVERSION.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GAUSS_RIFLE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.NIAMI.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SPITFIRE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.GATTALER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.THUNDERHEAD.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SCRATCHES.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CR4K_MINING_LASER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DOZIER_RL.get());


                        //PIGLIN
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.BLASPHEMY.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PYROCLASTIC_FLOW.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FREYR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.MANGALITSA.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.VULCANIC_REPEATER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.TROTTERS.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SUPER_SHOTGUN.get());

                        //DEEP
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.WHISPERS.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.ECHOES_2.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SCULK_RESONATOR.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FORLORN_HOPE.get());

                        //END
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.CARAPICE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.SHELLURKER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.WEEVIL.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.DARK_MATTER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LONE_WONDER.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RAYGUN.get());


                        //SCORCHED
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.PRIMA_MATERIA.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.RAT_KING_AND_QUEEN.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.LOCUST.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.NEWBORN_CYST.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.EARTHS_CORPSE.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.FLAYED_GOD.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.NERVEPINCH.get());
                        CreativeTabHelper.addItemWithFullAmmo(pOutput, ModItems.TERRA_INCOGNITA.get());
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
                        pOutput.accept(ModItems.ANTHRALITE_RESPIRATOR.get());
                        pOutput.accept(ModItems.ANTHRALITE_CHESTPLATE.get());
                        pOutput.accept(ModItems.ANTHRALITE_LEGGINGS.get());
                        pOutput.accept(ModItems.ANTHRALITE_BOOTS.get());
                        pOutput.accept(ModItems.RIDGETOP.get());
                        pOutput.accept(ModItems.ADRIEN_HELM.get());
                        pOutput.accept(ModItems.ADRIEN_CHESTPLATE.get());
                        pOutput.accept(ModItems.ADRIEN_LEGGINGS.get());
                        pOutput.accept(ModItems.ADRIEN_BOOTS.get());
                        pOutput.accept(ModItems.BRASS_MASK.get());
                        pOutput.accept(ModItems.COG_KNIGHT_HELMET.get());
                        pOutput.accept(ModItems.COG_KNIGHT_CHESTPLATE.get());
                        pOutput.accept(ModItems.COG_KNIGHT_LEGGINGS.get());
                        pOutput.accept(ModItems.COG_KNIGHT_BOOTS.get());
                        pOutput.accept(ModItems.EXO_SUIT_HELMET.get());
                        pOutput.accept(ModItems.EXO_SUIT_CHESTPLATE.get());
                        pOutput.accept(ModItems.EXO_SUIT_LEGGINGS.get());
                        pOutput.accept(ModItems.EXO_SUIT_BOOTS.get());
                        pOutput.accept(ModItems.NETHERITE_RESPIRATOR.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_HELMET.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_CHESTPLATE.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_LEGGINGS.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_BOOTS.get());
                        pOutput.accept(ModItems.TREATED_BRASS_HELMET.get());
                        pOutput.accept(ModItems.TREATED_BRASS_CHESTPLATE.get());
                        pOutput.accept(ModItems.TREATED_BRASS_LEGGINGS.get());
                        pOutput.accept(ModItems.TREATED_BRASS_BOOTS.get());
                        pOutput.accept(ModItems.REPAIR_KIT.get());
                        pOutput.accept(ModItems.COMPOSITE_FILTER.get());
                        pOutput.accept(ModItems.RANGE_FINDER.get());

                        pOutput.accept(ModItems.EXO_SUIT_CORE.get());
                        pOutput.accept(ModItems.ADVANCED_EXO_SUIT_CORE.get());

                        pOutput.accept(ModItems.NIGHT_VISION_MODULE.get());
                        pOutput.accept(ModItems.TARGET_TRACKER_MODULE.get());
                        pOutput.accept(ModItems.GAS_MASK_MODULE.get());
                        pOutput.accept(ModItems.REBREATHER_MODULE.get());
                        pOutput.accept(ModItems.JETPACK_MODULE.get());
                        pOutput.accept(ModItems.RABBIT_MODULE.get());
                        pOutput.accept(ModItems.SUIT_GREASE.get());
                        pOutput.accept(ModItems.TENSION_SPRING.get());
                        pOutput.accept(ModItems.SHOCK_ABSORBER.get());

                        pOutput.accept(ModItems.ARMOR_PLATE.get());
                        pOutput.accept(ModItems.PAULDRON.get());
                        pOutput.accept(ModItems.HEAVY_ARMOR_PLATE.get());
                        pOutput.accept(ModItems.HEAVY_PAULDRON.get());
                        pOutput.accept(ModItems.ARMOR_POUCHES.get());
                        pOutput.accept(ModItems.HEAVY_ARMOR_POUCHES.get());

                        pOutput.accept(ModItems.PEBBLES.get());
                        pOutput.accept(ModItems.SULFUR_CHUNK.get());
                        pOutput.accept(ModItems.SULFUR_DUST.get());
                        pOutput.accept(ModItems.RAW_PHOSPHOR.get());
                        pOutput.accept(ModItems.PHOSPHOR_DUST.get());
                        pOutput.accept(ModItems.NITER_DUST.get());
                        pOutput.accept(ModItems.GUNPOWDER_DUST.get());
                        pOutput.accept(ModItems.AUREOUS_SLAG.get());
                        pOutput.accept(ModItems.SHEOL.get());
                        pOutput.accept(ModItems.SHEOL_DUST.get());
                        pOutput.accept(ModItems.PEAL.get());
                        pOutput.accept(ModItems.PEAL_DUST.get());
                        pOutput.accept(ModItems.BUCKSHOT.get());
                        pOutput.accept(ModItems.FLECHETTE.get());
                        pOutput.accept(ModItems.NITRO_BUCKSHOT.get());
                        pOutput.accept(ModItems.VEHEMENT_COAL.get());
                        pOutput.accept(ModItems.NITRO_POWDER.get());
                        pOutput.accept(ModItems.NITRO_POWDER_DUST.get());
                        pOutput.accept(ModItems.PLASMA.get());
                        pOutput.accept(ModItems.PLASMA_NUGGET.get());
                        pOutput.accept(ModItems.SCORCHED_BLEND.get());
                        pOutput.accept(ModItems.SCORCHED_INGOT.get());
                        pOutput.accept(ModItems.RAW_ANTHRALITE.get());
                        if (ModCompat.isCreateLoaded()) {
                            pOutput.accept(ModItems.CRUSHED_RAW_ANTHRALITE.get());
                        }
                        if (ModCompat.isIELoaded() || ModCompat.isMekanismLoaded()) {
                            pOutput.accept(ModItems.ANTHRALITE_DUST.get());
                        }
                        if (ModCompat.isMekanismLoaded()) {
                            pOutput.accept(ModItems.DIRTY_DUST_ANTHRALITE.get());
                            pOutput.accept(ModItems.CLUMP_ANTHRALITE.get());
                            pOutput.accept(ModItems.SHARD_ANTHRALITE.get());
                        }
                        pOutput.accept(ModItems.ANTHRALITE_INGOT.get());
                        pOutput.accept(ModItems.ANTHRALITE_NUGGET.get());
                        pOutput.accept(ModItems.ANCIENT_BRASS.get());
                        pOutput.accept(ModItems.TREATED_IRON_BLEND.get());
                        pOutput.accept(ModItems.TREATED_IRON_INGOT.get());
                        pOutput.accept(ModItems.TREATED_IRON_NUGGET.get());
                        pOutput.accept(ModItems.TREATED_BRASS_BLEND.get());
                        pOutput.accept(ModItems.TREATED_BRASS_INGOT.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_BLEND.get());
                        pOutput.accept(ModItems.DEPLETED_DIAMOND_STEEL_INGOT.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_INGOT.get());
                        pOutput.accept(ModItems.NETHERITE_SCRAP_CHUNK.get());
                        pOutput.accept(ModItems.NETHER_STAR_FRAGMENT.get());
                        pOutput.accept(ModItems.STANDARD_BULLET.get());
                        pOutput.accept(ModItems.ADVANCED_BULLET.get());
                        pOutput.accept(ModItems.BLANK_MOLD.get());
                        pOutput.accept(ModItems.SMALL_CASING_MOLD.get());
                        pOutput.accept(ModItems.MEDIUM_CASING_MOLD.get());
                        pOutput.accept(ModItems.LARGE_CASING_MOLD.get());
                        pOutput.accept(ModItems.BULLET_MOLD.get());
                        pOutput.accept(ModItems.GUN_PARTS_MOLD.get());
                        pOutput.accept(ModItems.DISC_MOLD.get());
                        pOutput.accept(ModItems.ANTIQUE_BLUEPRINT.get());
                        pOutput.accept(ModItems.FRONTIER_BLUEPRINT.get());
                        pOutput.accept(ModItems.COPPER_BLUEPRINT.get());
                        pOutput.accept(ModItems.IRON_BLUEPRINT.get());
                        pOutput.accept(ModItems.WRECKER_BLUEPRINT.get());
                        pOutput.accept(ModItems.TREATED_BRASS_BLUEPRINT.get());
                        pOutput.accept(ModItems.DIAMOND_STEEL_BLUEPRINT.get());
                        pOutput.accept(ModItems.OCEAN_BLUEPRINT.get());
                        pOutput.accept(ModItems.PIGLIN_BLUEPRINT.get());
                        pOutput.accept(ModItems.DEEP_DARK_BLUEPRINT.get());
                        pOutput.accept(ModItems.END_BLUEPRINT.get());
                        pOutput.accept(ModItems.SCORCHED_BLUEPRINT.get());
                        pOutput.accept(ModItems.EXO_SUIT_BLUEPRINT.get());

                        pOutput.accept(ModItems.SMALL_COPPER_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_COPPER_CASING.get());
                        pOutput.accept(ModItems.SMALL_IRON_CASING.get());
                        pOutput.accept(ModItems.LARGE_IRON_CASING.get());
                        pOutput.accept(ModItems.EMPTY_CELL.get());
                        pOutput.accept(ModItems.SMALL_DIAMOND_STEEL_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_DIAMOND_STEEL_CASING.get());
                        pOutput.accept(ModItems.SMALL_BRASS_CASING.get());
                        pOutput.accept(ModItems.MEDIUM_BRASS_CASING.get());
                        pOutput.accept(ModItems.LARGE_BRASS_CASING.get());
                        pOutput.accept(ModItems.SHULKER_CASING.get());

                        pOutput.accept(ModItems.POWDER_AND_BALL.get());
                        pOutput.accept(ModItems.GRAPESHOT.get());
                        pOutput.accept(ModItems.COMPACT_COPPER_ROUND.get());
                        pOutput.accept(ModItems.STANDARD_COPPER_ROUND.get());
                        pOutput.accept(ModItems.RAMROD_ROUND.get());
                        pOutput.accept(ModItems.HOG_ROUND.get());
                        pOutput.accept(ModItems.COMPACT_ADVANCED_ROUND.get());
                        pOutput.accept(ModItems.ADVANCED_ROUND.get());
                        pOutput.accept(ModItems.SHATTER_ROUND.get());
                        pOutput.accept(ModItems.KRAHG_ROUND.get());
                        pOutput.accept(ModItems.BEOWULF_ROUND.get());
                        pOutput.accept(ModItems.GIBBS_ROUND.get());
                        pOutput.accept(ModItems.SHOTGUN_SHELL.get());
                        pOutput.accept(ModItems.BLAZE_FUEL.get());
                        pOutput.accept(ModItems.BEARPACK_SHELL.get());
                        pOutput.accept(ModItems.SHOCK_CELL.get());
                        pOutput.accept(ModItems.ENERGY_CELL.get());
                        pOutput.accept(ModItems.SCULK_CELL.get());
                        pOutput.accept(ModItems.SHULKSHOT.get());
                        pOutput.accept(ModItems.SYRINGE.get());
                        pOutput.accept(ModItems.MICROJET.get());
                        pOutput.accept(ModItems.ROCKET.get());
                        pOutput.accept(ModItems.OSBORNE_SLUG.get());

//                        pOutput.accept(ModItems.UNFINISHED_COMPACT_COPPER_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_STANDARD_COPPER_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_RAMROD_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_HOG_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_COMPACT_ADVANCED_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_ADVANCED_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_KRAHG_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_BEOWULF_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_GIBBS_ROUND.get());
//                        pOutput.accept(ModItems.UNFINISHED_SHOTGUN_SHELL.get());
//                        pOutput.accept(ModItems.UNFINISHED_BEARPACK_SHELL.get());
//                        pOutput.accept(ModItems.UNFINISHED_ENERGY_CELL.get());
//                        pOutput.accept(ModItems.UNFINISHED_SCULK_CELL.get());
//                        pOutput.accept(ModItems.UNFINISHED_SHULKSHOT.get());
//                        pOutput.accept(ModItems.UNFINISHED_MICROJET.get());
//                        pOutput.accept(ModItems.UNFINISHED_ROCKET.get());
//                        pOutput.accept(ModItems.UNFINISHED_BLAZE_FUEL.get());



                        pOutput.accept(ModItems.PISTOL_AMMO_BOX.get());
                        pOutput.accept(ModItems.RIFLE_AMMO_BOX.get());
                        pOutput.accept(ModItems.SHOTGUN_AMMO_BOX.get());
                        pOutput.accept(ModItems.MAGNUM_AMMO_BOX.get());
                        pOutput.accept(ModItems.ENERGY_AMMO_BOX.get());
                        pOutput.accept(ModItems.ROCKET_AMMO_BOX.get());
                        pOutput.accept(ModItems.SPECIAL_AMMO_BOX.get());
                        pOutput.accept(ModItems.CREATIVE_AMMO_BOX.get());
                        pOutput.accept(ModItems.EMPTY_CASING_POUCH.get());
                        pOutput.accept(ModItems.DISHES_POUCH.get());
                        pOutput.accept(ModItems.ROCK_POUCH.get());



                        pOutput.accept(ModItems.COPPER_GUN_FRAME.get());
                        pOutput.accept(ModItems.IRON_GUN_FRAME.get());
                        pOutput.accept(ModItems.TREATED_IRON_GUN_FRAME.get());
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
                        pOutput.accept(ModItems.CHARGED_AMETHYST_SHARD.get());
                        pOutput.accept(ModItems.EMPTY_TANK.get());
                        pOutput.accept(ModItems.EMPTY_CORE.get());
                        pOutput.accept(ModItems.ENERGY_CORE.get());
                        pOutput.accept(ModItems.DEPLETED_ENERGY_CORE.get());
                        pOutput.accept(ModItems.PLASMA_CORE.get());
                        pOutput.accept(ModItems.EMPTY_BLASPHEMY.get());
                        pOutput.accept(ModItems.COPPER_DISC.get());

                        pOutput.accept(ModItems.GRENADE.get());
                        pOutput.accept(ModItems.STUN_GRENADE.get());
                        pOutput.accept(ModItems.SHOTBALL.get());
                        pOutput.accept(ModItems.MOLOTOV_COCKTAIL.get());
                        pOutput.accept(ModItems.CHOKE_BOMB.get());
                        pOutput.accept(ModItems.SWARM_BOMB.get());
                        pOutput.accept(ModItems.NAIL_BOMB.get());
                        pOutput.accept(ModItems.GAS_GRENADE.get());
                        pOutput.accept(ModItems.BEACON_GRENADE.get());

                        pOutput.accept(ModItems.COLD_PACK.get());
                        pOutput.accept(ModItems.BASIC_POULTICE.get());
                        pOutput.accept(ModItems.HONEY_SULFUR_POULTICE.get());
                        pOutput.accept(ModItems.ENCHANTED_BANDAGE.get());
                        pOutput.accept(ModItems.DRAGON_SALVE.get());


                        pOutput.accept(ModItems.REFLEX_SIGHT.get());
                        pOutput.accept(ModItems.LASER_SIGHT.get());
                        pOutput.accept(ModItems.MEDIUM_SCOPE.get());
                        pOutput.accept(ModItems.LONG_SCOPE.get());
                        pOutput.accept(ModItems.LIGHT_STOCK.get());
                        pOutput.accept(ModItems.WEIGHTED_STOCK.get());
                        pOutput.accept(ModItems.WOODEN_STOCK.get());
                        pOutput.accept(ModItems.SILENCER.get());
                        pOutput.accept(ModItems.ADVANCED_SILENCER.get());
                        pOutput.accept(ModItems.EXTENDED_BARREL.get());
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
                        pOutput.accept(ModItems.MASS_PRODUCTION_MUSIC_DISC.get());
                        pOutput.accept(ModItems.MASS_DESTRUCTION_MUSIC_DISC.get());
                        pOutput.accept(ModItems.MASS_DESTRUCTION_EXTENDED_MUSIC_DISC.get());
                        pOutput.accept(ModItems.TEAM_LOG.get());
                        pOutput.accept(ModItems.ENEMY_LOG.get());
                        pOutput.accept(ModItems.SCAMP_PACKAGE.get());
                        pOutput.accept(ModItems.COG_LOCATOR.get());
                        pOutput.accept(ModItems.THE_PACT.get());


                        pOutput.accept(ModBlocks.ANTHRALITE_ORE.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_ANTHRALITE_ORE.get());
                        pOutput.accept(ModBlocks.ANTHRALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_ANTHRALITE_BLOCK.get());
                        pOutput.accept(ModBlocks.SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.DEEPSLATE_SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.NETHER_SULFUR_ORE.get());
                        pOutput.accept(ModBlocks.VEHEMENT_COAL_ORE.get());

                        pOutput.accept(ModBlocks.RICH_PHOSPHORITE.get());
                        pOutput.accept(ModBlocks.RAW_PHOSPHOR_BLOCK.get());
                        pOutput.accept(ModBlocks.SULFUR_BLOCK.get());
                        pOutput.accept(ModBlocks.ANCIENT_BRASS_BLOCK.get());
                        pOutput.accept(ModBlocks.TREATED_IRON_BLOCK.get());
                        pOutput.accept(ModBlocks.TREATED_BRASS_BLOCK.get());
                        pOutput.accept(ModBlocks.DIAMOND_STEEL_BLOCK.get());
                        pOutput.accept(ModBlocks.VEHEMENT_COAL_BLOCK.get());
                        pOutput.accept(ModBlocks.PLASMA_BLOCK.get());
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
                        pOutput.accept(ModBlocks.PHOSPHORITE.get());
                        pOutput.accept(ModBlocks.SMOOTH_PHOSPHORITE.get());
                        pOutput.accept(ModBlocks.POLISHED_PHOSPHORITE.get());
                        pOutput.accept(ModBlocks.PHOSPHORITE_BRICKS.get());
                        pOutput.accept(ModBlocks.CRACKED_PHOSPHORITE_BRICKS.get());
                        pOutput.accept(ModBlocks.PHOSPHORITE_BRICK_STAIRS.get());
                        pOutput.accept(ModBlocks.PHOSPHORITE_BRICK_SLAB.get());
                        pOutput.accept(ModBlocks.PHOSPHORITE_BRICK_WALL.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICKS.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICK_STAIRS.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICK_SLAB.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_BRICK_WALL.get());
                        pOutput.accept(ModBlocks.ASGHARIAN_PILLAR.get());
                        pOutput.accept(ModBlocks.CRACKED_ASGHARIAN_BRICKS.get());
                        pOutput.accept(ModBlocks.TREATED_BRASS_PLATES.get());
                        pOutput.accept(ModBlocks.CUT_TREATED_BRASS.get());
                        pOutput.accept(ModBlocks.TREATED_IRON_GRATE.get());
                        pOutput.accept(ModBlocks.SANDBAG.get());
                        pOutput.accept(ModBlocks.SUPPLY_CRATE.get());
                        pOutput.accept(ModBlocks.POWDER_KEG.get());
                        pOutput.accept(ModBlocks.NITRO_KEG.get());
                        pOutput.accept(ModBlocks.PENETRATOR.get());


                        pOutput.accept(ModBlocks.ADVANCED_COMPOSTER.get());
                        pOutput.accept(ModBlocks.GEOTHERMAL_VENT.get());
                        pOutput.accept(ModBlocks.SULFUR_VENT.get());
                        pOutput.accept(ModBlocks.VENT_COLLECTOR.get());
                        pOutput.accept(ModBlocks.CRYONITER.get());
                        pOutput.accept(ModBlocks.THERMOLITH.get());
                        pOutput.accept(ModBlocks.POLAR_GENERATOR.get());
                        pOutput.accept(ModBlocks.LIGHTNING_BATTERY.get());
                        pOutput.accept(ModBlocks.LIGHTNING_ROD_CONNECTOR.get());
                        pOutput.accept(ModBlocks.GUN_BENCH.get());
                        pOutput.accept(ModBlocks.MACERATOR.get());
                        pOutput.accept(ModBlocks.MECHANICAL_PRESS.get());
                        pOutput.accept(ModBlocks.POWERED_MACERATOR.get());
                        pOutput.accept(ModBlocks.POWERED_MECHANICAL_PRESS.get());
                        pOutput.accept(ModBlocks.GUN_SHELF.get());
                        pOutput.accept(ModBlocks.AMMO_BOX.get());
                        pOutput.accept(ModBlocks.PLASMA_LANTERN.get());
                        pOutput.accept(ModBlocks.CHARGED_AMETHYST_RELAY.get());
                        pOutput.accept(ModBlocks.BASIC_TURRET.get());
                        pOutput.accept(ModBlocks.AUTO_TURRET.get());
                        pOutput.accept(ModBlocks.SHOTGUN_TURRET.get());
                        pOutput.accept(ModBlocks.HOSTILE_TURRET_TARGETING_BLOCK.get());
                        pOutput.accept(ModBlocks.PLAYER_TURRET_TARGETING_BLOCK.get());
                        pOutput.accept(ModBlocks.TURRET_TARGETING_BLOCK.get());
                        pOutput.accept(ModBlocks.FIRE_RATE_TURRET_MODULE.get());
                        pOutput.accept(ModBlocks.DAMAGE_TURRET_MODULE.get());
                        pOutput.accept(ModBlocks.RANGE_TURRET_MODULE.get());
                        pOutput.accept(ModBlocks.SHELL_CATCHER_TURRET_MODULE.get());
                        pOutput.accept(ModBlocks.AMMO_TURRET_MODULE.get());

                        pOutput.accept(ModBlocks.ENEMY_TURRET.get());
                        pOutput.accept(ModBlocks.MOB_TRAP.get());
                        pOutput.accept(ModBlocks.FELIX_MEMORIAL.get());



                        pOutput.accept(ModItems.COG_KNIGHT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.TRAUMA_UNIT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.COG_MINION_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SKY_CARRIER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SUPPLY_SCAMP_SPAWN_EGG.get());
                        pOutput.accept(ModItems.DISSIDENT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.REDCOAT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.BLUNDERER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.HORNLIN_SPAWN_EGG.get());
                        pOutput.accept(ModItems.ZOMBIFIED_HORNLIN_SPAWN_EGG.get());
                        pOutput.accept(ModItems.HIVE_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SWARM_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SIGNAL_BEACON_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SCAMP_TANK_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SCAMPLER_SPAWN_EGG.get());
                       // pOutput.accept(ModItems.THE_MERCHANT_SPAWN_EGG.get());
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
        public static void addGunOrEnergyWeaponWithFullResources(CreativeModeTab.Output output, Item item) {
            ItemStack stack = new ItemStack(item);

            if (item instanceof GunItem gunItem) {
                // Set full ammo
                stack.getOrCreateTag().putInt("AmmoCount", gunItem.getGun().getReloads().getMaxAmmo());
            }

            if (item instanceof EnergyGunItem energyGunItem) {
                // Set full energy
                stack.getOrCreateTag().putInt("Energy", energyGunItem.getMaxEnergyStored(stack));
            }

            output.accept(stack);
        }
    }


}