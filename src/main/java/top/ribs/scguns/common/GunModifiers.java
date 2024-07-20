package top.ribs.scguns.common;

import net.minecraft.util.Mth;
import top.ribs.scguns.interfaces.IGunModifier;

/**
 * Author: MrCrayfish
 */
public class GunModifiers
{
    public static final IGunModifier SILENCED = new IGunModifier()
    {
        @Override
        public boolean silencedFire()
        {
            return true;
        }
        @Override
        public double modifyFireSoundRadius(double radius)
        {
            return radius * 0.5;
        }
    };

    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        {
            return damage * 0.9F;
        }
    };
    public static final IGunModifier INCREASED_DAMAGE = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        {
            return damage * 1.2F;
        }
    };
    public static final IGunModifier IRON_BAYONET_DAMAGE = new IGunModifier() {
        @Override
        public float additionalDamage() {
            return 2.0F;
        }
    };
    public static final IGunModifier ANTHRALITE_BAYONET_DAMAGE = new IGunModifier() {
        @Override
        public float additionalDamage() {
            return 3.0F;
        }
    };
    public static final IGunModifier DIAMOND_BAYONET_DAMAGE = new IGunModifier() {
        @Override
        public float additionalDamage() {
            return 4.0F;
        }
    };

    public static final IGunModifier NETHERITE_BAYONET_DAMAGE = new IGunModifier() {
        @Override
        public float additionalDamage() {
            return 6.0F;
        }
    };

    public static final IGunModifier SLOW_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.97F;
        }
        @Override
        public float recoilModifier()
        {
            return 1.05F;
        }
    };

    public static final IGunModifier SLOWER_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.85F;
        }
        @Override
        public float recoilModifier()
        {
            return 1.05F;
        }
    };
    public static final IGunModifier NORMAL_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 1.2F;
        }
    };
    public static final IGunModifier SLOWEST_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.65F;
        }
        @Override
        public float recoilModifier()
        {
            return 1.2F;
        }
    };
    public static final IGunModifier BETTER_CONTROL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.85F;
        }

        @Override
        public float kickModifier()
        {
            return 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.85F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.97F;
        }
    };
  public static final IGunModifier SLIGHTLY_STABILISED = new IGunModifier()
    {
        @Override
        public float kickModifier()
        {
            return 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.85F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.98F;
        }
    };
    public static final IGunModifier STABILISED = new IGunModifier()
    {
        @Override
        public float kickModifier()
        {
            return 0.8F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.65F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }
    };

    public static final IGunModifier SUPER_STABILISED = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.75F;
        }

        @Override
        public float kickModifier()
        {
            return 0.75F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.75F;
        }

        @Override
        public int modifyFireRate(int rate)
        {
            return Mth.clamp(rate, rate, Integer.MAX_VALUE);
        }
    };

    public static final IGunModifier LIGHT_RECOIL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.85F;
        }

        @Override
        public float kickModifier()
        {
            return 0.85F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 1.1F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.9F;
        }
    };

    public static final IGunModifier REDUCED_RECOIL = new IGunModifier()
    {
        @Override
        public float recoilModifier()
        {
            return 0.8F;
        }

        @Override
        public float kickModifier()
        {
            return 0.8F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.97F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.75F;
        }
    };
    public static final IGunModifier SLOW_RELOAD = new IGunModifier() {
        @Override
        public double modifyReloadSpeed(double reloadSpeed) {
            return reloadSpeed * 1.4;
        }
    };

    public static final IGunModifier FAST_RELOAD = new IGunModifier() {
        @Override
        public double modifyReloadSpeed(double reloadSpeed) {
            return reloadSpeed * 0.7;
        }
    };
    public static final IGunModifier EXTENDED_MAG = new IGunModifier() {
        @Override
        public int modifyAmmoCapacity(int baseCapacity) {
            return (int) (baseCapacity * 1.75);
        }
    };

    public static final IGunModifier PLUS_P_MAG = new IGunModifier() {
        @Override
        public int modifyAmmoCapacity(int baseCapacity) {
            return (int) (baseCapacity * 0.5);
        }
    };
}