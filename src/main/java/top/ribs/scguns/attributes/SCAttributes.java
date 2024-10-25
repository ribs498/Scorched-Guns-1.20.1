package top.ribs.scguns.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.ScorchedGuns;

public class SCAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ScorchedGuns.MODID);
    public static final RegistryObject<Attribute> PROJECTILE_SPEED = ATTRIBUTES.register("projectile_speed",
            () -> new RangedAttribute("attribute.scguns.projectile_speed", 1.0, 0.01, 100.0)
                    .setSyncable(true)
    );

    public static final RegistryObject<Attribute> FIRE_RATE = ATTRIBUTES.register("fire_rate",
            () -> new RangedAttribute("attribute.scguns.fire_rate", 1.0F, 0.01F, 100.0F)
                    .setSyncable(true)
    );

    public static final RegistryObject<Attribute> ADDITIONAL_BULLET_DAMAGE = ATTRIBUTES.register("additional_bullet_damage",
            () -> new RangedAttribute("attribute.scguns.additional_bullet_damage", 0.0, -1000, 10000.0)
                    .setSyncable(true)
    );

    public static final RegistryObject<Attribute> RELOAD_SPEED = ATTRIBUTES.register("reload_speed",
            () -> new RangedAttribute("attribute.scguns.reload_speed", 1.0, 0.01, 1000.0)
                    .setSyncable(true)
    );

    public static final RegistryObject<Attribute> BULLET_DAMAGE_MULTIPLIER = ATTRIBUTES.register("bullet_damage_multiplier",
            () -> new RangedAttribute("attribute.scguns.bullet_damage_multiplier", 1.0, 0, 1000.0)
                    .setSyncable(true)
    );

    //The only reason this is from 0-100 instead of 0-1 given its a percentage is because it looks nicer for my skilltree stuff lmao
    //-Billnotic
    public static final RegistryObject<Attribute> BULLET_RESISTANCE = ATTRIBUTES.register("bullet_resistance",
            () -> new RangedAttribute("attribute.scguns.bullet_resistance", 0.0, 0, 100.0)
                    .setSyncable(true)
    );
    public static final RegistryObject<Attribute> SPREAD_MULTIPLIER = ATTRIBUTES.register("spread_multiplier",
            () -> new RangedAttribute("attribute.scguns.spread_multiplier", 1.0, 0, 1000.0)
                    .setSyncable(true)
    );
}