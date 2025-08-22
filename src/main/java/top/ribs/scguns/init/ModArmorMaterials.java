package top.ribs.scguns.init;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;
import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {

    ADRIEN("adrien", 22, new int[]{ 3, 6, 6, 4 }, 8,
            SoundEvents.ARMOR_EQUIP_IRON, 0.5f, 0.1f, () -> Ingredient.of(ModItems.TREATED_IRON_INGOT.get())),
    ANTHRALITE("anthralite", 32, new int[]{ 2, 4, 3, 2 }, 12,
            SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 0.05f, () -> Ingredient.of(ModItems.ANTHRALITE_INGOT.get())),
    DIAMOND_STEEL("diamond_steel", 36, new int[]{ 3, 6, 5, 3 }, 16,
            SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0f, 0.05f, () -> Ingredient.of(ModItems.DIAMOND_STEEL_INGOT.get())),
    TREATED_BRASS("treated_brass", 30, new int[]{ 4, 6, 5, 4 }, 10,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.2f, () -> Ingredient.of(ModItems.TREATED_BRASS_INGOT.get())),
   ANCIENT_BRASS("ancient_brass", 16, new int[]{ 3, 5, 4, 3 }, 10,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0f, 0.15f, () -> Ingredient.of(ModItems.ANCIENT_BRASS.get())),
    EXO_SUIT("exo_suit", 200, new int[]{ 1, 1, 1, 1 }, 6,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 0.0f, 0.0f, () -> Ingredient.of(ModItems.TREATED_IRON_INGOT.get()));

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] BASE_DURABILITY = { 8, 12, 12, 9 };

    ModArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantmentValue, SoundEvent equipSound,
                      float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        return BASE_DURABILITY[pType.ordinal()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type pType) {
        return this.protectionAmounts[pType.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return Reference.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}