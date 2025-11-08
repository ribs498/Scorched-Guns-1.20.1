package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.GunItemStackRenderer;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.common.*;
import top.ribs.scguns.enchantment.EnchantmentTypes;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class GunItem extends Item implements IColored, IMeta {
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();
    private Gun gun = new Gun();

    public GunItem(Item.Properties properties) {
        super(properties);
    }

    public void setGun(NetworkGunManager.Supplier supplier) {
        this.gun = supplier.getGun();
    }

    public Gun getGun() {
        return this.gun;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        Gun modifiedGun = this.getModifiedGun(stack);

        ResourceLocation advantage = modifiedGun.getProjectile().getAdvantage();
        float baseDamage = modifiedGun.getProjectile().getDamage();
        baseDamage = GunModifierHelper.getModifiedProjectileDamage(stack, baseDamage);
        baseDamage = GunEnchantmentHelper.getAcceleratorDamage(stack, baseDamage);
        baseDamage = GunEnchantmentHelper.getHeavyShotDamage(stack, baseDamage);

        baseDamage *= Config.COMMON.gameplay.globalDamageMultiplier.get().floatValue();
        String additionalDamageText = "";
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound != null) {
            if (tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
                float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                additionalDamage += GunModifierHelper.getAdditionalDamage(stack, false);

                if (additionalDamage > 0) {
                    additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                } else if (additionalDamage < 0) {
                    additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                }
            }
        }

        tooltip.add(Component.translatable("info.scguns.damage")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(baseDamage) + additionalDamageText).withStyle(ChatFormatting.WHITE)));
        float baseArmorPen = modifiedGun.getProjectile().getArmorPen();
        float puncturingPen = GunEnchantmentHelper.getPuncturingArmorBypass(stack);
        float totalArmorPen = baseArmorPen + puncturingPen;

        if (totalArmorPen > 0) {
            String armorPenText = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(totalArmorPen);
            tooltip.add(Component.translatable("info.scguns.armor_penetration")
                    .append(": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(armorPenText).withStyle(ChatFormatting.YELLOW)));
        }

        if (!advantage.equals(ModTags.Entities.NONE.location())) {
            tooltip.add(Component.translatable("info.scguns.advantage").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("advantage." + advantage).withStyle(ChatFormatting.GOLD)));
        }
        String fireMode = modifiedGun.getGeneral().getFireMode().id().toString();
        tooltip.add(Component.translatable("info.scguns.fire_mode").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("fire_mode." + fireMode).withStyle(ChatFormatting.WHITE)));

        Item ammo = modifiedGun.getProjectile().getItem();
        Item reloadItem = modifiedGun.getReloads().getReloadItem();
        if (modifiedGun.getReloads().getReloadType() == ReloadType.SINGLE_ITEM) {
            ammo = reloadItem;
        }

        if (tagCompound != null) {
            if (tagCompound.getBoolean("IgnoreAmmo")) {
                tooltip.add(Component.translatable("info.scguns.ignore_ammo").withStyle(ChatFormatting.AQUA));
            } else {
                int ammoCount = tagCompound.getInt("AmmoCount");
                tooltip.add(Component.translatable("info.scguns.ammo")
                        .append(": ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(ammoCount + "/" + GunModifierHelper.getModifiedAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.WHITE)));
            }
        }
        float totalMeleeDamage = getTotalMeleeDamage(stack);
        if (totalMeleeDamage > 0) {
            String meleeDamageText = (totalMeleeDamage % 1.0 == 0) ? String.format("%d", (int) totalMeleeDamage) : String.format("%.1f", totalMeleeDamage);
            tooltip.add(Component.translatable("info.scguns.melee_damage")
                    .append(": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(meleeDamageText).withStyle(ChatFormatting.WHITE)));
        }
        ResourceLocation effectLocation = modifiedGun.getProjectile().getImpactEffect();
        if (effectLocation != null) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
            if (effect != null) {
                tooltip.add(Component.translatable("info.scguns.impact_effect").withStyle(ChatFormatting.GRAY)
                        .append(": ")
                        .append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.BLUE)));
            }
        }

        Gun.WeaponType weaponType = modifiedGun.getGeneral().getWeaponType();
        if (weaponType != null) {
            String weaponTypeKey = "desc.scguns." + weaponType.name().toLowerCase();
            tooltip.add(Component.translatable("info.scguns.weapon_type").withStyle(ChatFormatting.GRAY)
                    .append(": ")
                    .append(Component.translatable(weaponTypeKey).withStyle(ChatFormatting.AQUA)));
        }
        if (ammo != null) {
            tooltip.add(Component.translatable("info.scguns.ammo_type", Component.translatable(ammo.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
        }

        if (modifiedGun.getGeneral().allowsAmmoChange() && !modifiedGun.getGeneral().getAvailableAmmoTypes().isEmpty()) {
            if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                tooltip.add(Component.translatable("info.scguns.available_ammo_types").withStyle(ChatFormatting.GRAY));

                int currentIndex = modifiedGun.getGeneral().getCurrentAmmoTypeIndex();
                List<String> ammoTypes = modifiedGun.getGeneral().getAvailableAmmoTypes();

                for (int i = 0; i < ammoTypes.size(); i++) {
                    String ammoType = ammoTypes.get(i);
                    ResourceLocation ammoLocation = new ResourceLocation(
                            ammoType.contains(":") ? ammoType : top.ribs.scguns.Reference.MOD_ID + ":" + ammoType
                    );
                    Item ammoItem = ForgeRegistries.ITEMS.getValue(ammoLocation);

                    if (ammoItem != null) {
                        Component ammoName = Component.translatable(ammoItem.getDescriptionId());
                        if (i == currentIndex) {
                            tooltip.add(Component.literal("  • ").withStyle(ChatFormatting.YELLOW)
                                    .append(ammoName.copy().withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal(" ✓").withStyle(ChatFormatting.GREEN)));
                        } else {
                            tooltip.add(Component.literal("  • ").withStyle(ChatFormatting.GRAY)
                                    .append(ammoName.copy().withStyle(ChatFormatting.WHITE)));
                        }
                    }
                }
            } else {
                tooltip.add(Component.translatable("info.scguns.ammo_swap_available").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            }
        }
        tooltip.add(Component.translatable("info.scguns.attachment_help", KeyBinds.KEY_ATTACHMENTS.getTranslatedKeyMessage().getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.YELLOW));
    }



    public float getBayonetAdditionalDamage(ItemStack gunStack) {
        float additionalDamage = 0.0F;
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = Gun.getAttachment(type, gunStack);
            if (attachmentStack != null && attachmentStack.getItem() instanceof BayonetItem) {
                additionalDamage += ((BayonetItem) attachmentStack.getItem()).getAdditionalDamage();
            }
        }
        return additionalDamage;
    }

    public float getTotalMeleeDamage(ItemStack stack) {
        Gun gun = this.getModifiedGun(stack);
        float baseMeleeDamage = gun.getGeneral().getMeleeDamage();
        float bayonetDamage = getBayonetAdditionalDamage(stack);
        return baseMeleeDamage + bayonetDamage;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        return gun.getGeneral().getRate() * 4;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        stack.getOrCreateTag();
        this.getModifiedGun(stack);
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / (float) this.getMaxDamage(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (stack.getDamageValue() >= (stack.getMaxDamage() - 1)) {
            return 0x808080;
        }

        if (stack.getDamageValue() >= (stack.getMaxDamage() / 1.5)) {
            return Objects.requireNonNull(ChatFormatting.RED.getColor());
        }
        float stackMaxDamage = this.getMaxDamage(stack);
        float f = Math.max(0.0F, (stackMaxDamage - (float) stack.getDamageValue()) / stackMaxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public Gun getModifiedGun(ItemStack stack) {
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND)) {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item -> {
                if (tagCompound.getBoolean("Custom")) {
                    return Gun.create(tagCompound.getCompound("Gun"));
                } else {
                    Gun gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        return this.gun;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.category == EnchantmentTypes.SEMI_AUTO_GUN) {
            Gun modifiedGun = this.getModifiedGun(stack);
            return (modifiedGun.getGeneral().getFireMode() != FireMode.AUTOMATIC);
        }
        if (stack.is(ModTags.Items.MINING_GUN)) {
            if (enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.SILK_TOUCH) {
                return true;
            }
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return this.getMaxStackSize(stack) == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 13;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new GunItemStackRenderer();
            }
        });
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
        return pRepair.is(ModItems.REPAIR_KIT.get());
    }

    public ItemStack getAttachment(ItemStack heldItem, IAttachment.Type type) {
        return Gun.getAttachment(type, heldItem);
    }

    public boolean hasBayonet(ItemStack gunStack) {
        if (isBuiltInBayonetGun(gunStack)) {
            return true;
        }
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = Gun.getAttachment(type, gunStack);
            if (attachmentStack != null && attachmentStack.getItem() instanceof BayonetItem) {
                return true;
            }
        }
        return false;
    }
    public boolean isBuiltInBayonetGun(ItemStack gunStack) {
        return gunStack.is(ModTags.Items.BUILT_IN_BAYONET);
    }


    public boolean hasExtendedBarrel(ItemStack gunStack) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = Gun.getAttachment(type, gunStack);
            if (attachmentStack != null && attachmentStack.getItem() instanceof ExtendedBarrelItem) {
                return true;
            }
        }
        return false;
    }
    public boolean isOneHandedCarbineCandidate(ItemStack gunStack) {
        return gunStack.is(ModTags.Items.ONE_HANDED_CARBINE);
    }

    public boolean isOneHandedCarbineActive(ItemStack gunStack) {
        return isOneHandedCarbineCandidate(gunStack) &&
                !Gun.hasExtendedBarrel(gunStack) &&
                !Gun.hasStock(gunStack);
    }

    public void onAttachmentChanged(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("AttachmentChanged", true);
    }

    public int getBayonetBanzaiLevel(ItemStack gunStack) {
        for (IAttachment.Type type : IAttachment.Type.values()) {
            ItemStack attachmentStack = Gun.getAttachment(type, gunStack);
            if (attachmentStack != null && attachmentStack.getItem() instanceof BayonetItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(attachmentStack);
                if (enchantments.containsKey(ModEnchantments.BANZAI.get())) {
                    return enchantments.get(ModEnchantments.BANZAI.get());
                }
            }
        }
        return 0;
    }

    public Gun getGunProperties() {
        return this.gun;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        if (stack.is(ModTags.Items.MINING_GUN)) {
            return true;
        }
        return super.isBookEnchantable(stack, book);
    }

}