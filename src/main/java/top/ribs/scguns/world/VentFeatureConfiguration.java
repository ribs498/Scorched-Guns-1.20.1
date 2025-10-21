package top.ribs.scguns.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public record VentFeatureConfiguration(Block ventBlock, int minHeight, int maxHeight, Block baseBlock,
                                       List<Block> requiredBelowBlocks, boolean placeBaseBlock,
                                       boolean requiresWaterlogged, boolean checkActiveLimit, int maxActiveNearby,
                                       int checkRadius) implements FeatureConfiguration {
    public static final Codec<VentFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("vent_block").forGetter(config -> config.ventBlock),
                    Codec.INT.fieldOf("min_height").orElse(1).forGetter(config -> config.minHeight),
                    Codec.INT.fieldOf("max_height").orElse(3).forGetter(config -> config.maxHeight),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("base_block").forGetter(config -> config.baseBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("required_below_blocks").forGetter(config -> config.requiredBelowBlocks),
                    Codec.BOOL.fieldOf("place_base_block").orElse(true).forGetter(config -> config.placeBaseBlock),
                    Codec.BOOL.fieldOf("requires_waterlogged").orElse(false).forGetter(config -> config.requiresWaterlogged),
                    Codec.BOOL.fieldOf("check_active_limit").orElse(false).forGetter(config -> config.checkActiveLimit),
                    Codec.INT.fieldOf("max_active_nearby").orElse(1).forGetter(config -> config.maxActiveNearby),
                    Codec.INT.fieldOf("check_radius").orElse(32).forGetter(config -> config.checkRadius)
            ).apply(instance, VentFeatureConfiguration::new)
    );

    public boolean shouldPlaceBaseBlock() {
        return placeBaseBlock;
    }

    public boolean shouldCheckActiveLimit() {
        return checkActiveLimit;
    }

    public Block getVentBlock() {
        return ventBlock;
    }
}