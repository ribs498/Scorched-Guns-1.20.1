package top.ribs.scguns.item;

import net.minecraft.world.item.Item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import top.ribs.scguns.entity.monster.SupplyScampEntity;
import top.ribs.scguns.init.ModEntities;

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
                serverLevel.addFreshEntity(supplyScamp);
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }
}

