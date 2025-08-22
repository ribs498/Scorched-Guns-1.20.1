package top.ribs.scguns.event;


import com.simibubi.create.content.equipment.armor.BacktankUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.block.SulfurVentBlock;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.client.render.gun.model.RatKingAndQueenModel;
import top.ribs.scguns.common.*;
import top.ribs.scguns.init.*;
import top.ribs.scguns.interfaces.IAirGun;
import top.ribs.scguns.interfaces.IEnergyGun;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.EmptyCasingPouchItem;
import top.ribs.scguns.item.animated.AnimatedDualWieldGunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.animated.AnimatedUnderWaterGunItem;
import top.ribs.scguns.item.animated.NonUnderwaterAnimatedGunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static top.ribs.scguns.event.TemporaryLightManager.temporaryLights;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEventBus {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerLevel world : server.getAllLevels()) {
                    TemporaryLightManager.tickLights(world);
                    BeamHandlerCommon.BeamMiningManager.tickMiningProgress(world);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel world : server.getAllLevels()) {
            TemporaryLightManager.cleanup(world);
        }
    }
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            ChunkPos chunkPos = event.getChunk().getPos();
            Iterator<Map.Entry<Long, TemporaryLightManager.LightData>> iterator = temporaryLights.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, TemporaryLightManager.LightData> entry = iterator.next();
                BlockPos pos = BlockPos.of(entry.getKey());
                if (chunkPos.equals(new ChunkPos(pos))) {
                    TemporaryLightManager.LightData data = entry.getValue();
                    Level level = (Level) event.getLevel();
                    if (data.dimension == level.dimension()) {
                        if (level.hasChunkAt(pos)) {
                            BlockState currentState = level.getBlockState(pos);
                            if (currentState.is(Blocks.LIGHT)) {
                                level.setBlock(pos, data.previousState, 3);
                            }
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event) {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();

        if (MeleeAttackHandler.isBanzaiActive()) {
            MeleeAttackHandler.stopBanzai();
        }

        if (level.isClientSide() && heldItem.getItem() instanceof AnimatedGunItem animatedGunItem) {
            try {
                long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache()
                        .getManagerForId(id)
                        .getAnimationControllers()
                        .get("controller");

                if (animationController != null) {
                    // **ADD THIS DURABILITY CHECK BEFORE ANIMATION LOGIC**
                    if (heldItem.isDamageableItem() &&
                            heldItem.getDamageValue() >= (heldItem.getMaxDamage() - 1)) {
                        // Gun is broken - don't allow any shooting animations
                        event.setCanceled(true);
                        return;
                    }

                    if (animatedGunItem.isAnimationPlaying(animationController, "reload_stop")) {
                        event.setCanceled(true);
                        return;
                    }

                    if (tag != null && tag.getBoolean("scguns:IsReloading")) {
                        if (animatedGunItem.isAnimationPlaying(animationController, "reload_loop") ||
                                animatedGunItem.isAnimationPlaying(animationController, "reload_start")) {
                            tag.putBoolean("scguns:ReloadComplete", true);
                            animationController.tryTriggerAnimation("reload_stop");
                            tag.remove("scguns:IsReloading");
                            ModSyncedDataKeys.RELOADING.setValue(player, false);
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                        }
                        event.setCanceled(true);
                        return;
                    }
                }
            } catch (Exception e) {
                ScorchedGuns.LOGGER.error("Error in preShoot animation handling: " + e.getMessage());
            }
        }
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            GripType gripType = gun.determineGripType(heldItem);

            if (player.isUsingItem() && player.getOffhandItem().getItem() == Items.SHIELD
                    && (gripType == GripType.ONE_HANDED || gripType == GripType.ONE_HANDED_2)) {
                event.setCanceled(true);
                return;
            }
            if (heldItem.getTag() != null && tag != null && tag.contains("DrawnTick") &&
                    tag.getInt("DrawnTick") < 15) {
                event.setCanceled(true);
                return;
            }

            int energyUse = gun.getGeneral().getEnergyUse();
            if (!player.isCreative()) {
                if (heldItem.getItem() instanceof IEnergyGun) {
                    IEnergyStorage energyStorage = heldItem.getCapability(ForgeCapabilities.ENERGY)
                            .orElseThrow(IllegalStateException::new);
                    if (energyStorage.getEnergyStored() >= energyUse) {
                        energyStorage.extractEnergy(energyUse, false);
                    } else {
                        player.displayClientMessage(Component.translatable("message.energy_gun.no_energy")
                                .withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }
                }

                if (ScorchedGuns.createLoaded && heldItem.getItem() instanceof IAirGun) {
                    List<ItemStack> backtanks = BacktankUtil.getAllWithAir(player);
                    if (backtanks.isEmpty()) {
                        player.displayClientMessage(Component.translatable("message.airgun.no_air")
                                .withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }
                    float airCostPerShot = calculateAirCostPerShot(gun);
                    BacktankUtil.consumeAir(player, backtanks.get(0), airCostPerShot);
                    if (!BacktankUtil.hasAirRemaining(backtanks.get(0))) {
                        player.displayClientMessage(Component.translatable("message.airgun.no_air")
                                .withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            // Underwater checks
            if ((heldItem.getItem() instanceof NonUnderwaterGunItem ||
                    heldItem.getItem() instanceof NonUnderwaterAnimatedGunItem) &&
                    player.isUnderWater()) {
                event.setCanceled(true);
                return;
            }

            // Damage checks
            if (heldItem.isDamageableItem() && tag != null) {
                if (heldItem.getDamageValue() == (heldItem.getMaxDamage() - 1)) {
                    level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK,
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                    event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(),
                            gun.getGeneral().getRate());
                    event.setCanceled(true);
                    return;
                }

                int maxDamage = heldItem.getMaxDamage();
                int currentDamage = heldItem.getDamageValue();
                if (currentDamage >= maxDamage / 1.5) {
                    if (Math.random() >= 0.975) {
                        event.getEntity().playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
                        int coolDown = gun.getGeneral().getRate() * 10;
                        if (coolDown > 30) {
                            coolDown = 30;
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
        CompoundTag tag = heldItem.getOrCreateTag();

        if (heldItem.getItem() instanceof AnimatedGunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            if (gun.getGeneral().isRevolver()) {
                ((AnimatedGunItem)heldItem.getItem()).getRotationHandler().incrementCylinderRotation(30.0f);
            }
            if (heldItem.getItem().toString().contains("cogloader")) {
                ((AnimatedGunItem)heldItem.getItem()).getRotationHandler().incrementMagazineRotation(15.0f);
            }
            if (heldItem.getItem().toString().contains("scrapper")) {
                float maxAmmo = Gun.getMaxAmmo(heldItem);
                float currentAmmo = Gun.getAmmoCount(heldItem);
                float slidePosition = Math.min((maxAmmo - currentAmmo) / maxAmmo, 1.0f);
                tag.putFloat("MagazinePosition", slidePosition);
            }
            long id = GeoItem.getId(heldItem);
            AnimationController<GeoAnimatable> controller = gunItem.getAnimatableInstanceCache()
                    .getManagerForId(id)
                    .getAnimationControllers()
                    .get("controller");

            controller.forceAnimationReset();
            boolean isCarbine = gunItem.isInCarbineMode(heldItem);

            if (gunItem instanceof AnimatedDualWieldGunItem) {
                RatKingAndQueenModel.GunFireEventRatHandler.incrementShotCount();
                boolean useAlternate = RatKingAndQueenModel.GunFireEventRatHandler.shouldUseAlternateAnimation();

                if (ModSyncedDataKeys.AIMING.getValue(player)) {
                    controller.tryTriggerAnimation(useAlternate ? "aim_shoot1" : "aim_shoot");
                } else {
                    controller.tryTriggerAnimation(useAlternate ? "shoot1" : "shoot");
                }
            } else {
                if (ModSyncedDataKeys.AIMING.getValue(player)) {
                    controller.tryTriggerAnimation(isCarbine ? "carbine_aim_shoot" : "aim_shoot");
                } else {
                    controller.tryTriggerAnimation(isCarbine ? "carbine_shoot" : "shoot");
                }
            }
        }
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            int hotBarrelLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.get(), heldItem);
            if (gun.getGeneral().hasPlayerKnockBack()) {
                applyGunKnockback(player, gun);
            }
            if (hotBarrelLevel > 0) {
                int hotBarrelFillRate = gun.getGeneral().getHotBarrelRate();
                HotBarrelHandler.increaseHotBarrel(heldItem, hotBarrelFillRate);

                boolean inSulfurCloud = isInSulfurCloudArea(player.level(), player.blockPosition());
                if (inSulfurCloud) {
                    triggerExplosion(player.level(), player.blockPosition());
                }
            }
            Vec3 lookVec = player.getLookAngle();
            BlockPos lightPos = player.blockPosition()
                    .offset((int)(lookVec.x * 2), 2, (int)(lookVec.z * 2));
            boolean isBeamWeapon = gun.getGeneral().getFireMode() == FireMode.BEAM;
            TemporaryLightManager.addTemporaryLight(level, lightPos, isBeamWeapon);
            int shotCount = RatKingAndQueenModel.GunFireEventRatHandler.getShotCount();
            boolean mirror = (heldItem.getItem() instanceof AnimatedDualWieldGunItem && (shotCount % 2 == 1));

            if (Config.COMMON.gameplay.spawnCasings.get()) {
                if (gun.getProjectile().ejectsCasing() && !gun.getProjectile().ejectDuringReload()) {
                    if (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild) {
                        ejectCasing(level, player, mirror);
                    }
                }
                if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild && !gun.getProjectile().ejectDuringReload()) {
                    ItemStack casingStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(gun.getProjectile().casingType)));
                    double baseChance = 0.4;
                    int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHELL_CATCHER.get(), heldItem);
                    double finalChance = baseChance + (enchantmentLevel * 0.15);
                    if (Math.random() < finalChance) {
                        if (addCasingToPouch(player, casingStack)) {
                            spawnCasingInWorld(level, player, casingStack);
                        }
                    }
                }
            }

            if (heldItem.isDamageableItem()) {
                if (Config.COMMON.gameplay.enableGunDamage.get()) {
                    damageGun(heldItem, level, player);
                }
                if (Config.COMMON.gameplay.enableAttachmentDamage.get()) {
                    damageAttachments(heldItem, level, player);
                }
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() / 1.5) && Math.random() < 0.15) {
                    level.playSound(player, player.blockPosition(), ModSounds.COPPER_GUN_JAM.get(), SoundSource.PLAYERS, 1.0F, 1.0f);
                }
            }
        }
    }
    private static void applyGunKnockback(Player player, Gun gun) {
        Vec3 lookVec = player.getLookAngle();
        float baseStrength = gun.getGeneral().getPlayerKnockBackStrength();
        float totalKnockbackResistance = 0.0F;
        totalKnockbackResistance += (float) player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.5f;

        for (ItemStack armorPiece : player.getArmorSlots()) {
            if (armorPiece.getItem() instanceof ArmorItem armor) {
                if (armor.getMaterial() instanceof ArmorMaterial) {
                    ArmorMaterial material = armor.getMaterial();
                    totalKnockbackResistance += material.getKnockbackResistance() * 0.25f;
                }
            }
        }
        totalKnockbackResistance = Math.min(0.75F, totalKnockbackResistance);
        float effectiveStrength = baseStrength * (1.0F - totalKnockbackResistance);
        if (effectiveStrength > 0) {
            double verticalBoost;
            if (lookVec.y < -0.5 && !player.onGround() && player.getDeltaMovement().y > 0) {
                verticalBoost = effectiveStrength * 1.25;
            } else {
                verticalBoost = 0.1 * effectiveStrength;
            }

            player.setDeltaMovement(player.getDeltaMovement().add(
                    -lookVec.x * effectiveStrength,
                    verticalBoost,
                    -lookVec.z * effectiveStrength
            ));

            if (verticalBoost > 0.5) {
                player.fallDistance = 0;
            }

            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.get(), heldItem) > 0) {
            HotBarrelHandler.decayHotBarrel(heldItem);
        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (itemStack.getItem() instanceof GunItem && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL.get(), itemStack) > 0) {
                    HotBarrelHandler.clearHotBarrel(itemStack);
                }
            }
        }
    }


    private static boolean isInSulfurCloudArea(Level level, BlockPos playerPos) {
        int effectRadiusSquared = SulfurVentBlock.EFFECT_RADIUS_SQUARED;

        for (BlockPos checkPos : BlockPos.betweenClosed(playerPos.offset(-SulfurVentBlock.EFFECT_RADIUS, -1, -SulfurVentBlock.EFFECT_RADIUS), playerPos.offset(SulfurVentBlock.EFFECT_RADIUS, 1, SulfurVentBlock.EFFECT_RADIUS))) {
            BlockState state = level.getBlockState(checkPos);

            if (state.getBlock() instanceof SulfurVentBlock && state.getValue(SulfurVentBlock.ACTIVE)) {
                double distanceSquared = checkPos.distSqr(playerPos);
                if (distanceSquared <= effectRadiusSquared) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void triggerExplosion(Level level, BlockPos pos) {
        RandomSource random = level.random;
        for (int i = 0; i < 7; i++) { // Trigger multiple explosions for effect
            double xOffset = (random.nextDouble() - 0.5) * 2.0 * SulfurVentBlock.EFFECT_RADIUS;
            double yOffset = (random.nextDouble() - 0.5) * 2.0 * SulfurVentBlock.EFFECT_RADIUS;
            double zOffset = (random.nextDouble() - 0.5) * 2.0 * SulfurVentBlock.EFFECT_RADIUS;
            BlockPos explosionPos = pos.offset((int) xOffset, (int) yOffset, (int) zOffset);

            level.explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
        }
    }
    private static float calculateAirCostPerShot(Gun gun) {
        return gun.getGeneral().getEnergyUse();
    }

    public static void broken(ItemStack stack, Level level, Player player) {
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        if (currentDamage >= (maxDamage - 2)) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
    public static void damageGun(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild && Config.COMMON.gameplay.enableGunDamage.get()) {
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamageValue();
                boolean isUnderwater = player.isUnderWater();
                boolean isUnderwaterGun = stack.getItem() instanceof UnderwaterGunItem
                        || stack.getItem() instanceof AnimatedUnderWaterGunItem;


                int damageAmount = 1;
                int waterProofLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WATER_PROOF.get(), stack);

                if (isUnderwater) {
                    if (isUnderwaterGun) {
                        if (waterProofLevel > 0) {
                            damageAmount = (Math.random() < 0.5) ? 1 : 0;
                        }
                    } else {
                        damageAmount = (waterProofLevel > 0) ? 1 : 3;
                    }
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
        if (!player.getAbilities().instabuild && Config.COMMON.gameplay.enableAttachmentDamage.get()) {
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
        if (!level.isClientSide()) return;

        Player playerEntity = (Player) livingEntity;
        ItemStack heldItem = playerEntity.getMainHandItem();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        Vec3 lookVec = playerEntity.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        double offsetX = (mirror ? -rightVec.x : rightVec.x) * 0.5 + forwardVec.x * 0.5;
        double offsetY = playerEntity.getEyeHeight() - 0.4;
        double offsetZ = (mirror ? -rightVec.z : rightVec.z) * 0.5 + forwardVec.z * 0.5;

        Vec3 particlePos = playerEntity.getPosition(1).add(offsetX, offsetY, offsetZ);
        ResourceLocation particleLocation = gun.getProjectile().getCasingParticle();

        if (particleLocation != null) {
            ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(particleLocation);
            if (particleType instanceof SimpleParticleType simpleParticleType) {
                level.addParticle(simpleParticleType,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);
            }
        }
    }


    public static void spawnCasingInWorld(Level level, Player player, ItemStack casingStack) {
        ItemEntity casingEntity = new ItemEntity(level, player.getX(), player.getY() + 1.5, player.getZ(), casingStack);
        casingEntity.setPickUpDelay(40);
        casingEntity.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(casingEntity);
    }

    public static boolean addCasingToPouch(Player player, ItemStack casingStack) {
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof EmptyCasingPouchItem) {
                int insertedItems = EmptyCasingPouchItem.add(itemStack, casingStack);
                if (insertedItems > 0) {
                    casingStack.shrink(insertedItems);
                    return false;
                }
            }
        }

        // Check Curios slots for the empty casing pouch
        AtomicBoolean result = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof EmptyCasingPouchItem) {
                    int insertedItems = EmptyCasingPouchItem.add(stack, casingStack);
                    if (insertedItems > 0) {
                        casingStack.shrink(insertedItems);
                        result.set(true);
                        break;
                    }
                }
            }
        });

        return !result.get();
    }
    public static class RatKingAndQueenModel {
        public static class GunFireEventRatHandler {
            private static int shotCount = 0;

            public static int getShotCount() {
                return shotCount;
            }

            public static void incrementShotCount() {
                shotCount++;
            }

            public static boolean shouldUseAlternateAnimation() {
                return shotCount % 2 == 1;
            }
        }
    }
}
