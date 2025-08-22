package top.ribs.scguns.common.exosuit;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.item.animated.ExoSuitItem;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ExoSuitFlightHandler {

    private static final float FLIGHT_UPWARD_SPEED = 0.8f;
    private static final float FLIGHT_DESCENT_SPEED = 0.4f;
    private static final float NATURAL_SINK_SPEED = 0.05f;

    private static final float HORIZONTAL_ACCELERATION = 0.003f;
    private static final float HORIZONTAL_DRAG = 0.90f;
    private static final float MAX_HORIZONTAL_SPEED = 0.12f;
    private static final float STOP_THRESHOLD = 0.01f;

    private static final double JETPACK_OFFSET_DISTANCE = -0.5;
    private static final double JETPACK_HEIGHT_OFFSET = 0.7;
    private static final double JETPACK_SIDE_OFFSET = 0.4;
    private static final int PARTICLE_COUNT = 1;

    private static boolean wasGamePaused = false;

    private static boolean wasJumpPressed = false;
    private static long lastJumpPressTime = 0;
    private static final long DOUBLE_TAP_WINDOW = 300;
    private static boolean jetpackActive = false;
    private static long lastEnergyConsumptionTime = 0;
    private static final long ENERGY_CONSUMPTION_INTERVAL = 1000;
    private static long lastLoopSoundTime = 0;
    private static final long LOOP_SOUND_DURATION = 3000;
    private static boolean isLoopSoundPlaying = false;
    private static boolean wasThrustingLastTick = false;

    private static boolean consumeJetpackEnergy(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            return false;
        }
        ItemStack jetpackUpgrade = findJetpackModule(chestplate);
        if (jetpackUpgrade.isEmpty()) {
            return false;
        }
        return ExoSuitPowerManager.consumeEnergyForUpgrade(player, "utility", jetpackUpgrade);
    }
    private static ItemStack findJetpackModule(ItemStack chestplate) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack upgradeItem = ExoSuitData.getUpgradeInSlot(chestplate, slot);
            if (!upgradeItem.isEmpty()) {
                ExoSuitUpgrade upgrade = ExoSuitUpgradeManager.getUpgradeForItem(upgradeItem);
                if (upgrade != null && upgrade.getType().equals("utility") && upgrade.getEffects().hasFlight()) {
                    return upgradeItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.getAbilities().flying && !player.isCreative() && !player.isSpectator() && isUsingJetpack(player)) {
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }
    }

    private static boolean isUsingJetpack(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            return false;
        }

        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);
        boolean hasFlightCapability = totalEffects.hasFlight();
        boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");
        boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");

        return hasFlightCapability && utilityEnabled && canFunction;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.level().isClientSide) return;

        boolean isGamePaused = mc.isPaused();
        if (wasGamePaused && !isGamePaused) {
            wasGamePaused = false;
            handleJetpackFlightWithoutParticles(player);
            return;
        }
        wasGamePaused = isGamePaused;

        if (isGamePaused) {
            return;
        }

        handleJetpackFlight(player);
    }


    private static void handleJetpackFlightWithoutParticles(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            if (player.getAbilities().flying && !player.isCreative()) {
                disableFlight(player);
            }
            return;
        }

        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);

        boolean hasFlightCapability = totalEffects.hasFlight();
        boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");
        boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");

        if (!hasFlightCapability || !utilityEnabled || !canFunction) {
            if (player.getAbilities().flying && !player.isCreative()) {
                disableFlight(player);
                jetpackActive = false;
            }
            return;
        }

        float flightSpeed = totalEffects.getFlightSpeed();
        if (flightSpeed <= 0) {
            flightSpeed = 0.1f;
        }

        if (!player.getAbilities().mayfly) {
            enableFlight(player, flightSpeed);
        }

        Minecraft mc = Minecraft.getInstance();
        if (jetpackActive && player.getAbilities().mayfly) {
            if (player.getAbilities().flying && !player.isCreative()) {
                handleFlightMovement(player, mc, flightSpeed);
            }
        }
    }

    private static void enableFlight(Player player, float flightSpeed) {
        player.getAbilities().mayfly = true;
        player.getAbilities().setFlyingSpeed(flightSpeed);
        player.onUpdateAbilities();
    }

    private static void disableFlight(Player player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getAbilities().setFlyingSpeed(0.05f);
        player.onUpdateAbilities();
        jetpackActive = false;

        if (player.level().isClientSide) {
            stopJetpackSounds(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleFlightMovement(Player player, Minecraft mc, float flightSpeed) {
        if (!player.getAbilities().flying || !jetpackActive || player.isSpectator()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEnergyConsumptionTime >= ENERGY_CONSUMPTION_INTERVAL) {
            if (!consumeJetpackEnergy(player)) {
                disableFlight(player);
                jetpackActive = false;
                stopJetpackSounds(player);

                player.level().playLocalSound(
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        0.5f, 0.6f, false
                );
                return;
            }
            lastEnergyConsumptionTime = currentTime;
        }

        Vec3 currentVelocity = player.getDeltaMovement();
        double newY;
        boolean isThrusting = false;

        if (mc.options.keyJump.isDown()) {
            newY = FLIGHT_UPWARD_SPEED * flightSpeed;
            isThrusting = true;
        }
        else if (mc.options.keyShift.isDown()) {
            newY = -FLIGHT_DESCENT_SPEED * flightSpeed;
        }
        else {
            newY = -NATURAL_SINK_SPEED;
        }

        handleJetpackSounds(player, isThrusting);

        double newX = currentVelocity.x;
        double newZ = currentVelocity.z;

        Vec3 inputVector = getMovementInput(player, mc);

        if (inputVector.lengthSqr() > 0) {
            double maxSpeedForThisTick = MAX_HORIZONTAL_SPEED * Math.min(flightSpeed, 1.5f);

            newX += inputVector.x * HORIZONTAL_ACCELERATION;
            newZ += inputVector.z * HORIZONTAL_ACCELERATION;

            double horizontalSpeed = Math.sqrt(newX * newX + newZ * newZ);
            if (horizontalSpeed > maxSpeedForThisTick) {
                double ratio = maxSpeedForThisTick / horizontalSpeed;
                newX *= ratio;
                newZ *= ratio;
            }
        } else {
            newX *= HORIZONTAL_DRAG;
            newZ *= HORIZONTAL_DRAG;

            if (Math.abs(newX) < STOP_THRESHOLD) newX = 0;
            if (Math.abs(newZ) < STOP_THRESHOLD) newZ = 0;
        }

        player.setDeltaMovement(newX, newY, newZ);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleJetpackSounds(Player player, boolean isThrusting) {
        long currentTime = System.currentTimeMillis();

        if (!isLoopSoundPlaying || (currentTime - lastLoopSoundTime) >= LOOP_SOUND_DURATION) {
            player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.JETPACK_LOOP.get(),
                    SoundSource.PLAYERS,
                    0.5f,
                    1.0f,
                    false
            );
            lastLoopSoundTime = currentTime;
            isLoopSoundPlaying = true;
        }
        if (isThrusting && !wasThrustingLastTick) {
            player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.JETPACK.get(),
                    SoundSource.PLAYERS,
                    0.7f,
                    1.0f + (player.level().random.nextFloat() * 0.1f - 0.05f),
                    false
            );
        }

        wasThrustingLastTick = isThrusting;
    }
    private static void handleJetpackFlight(Player player) {
        if (player.isSpectator()) {
            return;
        }
        ItemStack chestplate = player.getInventory().getArmor(2);

        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            if (player.getAbilities().flying && !player.isCreative()) {
                disableFlight(player);
            }
            return;
        }

        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);

        boolean hasFlightCapability = totalEffects.hasFlight();
        boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");
        boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");

        if (!hasFlightCapability || !utilityEnabled || !canFunction) {
            if (player.getAbilities().flying && !player.isCreative()) {
                disableFlight(player);
                jetpackActive = false;
            }
            return;
        }

        float flightSpeed = totalEffects.getFlightSpeed();
        if (flightSpeed <= 0) {
            flightSpeed = 0.1f;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean jumpPressed = mc.options.keyJump.isDown();
        long currentTime = System.currentTimeMillis();

        if (jumpPressed && !wasJumpPressed) {
            long timeSinceLastPress = currentTime - lastJumpPressTime;

            if (timeSinceLastPress <= DOUBLE_TAP_WINDOW) {
                jetpackActive = !jetpackActive;

                if (jetpackActive) {
                    enableFlight(player, flightSpeed);
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                } else {
                    disableFlight(player);
                }
            }

            lastJumpPressTime = currentTime;
        }

        wasJumpPressed = jumpPressed;

        if (jetpackActive && player.getAbilities().mayfly) {
            if (player.getAbilities().flying && !player.isCreative()) {
                handleFlightMovement(player, mc, flightSpeed);
                spawnJetpackParticles(player, mc);
                Minecraft mcInstance = Minecraft.getInstance();
                boolean isThrusting = mcInstance.options.keyJump.isDown();
                handleJetpackSounds(player, isThrusting);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void stopJetpackSounds(Player player) {
        isLoopSoundPlaying = false;
        wasThrustingLastTick = false;
        lastLoopSoundTime = 0;
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnJetpackParticles(Player player, Minecraft mc) {
        Level level = player.level();

        float yaw = player.getYRot() * (float) (Math.PI / 180.0);

        double backwardX = -Math.sin(yaw);
        double backwardZ = Math.cos(yaw);

        double sideX = -backwardZ;

        double baseX = player.getX() + (backwardX * JETPACK_OFFSET_DISTANCE);
        double baseY = player.getY() + JETPACK_HEIGHT_OFFSET;
        double baseZ = player.getZ() + (backwardZ * JETPACK_OFFSET_DISTANCE);

        double leftJetX = baseX + (sideX * JETPACK_SIDE_OFFSET);
        double leftJetZ = baseZ + (backwardX * JETPACK_SIDE_OFFSET);

        double rightJetX = baseX - (sideX * JETPACK_SIDE_OFFSET);
        double rightJetZ = baseZ - (backwardX * JETPACK_SIDE_OFFSET);

        spawnJetParticles(level, leftJetX, baseY, leftJetZ, player);
        spawnJetParticles(level, rightJetX, baseY, rightJetZ, player);
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnJetParticles(Level level, double x, double y, double z, Player player) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.2;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.1;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.2;

            double velocityX = (level.random.nextDouble() - 0.5) * 0.1;
            double velocityY = -0.1 - (level.random.nextDouble() * 0.2);
            double velocityZ = (level.random.nextDouble() - 0.5) * 0.1;

            level.addParticle(ParticleTypes.FLAME,
                    x + offsetX, y + offsetY, z + offsetZ,
                    velocityX, velocityY, velocityZ);

            if (level.random.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.SMOKE,
                        x + offsetX, y + offsetY - 0.1, z + offsetZ,
                        velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
            }
        }
    }

    private static Vec3 getMovementInput(Player player, Minecraft mc) {
        float forward = 0;
        float strafe = 0;

        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;

        if (forward != 0 && strafe != 0) {
            forward *= 0.707f;
            strafe *= 0.707f;
        }

        if (forward == 0 && strafe == 0) {
            return Vec3.ZERO;
        }

        float yaw = player.getYRot() * (float) (Math.PI / 180.0);
        double x = strafe * Math.cos(yaw) - forward * Math.sin(yaw);
        double z = forward * Math.cos(yaw) + strafe * Math.sin(yaw);

        return new Vec3(x, 0, z);
    }
}