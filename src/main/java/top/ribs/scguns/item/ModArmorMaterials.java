package top.ribs.scguns.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;
import java.util.function.Supplier;

public enum ModArmorMaterials implements ArmorMaterial {
        ANTHRALITE("anthralite", 32, new int[]{ 4, 5, 5, 4 }, 10,
                SoundEvents.ARMOR_EQUIP_GOLD, 0.5f, 0f, () -> Ingredient.of(ModItems.ANTHRALITE_INGOT.get()));
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