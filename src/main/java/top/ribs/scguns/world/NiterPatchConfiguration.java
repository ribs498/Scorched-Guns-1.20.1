package top.ribs.scguns.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record NiterPatchConfiguration(Block niterBlock, int minLayers, int maxLayers,
                                      int spreadRadius, int spreadAttempts) implements FeatureConfiguration {
    public static final Codec<NiterPatchConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("niter_block").forGetter(config -> config.niterBlock),
                    Codec.INT.fieldOf("min_layers").orElse(1).forGetter(config -> config.minLayers),
                    Codec.INT.fieldOf("max_layers").orElse(3).forGetter(config -> config.maxLayers),
                    Codec.INT.fieldOf("spread_radius").orElse(3).forGetter(config -> config.spreadRadius),
                    Codec.INT.fieldOf("spread_attempts").orElse(16).forGetter(config -> config.spreadAttempts)
            ).apply(instance, NiterPatchConfiguration::new)
    );
}