package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MobTrapBlockEntity extends BlockEntity {
    private final List<EntityType<?>> storedMobs = new ArrayList<>();
    private static final int MAX_MOBS = 20;
    private final int radius;
    private final int heightRadius;
    private int tickCounter;

    public MobTrapBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MOB_TRAP.get(), pPos, pBlockState);
        this.radius = 10;
        this.heightRadius = 3;
        this.tickCounter = 0;
    }

    public boolean addMob(EntityType<?> mobType) {
        if (storedMobs.size() < MAX_MOBS) {
            storedMobs.add(mobType);
            setChanged();
            return true;
        }
        return false;
    }

    public void releaseMobs(ServerLevel level, BlockPos pos) {
        if (!storedMobs.isEmpty()) {
            for (EntityType<?> mob : storedMobs) {
                Entity entity = mob.create(level);
                if (entity != null) {
                    entity.moveTo(pos.getX() + level.random.nextDouble(), pos.getY(), pos.getZ() + level.random.nextDouble(), level.random.nextFloat() * 360.0F, 0.0F);
                    level.addFreshEntity(entity);
                    for (int i = 0; i < 20; i++) {
                        level.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY(), entity.getZ(), 1, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }
            storedMobs.clear();
            setChanged();
            level.destroyBlock(pos, true);
        }
    }

    public List<EntityType<?>> getStoredMobs() {
        return this.storedMobs;
    }


    public void serverTick(Level level, BlockPos pos) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;

                AABB detectionBox = new AABB(
                        pos.getX() - radius, pos.getY() - heightRadius, pos.getZ() - radius,
                        pos.getX() + radius, pos.getY() + heightRadius, pos.getZ() + radius
                );
                List<Player> players = serverLevel.getEntitiesOfClass(Player.class, detectionBox, player ->
                        !player.isCreative() && !player.isSpectator()
                );
                if (!players.isEmpty()) {
                    releaseMobs(serverLevel, pos);
                }
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedMobs.clear();
        ListTag mobList = tag.getList("StoredMobs", Tag.TAG_STRING);
        for (int i = 0; i < mobList.size(); i++) {
            String mobId = mobList.getString(i);
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobId));
            if (entityType != null) {
                storedMobs.add(entityType);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag mobList = new ListTag();
        for (EntityType<?> mob : storedMobs) {
            mobList.add(StringTag.valueOf(ForgeRegistries.ENTITY_TYPES.getKey(mob).toString()));
        }
        tag.put("StoredMobs", mobList);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }
}