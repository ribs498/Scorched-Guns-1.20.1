package top.ribs.scguns.init;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.util.GunModifierHelper;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;


// Fixed death messages by MikhailTapio!
public class ModDamageTypes
{
    public static final ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Reference.MOD_ID, "bullet"));
    public static final ResourceKey<DamageType> MELEE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Reference.MOD_ID, "melee"));

    /**
     * Based on code in Botania by Vazkii
     * <a href="https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/common/BotaniaDamageTypes.java">Link</a>
     */
    public static class Sources
    {
        private static Holder.Reference<DamageType> getHolder(RegistryAccess access, ResourceKey<DamageType> damageTypeKey) {
            return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageTypeKey);
        }

        public static DamageSource projectile(RegistryAccess access, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
            return new DamageSource(getHolder(access, BULLET), directEntity, causingEntity);
        }

        private static DamageSource source(RegistryAccess access, ResourceKey<DamageType> damageTypeKey, @Nullable Entity directEntity, @Nullable Entity causingEntity)
        {
            return new BulletDamageSource(getHolder(access, damageTypeKey), directEntity, causingEntity);
        }

        public static DamageSource projectile(RegistryAccess access, ProjectileEntity projectile, LivingEntity entity)
        {
            return source(access, BULLET, projectile, entity);
        }
        public static DamageSource melee(RegistryAccess access, LivingEntity entity) {
            return source(access, MELEE, null, entity);
        }
        public static class BulletDamageSource extends DamageSource {
            private static final String[] msgSuffix = {
                    "scguns.bullet.killed",
                    "scguns.bullet.eliminated",
                    "scguns.bullet.executed",
                    "scguns.bullet.annihilated",
                    "scguns.bullet.decimated"
            };
            public BulletDamageSource(Holder<DamageType> pType, Entity pDirectEntity, Entity pCausingEntity) {
                super(pType, pDirectEntity, pCausingEntity);
            }

            public Component getLocalizedDeathMessage(LivingEntity pLivingEntity) {
                final String s = "death.attack." + this.getMsgId();

                if (this.getEntity() == null && this.getDirectEntity() == null) {
                    LivingEntity living = pLivingEntity.getKillCredit();
                    return living != null ? Component.translatable(s + ".player", pLivingEntity.getDisplayName(), living.getDisplayName()) : Component.translatable(s, pLivingEntity.getDisplayName());
                } else {
                    final Component component = this.getEntity() == null ? this.getDirectEntity().getDisplayName() : this.getEntity().getDisplayName();
                    final ItemStack stack = this.getEntity() instanceof LivingEntity livingentity ? livingentity.getMainHandItem() : ItemStack.EMPTY;
                    final boolean isSilenced = GunModifierHelper.isSilencedFire(stack);

                    return isSilenced ? Component.translatable(s + ".silenced", pLivingEntity.getDisplayName()) : !stack.isEmpty() && stack.hasCustomHoverName() ?
                            Component.translatable(
                                    s + ".item",
                                    pLivingEntity.getDisplayName(),
                                    component,
                                    stack.getDisplayName()
                            ) : Component.translatable(s, pLivingEntity.getDisplayName(), component);
                }
            }

            @Override
            public String getMsgId() {
                return msgSuffix[ThreadLocalRandom.current().nextInt(5)];
            }
        }
    }
}