package top.ribs.scguns.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.Reference;
import top.ribs.scguns.particles.BulletHoleData;
import top.ribs.scguns.particles.TrailData;

/**
 * Author: MrCrayfish
 */
public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.MOD_ID);

    public static final RegistryObject<ParticleType<BulletHoleData>> BULLET_HOLE = REGISTER.register("bullet_hole", () -> new ParticleType<>(false, BulletHoleData.DESERIALIZER) {
        @Override
        public Codec<BulletHoleData> codec() {
            return BulletHoleData.CODEC;
        }
    });

    public static final RegistryObject<SimpleParticleType> BLOOD = REGISTER.register("blood", () -> new SimpleParticleType(true));
    public static final RegistryObject<ParticleType<TrailData>> TRAIL = REGISTER.register("trail", () -> new ParticleType<>(false, TrailData.DESERIALIZER) {
        @Override
        public Codec<TrailData> codec() {
            return TrailData.CODEC;
        }
    });
    public static final RegistryObject<SimpleParticleType> COPPER_CASING_PARTICLE = REGISTER.register("copper_casing", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> IRON_CASING_PARTICLE = REGISTER.register("iron_casing", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> DIAMOND_STEEL_CASING_PARTICLE = REGISTER.register("diamond_steel_casing", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType>BRASS_CASING_PARTICLE = REGISTER.register("brass_casing", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SHELL_PARTICLE = REGISTER.register("shell", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BEARPACK_PARTICLE = REGISTER.register("bearpack_shell", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ROCKET_TRAIL = REGISTER.register("rocket_trail", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GREEN_FLAME = REGISTER.register("green_flame", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PLASMA_RING = REGISTER.register("plasma_ring", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> PLASMA_EXPLOSION = REGISTER.register("plasma_explosion", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RAMROD_IMPACT = REGISTER.register("ramrod_impact", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BEOWULF_IMPACT = REGISTER.register("beowulf_impact", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> LASER = REGISTER.register("laser", () -> new SimpleParticleType(true));
}
