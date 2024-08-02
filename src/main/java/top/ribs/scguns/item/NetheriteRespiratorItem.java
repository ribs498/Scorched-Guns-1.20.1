package top.ribs.scguns.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.init.ModItems;

import java.util.UUID;

public class NetheriteRespiratorItem extends Item {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("6e59a150-8e12-16eb-8dcd-0242ac136703");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_UUID = UUID.fromString("6e59a156-8e12-11eb-8dcd-0272ac130003");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_UUID = UUID.fromString("6e59a122-8e12-10eb-7dcd-0242ac130003");

    private final int defense;
    private final float toughness;
    private final int enchantability;
    private final float knockbackResistance;

    public NetheriteRespiratorItem(Properties properties, int defense, float toughness, int durability, int enchantability, float knockbackResistance) {
        super(properties.defaultDurability(durability));
        this.defense = defense;
        this.toughness = toughness;
        this.enchantability = enchantability;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        EquipmentSlot slot = getEquipmentSlotForItem(heldStack);
        if (slot != null) {
            ItemStack currentHelmet = player.getItemBySlot(slot);
            if (!currentHelmet.isEmpty()) {
                player.getAttributes().removeAttributeModifiers(currentHelmet.getAttributeModifiers(slot));
            }
            player.setItemSlot(slot, heldStack.copy());
            player.getAttributes().addTransientAttributeModifiers(heldStack.getAttributeModifiers(slot));
            if (!currentHelmet.isEmpty()) {
                if (!player.getInventory().add(currentHelmet)) {
                    player.drop(currentHelmet, false);
                }
            }
            heldStack.shrink(1);

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, heldStack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, heldStack);
    }




    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    private static EquipmentSlot getEquipmentSlotForItem(ItemStack stack) {
        return stack.getItem().canEquip(stack, EquipmentSlot.HEAD, null) ? EquipmentSlot.HEAD : null;
    }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category == EnchantmentCategory.ARMOR_HEAD || super.canApplyAtEnchantingTable(stack, enchantment);
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIER_UUID, "Armor modifier", this.defense, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ARMOR_TOUGHNESS_MODIFIER_UUID, "Armor toughness", this.toughness, AttributeModifier.Operation.ADDITION));
            if (this.knockbackResistance > 0.0F) {
                builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(KNOCKBACK_RESISTANCE_MODIFIER_UUID, "Armor knockback resistance", this.knockbackResistance, AttributeModifier.Operation.ADDITION));
            }
            return builder.build();
        }
        return super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.NETHERITE_INGOT);
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    public int getDefense() {
        return defense;
    }

    public static class NetheriteRespiratorEventHandler {
        @SubscribeEvent
        public void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof NetheriteRespiratorItem) {
                    helmet.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.HEAD));
                }
            }
        }

        public static void register() {
            MinecraftForge.EVENT_BUS.register(new NetheriteRespiratorEventHandler());
        }
    }
}

