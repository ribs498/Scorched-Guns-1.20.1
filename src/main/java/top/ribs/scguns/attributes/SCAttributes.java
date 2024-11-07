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

    //-1000 to 100, further above 0 the faster it goes, further under the slower.
    //I was able to get it working but it is purely serversided and desynced with the client, client code for cooldowns
    //use methods that I can use player entities as input, however in some cases where it was called the player holding the
    //item wasn't available. Perhaps there is a way to get the attributes of a player holding a weapon from within the weapon,
    //but I'm not educated enough on that.
    //public static final RegistryObject<Attribute> FIRE_RATE_MULTIPLIER = ATTRIBUTES.register("fire_rate_multiplier",
    //        () -> new RangedAttribute("attribute.scguns.fire_rate_multiplier", 0.0F, -1000.0F, 99.9F)
    //                .setSyncable(true)
    //);

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

    //0-100, effectively 0 to 1.
    //Disabled due to difficulty of testing without my server up.
    //public static final RegistryObject<Attribute> BULLET_RESISTANCE = ATTRIBUTES.register("bullet_resistance",
    //        () -> new RangedAttribute("attribute.scguns.bullet_resistance", 0.0, 0, 100.0)
    //                .setSyncable(true)
    //);
    public static final RegistryObject<Attribute> SPREAD_MULTIPLIER = ATTRIBUTES.register("spread_multiplier",
            () -> new RangedAttribute("attribute.scguns.spread_multiplier", 1.0, 0, 1000.0)
                    .setSyncable(true)
    );
}