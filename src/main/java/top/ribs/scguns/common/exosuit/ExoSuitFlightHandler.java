package top.ribs.scguns.common.exosuit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.item.animated.ExoSuitItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageJetpackState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ExoSuitFlightHandler {

    private static final float FLIGHT_UPWARD_SPEED = 0.8f;
    private static final float FLIGHT_DESCENT_SPEED = 0.4f;
    private static final float NATURAL_SINK_SPEED = 0.02f;

    private static final float HORIZONTAL_ACCELERATION = 0.001f;
    private static final float HORIZONTAL_DRAG = 0.85f;
    private static final float MAX_HORIZONTAL_SPEED = 0.04f;
    private static final float SPRINT_MULTIPLIER = 1.15f;
    private static final float STOP_THRESHOLD = 0.005f;

    private static final double JETPACK_OFFSET_DISTANCE = -0.5;
    private static final double JETPACK_HEIGHT_OFFSET = 0.7;
    private static final double JETPACK_SIDE_OFFSET = 0.4;
    private static final int PARTICLE_COUNT = 1;

    private static final Map<UUID, Long> lastEnergyConsumptionTime = new HashMap<>();
    private static final long ENERGY_CONSUMPTION_INTERVAL = 1000; // 1 second
    private static final Map<UUID, Boolean> serverJetpackStates = new HashMap<>();


    private static final Map<UUID, Boolean> playerThrustStates = new HashMap<>();
    private static final Map<UUID, Long> lastThrustTime = new HashMap<>();
    private static final long THRUST_TIMEOUT = 100;

    private static long lastThrustSoundTime = 0;
    private static final long THRUST_SOUND_COOLDOWN = 300;

    public static void setJetpackActive(Player player, boolean active) {
        serverJetpackStates.put(player.getUUID(), active);
    }

    public static void setPlayerThrusting(Player player, boolean thrusting) {
        UUID playerId = player.getUUID();
        playerThrustStates.put(playerId, thrusting);
        if (thrusting) {
            lastThrustTime.put(playerId, System.currentTimeMillis());
        }
    }

    public static boolean isPlayerThrusting(Player player) {
        UUID playerId = player.getUUID();
        Boolean thrusting = playerThrustStates.get(playerId);
        if (thrusting == null || !thrusting) {
            return false;
        }
        Long lastThrust = lastThrustTime.get(playerId);
        if (lastThrust == null) {
            return false;
        }

        return (System.currentTimeMillis() - lastThrust) < THRUST_TIMEOUT;
    }

    public static boolean isJetpackActiveOnServer(Player player) {
        return serverJetpackStates.getOrDefault(player.getUUID(), false);
    }
    private static boolean wasGamePaused = false;
    private static boolean wasJumpPressed = false;
    private static long lastJumpPressTime = 0;
    private static final long DOUBLE_TAP_WINDOW = 300;
    private static boolean clientJetpackActive = false;
    private static long lastLoopSoundTime = 0;
    private static final long LOOP_SOUND_DURATION = 3000;
    private static boolean isLoopSoundPlaying = false;
    private static boolean wasThrustingLastTick = false;

    private static boolean exoSuitFlightEnabled = false;
    private static float originalFlySpeed = 0.05f;
    private static boolean originalMayFly = false;

    private static boolean wasDescending = false;
    private static long landingProtectionTime = 0;
    private static final long LANDING_PROTECTION_DURATION = 500;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (player.tickCount % 10 != 0) {
            return;
        }

        handleJetpackEnergyConsumption(player);
    }

    private static void handleJetpackEnergyConsumption(Player player) {
        boolean jetpackActive = isJetpackActiveOnServer(player);

        if (!jetpackActive) {
            return;
        }

        if (!hasJetpackModule(player)) {
            setJetpackActive(player, false);
            return;
        }

        boolean utilityEnabled = ExoSuitPowerManager.isPowerEnabled(player, "utility");

        if (!utilityEnabled) {
            setJetpackActive(player, false);
            return;
        }

        boolean canFunction = ExoSuitPowerManager.canUpgradeFunction(player, "utility");
        if (!canFunction) {
            setJetpackActive(player, false);
            return;
        }

        boolean isThrusting = isPlayerThrusting(player);

        if (!isThrusting) {
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastConsumption = lastEnergyConsumptionTime.get(playerId);

        if (lastConsumption == null || (currentTime - lastConsumption) >= ENERGY_CONSUMPTION_INTERVAL) {
            ItemStack jetpackUpgrade = findJetpackModuleFromPlayer(player);
            if (jetpackUpgrade.isEmpty()) {
                setJetpackActive(player, false);
                return;
            }

            if (!ExoSuitPowerManager.consumeEnergyForUpgrade(player, "utility", jetpackUpgrade)) {
                setJetpackActive(player, false);
                return;
            }

            lastEnergyConsumptionTime.put(playerId, currentTime);
        }
    }

    private static boolean hasJetpackModule(Player player) {
        return !findJetpackModuleFromPlayer(player).isEmpty();
    }

    private static ItemStack findJetpackModuleFromPlayer(Player player) {
        ItemStack chestplate = getEquippedChestplate(player);
        if (chestplate.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return findJetpackModule(chestplate);
    }

    private static ItemStack getEquippedChestplate(Player player) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ExoSuitItem exosuit &&
                    exosuit.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
                return armorStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (isUsingExoSuitJetpack(player) && exoSuitFlightEnabled && player.getAbilities().flying) {
            if (event.getInput().shiftKeyDown) {
                event.getInput().shiftKeyDown = true;
            }
        }
    }

    private static boolean hasExoSuitJetpackSetup(Player player) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!(chestplate.getItem() instanceof ExoSuitItem)) {
            return false;
        }
        ItemStack jetpackUpgrade = findJetpackModule(chestplate);
        if (jetpackUpgrade.isEmpty()) {
            return false;
        }
        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);

        return totalEffects.hasFlight();
    }

    private static boolean isUsingExoSuitJetpack(Player player) {
        if (player.level().isClientSide) {
            return hasExoSuitJetpackSetup(player) && clientJetpackActive;
        } else {
            return hasExoSuitJetpackSetup(player) && isJetpackActiveOnServer(player);
        }
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
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getSource().type().msgId().equals("fall")) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - landingProtectionTime < LANDING_PROTECTION_DURATION) {
                event.setCanceled(true);
                return;
            }
            if (isUsingExoSuitJetpack(player) && exoSuitFlightEnabled && wasDescending) {
                event.setCanceled(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.level().isClientSide) return;

        if (!hasExoSuitJetpackSetup(player)) {
            if (clientJetpackActive || exoSuitFlightEnabled) {
                disableExoSuitFlight(player);
                stopJetpackSounds(player);
            }
            return;
        }

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
        if (player.isSpectator()) {
            return;
        }

        if (!hasExoSuitJetpackSetup(player)) {
            if (clientJetpackActive) {
                disableExoSuitFlight(player);
            }
            return;
        }

        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);
        float flightSpeed = totalEffects.getFlightSpeed();
        if (flightSpeed <= 0) {
            flightSpeed = 0.1f;
        }

        if (!player.getAbilities().mayfly && clientJetpackActive) {
            enableExoSuitFlight(player, flightSpeed);
        }

        Minecraft mc = Minecraft.getInstance();
        if (clientJetpackActive && player.getAbilities().mayfly) {
            if (player.getAbilities().flying && !player.isCreative()) {
                handleFlightMovement(player, mc, flightSpeed);
            }
        }
    }

    private static void enableExoSuitFlight(Player player, float flightSpeed) {
        if (!exoSuitFlightEnabled) {
            originalMayFly = player.getAbilities().mayfly;
            originalFlySpeed = player.getAbilities().getFlyingSpeed();
        }

        player.getAbilities().mayfly = true;
        player.getAbilities().setFlyingSpeed(flightSpeed);
        player.onUpdateAbilities();

        exoSuitFlightEnabled = true;
    }

    private static void disableExoSuitFlight(Player player) {
        if (exoSuitFlightEnabled) {
            player.getAbilities().mayfly = originalMayFly;
            player.getAbilities().flying = false;
            player.getAbilities().setFlyingSpeed(originalFlySpeed);
            player.onUpdateAbilities();

            exoSuitFlightEnabled = false;
        }

        clientJetpackActive = false;
        wasDescending = false;

        if (player.level().isClientSide) {
            stopJetpackSounds(player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleFlightMovement(Player player, Minecraft mc, float flightSpeed) {
        if (!player.getAbilities().flying || !clientJetpackActive || !exoSuitFlightEnabled || player.isSpectator()) {
            return;
        }

        if (!hasExoSuitJetpackSetup(player)) {
            disableExoSuitFlight(player);
            stopJetpackSounds(player);

            player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS,
                    0.5f, 0.6f, false
            );
            return;
        }

        Vec3 currentVelocity = player.getDeltaMovement();
        double newY;
        boolean isThrusting = false;

        boolean currentlyDescending = false;
        if (mc.options.keyJump.isDown()) {
            newY = FLIGHT_UPWARD_SPEED * flightSpeed;
            isThrusting = true;
        }
        else if (mc.options.keyShift.isDown()) {
            newY = -FLIGHT_DESCENT_SPEED * flightSpeed;
            currentlyDescending = true;
            isThrusting = true;
        }
        else {
            newY = -NATURAL_SINK_SPEED;
        }

        long currentTime = System.currentTimeMillis();
        if (currentlyDescending && !player.onGround()) {
            wasDescending = true;
        } else if (player.onGround() && wasDescending) {
            landingProtectionTime = currentTime;
            wasDescending = false;
        } else if (!currentlyDescending && !player.onGround()) {
            wasDescending = false;
        }

        handleJetpackSounds(player, isThrusting);

        double newX = currentVelocity.x;
        double newZ = currentVelocity.z;

        Vec3 inputVector = getMovementInput(player, mc);

        if (inputVector.lengthSqr() > 0) {
            double speedMultiplier = 1.0;
            if (player.isSprinting()) {
                speedMultiplier = SPRINT_MULTIPLIER;
            }

            double maxSpeedForThisTick = MAX_HORIZONTAL_SPEED * Math.min(flightSpeed, 1.0f) * speedMultiplier;

            newX += inputVector.x * HORIZONTAL_ACCELERATION;
            newZ += inputVector.z * HORIZONTAL_ACCELERATION;

            double horizontalSpeed = Math.sqrt(newX * newX + newZ * newZ);
            if (horizontalSpeed > maxSpeedForThisTick) {
                double ratio = maxSpeedForThisTick / horizontalSpeed;
                newX *= ratio;
                newZ *= ratio;
            }
            if (player.getAbilities().flying && !player.onGround()) {
                isThrusting = true;
            }
        } else {
            newX *= HORIZONTAL_DRAG;
            newZ *= HORIZONTAL_DRAG;

            if (Math.abs(newX) < STOP_THRESHOLD) newX = 0;
            if (Math.abs(newZ) < STOP_THRESHOLD) newZ = 0;
        }
        sendThrustStateToServer(isThrusting);

        player.setDeltaMovement(newX, newY, newZ);
    }

    @OnlyIn(Dist.CLIENT)
    private static void sendThrustStateToServer(boolean thrusting) {
        PacketHandler.getPlayChannel().sendToServer(new top.ribs.scguns.network.message.C2SMessageJetpackThrust(thrusting));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleJetpackSounds(Player player, boolean isThrusting) {
        long currentTime = System.currentTimeMillis();

        if (clientJetpackActive && (!isLoopSoundPlaying || (currentTime - lastLoopSoundTime) >= LOOP_SOUND_DURATION)) {
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

        if (isThrusting && !wasThrustingLastTick &&
                (currentTime - lastThrustSoundTime) >= THRUST_SOUND_COOLDOWN) {

            player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.JETPACK.get(),
                    SoundSource.PLAYERS,
                    0.7f,
                    1.0f + (player.level().random.nextFloat() * 0.1f - 0.05f),
                    false
            );
            lastThrustSoundTime = currentTime;
        }

        wasThrustingLastTick = isThrusting;
    }

    private static void handleJetpackFlight(Player player) {
        if (player.isSpectator()) {
            return;
        }

        if (!hasExoSuitJetpackSetup(player)) {
            if (clientJetpackActive || exoSuitFlightEnabled) {
                disableExoSuitFlight(player);
            }
            return;
        }
        if (!canJetpackFunction(player)) {
            if (clientJetpackActive || exoSuitFlightEnabled) {
                disableExoSuitFlight(player);
            }
            return;
        }

        ExoSuitUpgrade.Effects totalEffects = ExoSuitEffectsHandler.getTotalEffects(player);
        float flightSpeed = totalEffects.getFlightSpeed();
        if (flightSpeed <= 0) {
            flightSpeed = 0.1f;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean jumpPressed = mc.options.keyJump.isDown();
        long currentTime = System.currentTimeMillis();

        if (jumpPressed && !wasJumpPressed) {
            long timeSinceLastPress = currentTime - lastJumpPressTime;

            if (timeSinceLastPress <= DOUBLE_TAP_WINDOW && timeSinceLastPress > 50) {
                if (ExoSuitPowerManager.isPowerEnabled(player, "utility")) {
                    clientJetpackActive = !clientJetpackActive;
                    sendJetpackStateToServer(clientJetpackActive);

                    if (clientJetpackActive) {
                        enableExoSuitFlight(player, flightSpeed);
                        player.getAbilities().flying = true;
                        player.onUpdateAbilities();
                    } else {
                        disableExoSuitFlight(player);
                    }
                }
                lastJumpPressTime = 0;
            } else {
                lastJumpPressTime = currentTime;
            }
        }

        wasJumpPressed = jumpPressed;

        if (clientJetpackActive && exoSuitFlightEnabled && player.getAbilities().mayfly) {
            if (player.getAbilities().flying && !player.isCreative()) {
                handleFlightMovement(player, mc, flightSpeed);
                spawnJetpackParticles(player, mc);
                Minecraft mcInstance = Minecraft.getInstance();
                boolean isThrusting = mcInstance.options.keyJump.isDown();
                handleJetpackSounds(player, isThrusting);
            }
        }
    }
    private static boolean canJetpackFunction(Player player) {
        if (!ExoSuitPowerManager.isPowerEnabled(player, "utility")) {
            return false;
        }

        if (!ExoSuitPowerManager.canUpgradeFunction(player, "utility")) {
            return false;
        }

        return hasJetpackModule(player);
    }
    @OnlyIn(Dist.CLIENT)
    private static void sendJetpackStateToServer(boolean active) {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageJetpackState(active));
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
            double velocityZ = (level.random.nextDouble() - 0.5) * 0.2;

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

    public static void onPlayerLogout(Player player) {
        UUID playerId = player.getUUID();
        lastEnergyConsumptionTime.remove(playerId);
        serverJetpackStates.remove(playerId);
        playerThrustStates.remove(playerId);
        lastThrustTime.remove(playerId);
    }
}