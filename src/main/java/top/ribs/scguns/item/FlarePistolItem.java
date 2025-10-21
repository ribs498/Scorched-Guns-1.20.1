package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.config.RaidFlareConfig;
import top.ribs.scguns.entity.projectile.RaidFlareEntity;
import top.ribs.scguns.entity.raid.RaidManager;

import javax.annotation.Nullable;
import java.util.List;

public class FlarePistolItem extends Item {
    public FlarePistolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pistolStack = player.getItemInHand(hand);
        ItemStack offhandStack = player.getOffhandItem();

        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(pistolStack);
        }

        if (!(offhandStack.getItem() instanceof RaidFlareItem flareItem)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.scguns.flare_pistol.no_flare")
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
            return InteractionResultHolder.fail(pistolStack);
        }

        if (!level.isClientSide) {
            if (level instanceof ServerLevel serverLevel) {
                if (RaidManager.hasActiveRaidInDimension(serverLevel)) {
                    player.displayClientMessage(
                            Component.translatable("item.scguns.flare_pistol.raid_active")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return InteractionResultHolder.fail(pistolStack);
                }
            }

            String raidId = flareItem.getRaidId();

            RaidFlareConfig.FlareData flareData = RaidFlareConfig.getFlareData(raidId);
            if (flareData == null) {
                player.displayClientMessage(
                        Component.translatable("item.scguns.flare_pistol.no_raid_config")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResultHolder.fail(pistolStack);
            }

            RaidFlareEntity flare = new RaidFlareEntity(level, player, raidId);

            Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
            Vec3 lookAngle = player.getLookAngle();

            flare.setPos(playerPos.x, playerPos.y, playerPos.z);

            double speed = 2.5;
            flare.setDeltaMovement(
                    lookAngle.x * speed,
                    lookAngle.y * speed,
                    lookAngle.z * speed
            );

            level.addFreshEntity(flare);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.9F);

            if (!player.getAbilities().instabuild) {
                offhandStack.shrink(1);
            }

            player.getCooldowns().addCooldown(this, 30);
        }

        return InteractionResultHolder.consume(pistolStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.scguns.flare_pistol.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.scguns.flare_pistol.usage")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}