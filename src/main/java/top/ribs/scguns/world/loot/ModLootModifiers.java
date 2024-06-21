package top.ribs.scguns.world.loot;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SCRAP_SIMPLE_DUNGEON =
            LOOT_MODIFIER_SERIALIZERS.register("scrap_on_simple_dungeon", ScrapOnSimpleDungeonModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> TECH_TRASH_SIMPLE_DUNGEON =
            LOOT_MODIFIER_SERIALIZERS.register("tech_trash_on_simple_dungeon", TechTrashOnSimpleDungeonModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ATLANTEAN_SPEAR_SHIPWRECK_TREASURE =
            LOOT_MODIFIER_SERIALIZERS.register("atlantean_spear_on_shipwreck_treasure", AtlanteanSpearOnShipwreckTreasureModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> TYPHOONE_ELDER_GUARDIAN =
            LOOT_MODIFIER_SERIALIZERS.register("typhoonee_on_elder_guardian", TyphooneeOnElderGuardianModifier.CODEC);


    public static void register(IEventBus bus) {
        LOOT_MODIFIER_SERIALIZERS.register(bus);
    }
}