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
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.ribs.scguns.init.ModItems;

import java.util.UUID;

public class AdrienHelmItem extends Item {
    private final int defense;
    private final float toughness;
    private final int durability;
    private final int enchantability;
    private final float knockbackResistance;

    public AdrienHelmItem(Properties properties, int defense, float toughness, int durability, int enchantability, float knockbackResistance) {
        super(properties.defaultDurability(durability));
        this.defense = defense;
        this.toughness = toughness;
        this.durability = durability;
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
            if (!currentHelmet.isEmpty() && currentHelmet.getItem() instanceof ArmorItem) {
                ((ArmorItem) currentHelmet.getItem()).getEquipmentSlot();
            }
            player.setItemSlot(slot, heldStack.copy());
            heldStack.shrink(1);  // Reduce the stack size by 1
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
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            UUID uuid = UUID.randomUUID(); // You can use a specific UUID for consistency
            builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", this.defense, AttributeModifier.Operation.ADDITION));
            builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", this.toughness, AttributeModifier.Operation.ADDITION));
            if (this.knockbackResistance > 0.0F) {
                builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", this.knockbackResistance, AttributeModifier.Operation.ADDITION));
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
        return repair.is(Items.IRON_INGOT);
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }
    public static class AdrienHelmEventHandler {
        @SubscribeEvent
        public void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof AdrienHelmItem) {
                    helmet.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.HEAD));
                }
            }
        }

        public static void register() {
            MinecraftForge.EVENT_BUS.register(new AdrienHelmEventHandler());
        }
    }
}
