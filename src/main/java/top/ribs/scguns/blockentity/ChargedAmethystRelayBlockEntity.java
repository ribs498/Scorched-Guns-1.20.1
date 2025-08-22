package top.ribs.scguns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import top.ribs.scguns.block.ChargedAmethystRelayBlock;
import top.ribs.scguns.init.ModBlockEntities;

public class ChargedAmethystRelayBlockEntity extends BlockEntity {
    private NoteBlockInstrument tunedInstrument = NoteBlockInstrument.HARP;
    private boolean isActive = false;
    private int activationTimer = 0;
    private static final int ACTIVATION_DURATION = 20; // 1 second

    public ChargedAmethystRelayBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CHARGED_AMETHYST_RELAY.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChargedAmethystRelayBlockEntity relay) {
        if (!(level instanceof ServerLevel)) return;
        if (relay.isActive && relay.activationTimer > 0) {
            relay.activationTimer--;

            if (relay.activationTimer <= 0) {
                relay.isActive = false;
                relay.setChanged();
            }
        }

        NoteBlockInstrument stateInstrument = state.getValue(ChargedAmethystRelayBlock.INSTRUMENT);
        if (stateInstrument != relay.tunedInstrument) {
            relay.tunedInstrument = stateInstrument;
            relay.setChanged();
        }
    }

    public void onInstrumentTuned(NoteBlockInstrument instrument) {
        this.tunedInstrument = instrument;
        setChanged();
    }

    public void activateFromInstrument() {
        if (!isActive) {
            isActive = true;
            activationTimer = ACTIVATION_DURATION;

            assert level != null;
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 1.0F, 1.0F);

            setChanged();
        }
    }

    public void deactivate() {
        if (isActive) {
            isActive = false;
            activationTimer = 0;
            setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.tunedInstrument = NoteBlockInstrument.valueOf(tag.getString("TunedInstrument"));
        this.isActive = tag.getBoolean("IsActive");
        this.activationTimer = tag.getInt("ActivationTimer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("TunedInstrument", this.tunedInstrument.name());
        tag.putBoolean("IsActive", this.isActive);
        tag.putInt("ActivationTimer", this.activationTimer);
    }
    public boolean isActive() {
        return isActive;
    }
}