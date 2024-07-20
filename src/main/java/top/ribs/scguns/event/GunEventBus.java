package top.ribs.scguns.event;


import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.render.gun.model.RatKingAndQueenModel;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.*;
import top.ribs.scguns.item.DualWieldGunItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.NonUnderwaterGunItem;
import top.ribs.scguns.item.UnderwaterGunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEventBus {

    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event) {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();

        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            if ((heldItem.getItem() instanceof NonUnderwaterGunItem) && player.isUnderWater()) {
                event.setCanceled(true);
            }

            ItemCooldowns tracker = player.getCooldowns();
            if (tracker.isOnCooldown(heldItem.getItem()) && gun.getGeneral().getFireMode() == FireMode.PULSE) {
                event.setCanceled(true);
            }

            if (heldItem.isDamageableItem() && tag != null) {
                if (heldItem.getDamageValue() == (heldItem.getMaxDamage() - 1)) {
                    level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(), gun.getGeneral().getRate());
                    event.setCanceled(true);
                }
                // This is the Jam function
                int maxDamage = heldItem.getMaxDamage();
                int currentDamage = heldItem.getDamageValue();
                if (currentDamage >= maxDamage / 1.5) {
                    if (Math.random() >= 0.975) {
                        event.getEntity().playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
                        int coolDown = gun.getGeneral().getRate() * 10;
                        if (coolDown > 60) {
                            coolDown = 60;
                        }
                        event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(), coolDown);
                        event.setCanceled(true);
                    }
                } else if (tag.getInt("AmmoCount") >= 1) {
                    broken(heldItem, level, player);
                }
            }
        }
    }
    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event) {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();

        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);

            // Get shot count and determine mirroring
            int shotCount = RatKingAndQueenModel.GunFireEventRatHandler.getShotCount();
            boolean mirror = (heldItem.getItem() instanceof DualWieldGunItem && (shotCount % 2 == 1));

            // Eject casing particles
            if (gun.getProjectile().ejectsCasing() && tag != null) {
                if (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild) {
                    ejectCasing(level, player, mirror);
                }
            }

            // Casing retrieval logic
            if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild) {
                ItemStack casingStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(gun.getProjectile().casingType)));
                double baseChance = 0.2;
                int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHELL_CATCHER.get(), heldItem);
                double finalChance = baseChance + (enchantmentLevel * 0.15);
                if (Math.random() < finalChance) {
                    spawnCasingInWorld(level, player, casingStack);
                }
            }

            // Damage logic for the gun and attachments
            if (heldItem.isDamageableItem() && tag != null) {
                if (tag.getInt("AmmoCount") >= 1) {
                    damageGun(heldItem, level, player);
                    damageAttachments(heldItem, level, player);
                }
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() / 1.5)) {
                    level.playSound(player, player.blockPosition(), ModSounds.COPPER_GUN_JAM.get(), SoundSource.PLAYERS, 1.0F, 1.0f);
                }
            }
        }
    }

    public static void broken(ItemStack stack, Level level, Player player) {
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        if (currentDamage >= (maxDamage - 2)) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void damageGun(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamageValue();
                boolean isUnderwater = player.isUnderWater();
                boolean isUnderwaterGun = stack.getItem() instanceof UnderwaterGunItem;

                int damageAmount = 1;
                if (isUnderwater && !isUnderwaterGun) {
                    damageAmount = 2;
                }

                if (currentDamage >= (maxDamage - damageAmount)) {
                    if (currentDamage >= (maxDamage - damageAmount - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else {
                    stack.hurtAndBreak(damageAmount, player, null);
                }
            }
        }
    }



    public static void damageAttachments(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            if (stack.getItem() instanceof GunItem) {

                // Scope
                ItemStack scopeStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
                if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.SCOPE) && scopeStack.isDamageableItem()) {
                    int maxDamage = scopeStack.getMaxDamage();
                    int currentDamage = scopeStack.getDamageValue();
                    if (currentDamage == (maxDamage - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        Gun.removeAttachment(stack, "Scope");
                    } else {
                        scopeStack.hurtAndBreak(1, player, null);
                    }
                }

                // Barrel
                ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, stack);
                if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL) && barrelStack.isDamageableItem()) {
                    int maxDamage = barrelStack.getMaxDamage();
                    int currentDamage = barrelStack.getDamageValue();
                    if (currentDamage == (maxDamage - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        Gun.removeAttachment(stack, "Barrel");
                    } else {
                        barrelStack.hurtAndBreak(1, player, null);
                    }
                }

                // Stock
                ItemStack stockStack = Gun.getAttachment(IAttachment.Type.STOCK, stack);
                if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK) && stockStack.isDamageableItem()) {
                    int maxDamage = stockStack.getMaxDamage();
                    int currentDamage = stockStack.getDamageValue();
                    if (currentDamage == (maxDamage - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        Gun.removeAttachment(stack, "Stock");
                    } else {
                        stockStack.hurtAndBreak(1, player, null);
                    }
                }
                ///Magazine
                ItemStack magazineStack = Gun.getAttachment(IAttachment.Type.MAGAZINE, stack);
                if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE) && magazineStack.isDamageableItem()) {
                    int maxDamage = magazineStack.getMaxDamage();
                    int currentDamage = magazineStack.getDamageValue();
                    if (currentDamage == (maxDamage - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        Gun.removeAttachment(stack, "Magazine");
                    } else {
                        magazineStack.hurtAndBreak(1, player, null);
                    }
                }

                // Under Barrel
                ItemStack underBarrelStack = Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack);
                if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL) && underBarrelStack.isDamageableItem()) {
                    int maxDamage = underBarrelStack.getMaxDamage();
                    int currentDamage = underBarrelStack.getDamageValue();
                    if (currentDamage == (maxDamage - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        Gun.removeAttachment(stack, "Under_Barrel");
                    } else {
                        underBarrelStack.hurtAndBreak(1, player, null);
                    }
                }
            }
        }
    }

    public static void ejectCasing(Level level, LivingEntity livingEntity, boolean mirror) {
        Player playerEntity = (Player) livingEntity;
        ItemStack heldItem = playerEntity.getMainHandItem();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        Vec3 lookVec = playerEntity.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        // Adjust offset based on mirroring
        double offsetX = (mirror ? -rightVec.x : rightVec.x) * 0.5 + forwardVec.x * 0.5;
        double offsetY = playerEntity.getEyeHeight() - 0.4;
        double offsetZ = (mirror ? -rightVec.z : rightVec.z) * 0.5 + forwardVec.z * 0.5;

        Vec3 particlePos = playerEntity.getPosition(1).add(offsetX, offsetY, offsetZ);

        ResourceLocation compactCopperRound = ForgeRegistries.ITEMS.getKey(ModItems.COMPACT_COPPER_ROUND.get());
        ResourceLocation standardCopperRound = ForgeRegistries.ITEMS.getKey(ModItems.STANDARD_COPPER_ROUND.get());
        ResourceLocation ramrodRound = ForgeRegistries.ITEMS.getKey(ModItems.RAMROD_ROUND.get());
        ResourceLocation hogRound = ForgeRegistries.ITEMS.getKey(ModItems.HOG_ROUND.get());
        ResourceLocation compactAdvancedRound = ForgeRegistries.ITEMS.getKey(ModItems.COMPACT_ADVANCED_ROUND.get());
        ResourceLocation advancedRound = ForgeRegistries.ITEMS.getKey(ModItems.ADVANCED_ROUND.get());
        ResourceLocation heavyRound = ForgeRegistries.ITEMS.getKey(ModItems.KRAHG_ROUND.get());
        ResourceLocation energyCell = ForgeRegistries.ITEMS.getKey(ModItems.ENERGY_CELL.get());
        ResourceLocation sculkCell = ForgeRegistries.ITEMS.getKey(ModItems.SCULK_CELL.get());
        ResourceLocation blazeFuel = ForgeRegistries.ITEMS.getKey(ModItems.BLAZE_FUEL.get());
        ResourceLocation beowulfRound = ForgeRegistries.ITEMS.getKey(ModItems.BEOWULF_ROUND.get());
        ResourceLocation gibbsRound = ForgeRegistries.ITEMS.getKey(ModItems.GIBBS_ROUND.get());
        ResourceLocation shotgunShellLocation = ForgeRegistries.ITEMS.getKey(ModItems.SHOTGUN_SHELL.get());
        ResourceLocation bearpackShellLocation = ForgeRegistries.ITEMS.getKey(ModItems.BEARPACK_SHELL.get());
        ResourceLocation projectileLocation = ForgeRegistries.ITEMS.getKey(gun.getProjectile().getItem());

        SimpleParticleType casingType = ModParticleTypes.COPPER_CASING_PARTICLE.get();

        if (projectileLocation != null) {
            if (projectileLocation.equals(compactCopperRound) || projectileLocation.equals(standardCopperRound)) {
                casingType = ModParticleTypes.COPPER_CASING_PARTICLE.get();
            }
            if (projectileLocation.equals(hogRound) || projectileLocation.equals(ramrodRound)|| projectileLocation.equals(blazeFuel)|| projectileLocation.equals(sculkCell)) {
                casingType = ModParticleTypes.IRON_CASING_PARTICLE.get();
            } if (projectileLocation.equals(energyCell) || projectileLocation.equals(gibbsRound) ||projectileLocation.equals(beowulfRound)) {
                casingType = ModParticleTypes.DIAMOND_STEEL_CASING_PARTICLE.get();
            }else if (projectileLocation.equals(compactAdvancedRound) || projectileLocation.equals(advancedRound) || projectileLocation.equals(heavyRound)) {
                casingType = ModParticleTypes.BRASS_CASING_PARTICLE.get();
            } else if (projectileLocation.equals(shotgunShellLocation)) {
                casingType = ModParticleTypes.SHELL_PARTICLE.get();
            } else if (projectileLocation.equals(bearpackShellLocation)) {
                casingType = ModParticleTypes.BEARPACK_PARTICLE.get();
            }

        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(casingType,
                    particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }
    }



    private static void spawnCasingInWorld(Level level, Player player, ItemStack casingStack) {
        ItemEntity casingEntity = new ItemEntity(level, player.getX(), player.getY() + 1.5, player.getZ(), casingStack);
        casingEntity.setPickUpDelay(40);
        casingEntity.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(casingEntity);
    }
}
