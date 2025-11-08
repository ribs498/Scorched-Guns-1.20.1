package top.ribs.scguns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class GuanoItemEventHandler {

    private static final Set<ItemEntity> guanoItems = new HashSet<>();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.is(ModItems.BAT_GUANO.get())) {
            guanoItems.add(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;

        Iterator<ItemEntity> iterator = guanoItems.iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();

            if (!itemEntity.isAlive() || itemEntity.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!itemEntity.onGround() || itemEntity.tickCount % 15 != 0) continue;

            if (tryFormGuanoLayer(itemEntity)) {
                iterator.remove();
            }
        }
    }

    private static boolean tryFormGuanoLayer(ItemEntity itemEntity) {
        Level level = itemEntity.level();
        BlockPos pos = itemEntity.blockPosition();
        BlockState stateAtPos = level.getBlockState(pos);
        BlockState stateBelow = level.getBlockState(pos.below());
        boolean shouldRemove = false;

        if (stateAtPos.is(ModBlocks.BAT_GUANO_LAYER.get())) {
            int layers = stateAtPos.getValue(SnowLayerBlock.LAYERS);
            if (layers < 8) {
                level.setBlock(pos, stateAtPos.setValue(SnowLayerBlock.LAYERS, layers + 1), 3);
                itemEntity.getItem().shrink(1);
                if (itemEntity.getItem().isEmpty()) {
                    shouldRemove = true;
                }
            }
        } else if (!stateBelow.isAir() && stateBelow.isSolidRender(level, pos.below()) && stateAtPos.isAir()) {
            BlockState newLayer = ModBlocks.BAT_GUANO_LAYER.get().defaultBlockState()
                    .setValue(SnowLayerBlock.LAYERS, 1);
            level.setBlock(pos, newLayer, 3);
            itemEntity.getItem().shrink(1);
            if (itemEntity.getItem().isEmpty()) {
                shouldRemove = true;
            }
        }

        if (shouldRemove) {
            itemEntity.discard();
        }

        return shouldRemove;
    }
}