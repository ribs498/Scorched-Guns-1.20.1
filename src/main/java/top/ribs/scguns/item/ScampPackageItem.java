package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.monster.SupplyScampEntity;
import top.ribs.scguns.init.ModEntities;

import java.util.List;

public class ScampPackageItem extends Item {

    public ScampPackageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) world;
            HitResult result = player.pick(10.0D, 0.0F, false);
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) result;
                BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
                Vec3 spawnPos = new Vec3(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);

                SupplyScampEntity supplyScamp = new SupplyScampEntity(ModEntities.SUPPLY_SCAMP.get(), serverLevel);
                supplyScamp.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                supplyScamp.tame(player);
                supplyScamp.setOrderedToSit(true);
                supplyScamp.setSitting(true);

                serverLevel.addFreshEntity(supplyScamp);

                ItemStack guideBook = top.ribs.scguns.common.MobGuideHelper.createGuideBook(ModEntities.SUPPLY_SCAMP.get());
                if (guideBook != null && !player.getInventory().add(guideBook)) {
                    player.drop(guideBook, false);
                }

                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("info.scguns.mob_guide").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}