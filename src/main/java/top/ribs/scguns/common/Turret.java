package top.ribs.scguns.common;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Reference;
import top.ribs.scguns.annotation.Optional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Turret implements INBTSerializable<CompoundTag> {
    protected Targeting targeting = new Targeting();
    protected Combat combat = new Combat();
    protected Ammunition ammunition = new Ammunition();
    protected Behavior behavior = new Behavior();
    protected Display display = new Display();

    public Targeting getTargeting() {
        return this.targeting;
    }

    public Combat getCombat() {
        return this.combat;
    }

    public Ammunition getAmmunition() {
        return this.ammunition;
    }

    public Behavior getBehavior() {
        return this.behavior;
    }

    public Display getDisplay() {
        return this.display;
    }

    public static class Targeting implements INBTSerializable<CompoundTag> {
        private double range = 12.0;
        private double verticalRange = 12.0;
        private double minFiringDistance = 1.3;
        private float rotationSpeed = 0.5F;
        private float positionSmoothing = 0.2F;
        private float maxPitch = 60.0F;
        private float minPitch = -25.0F;
        private int predictionMultiplier = 7;
        private boolean requiresLineOfSight = true;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("Range", this.range);
            tag.putDouble("VerticalRange", this.verticalRange);
            tag.putDouble("MinFiringDistance", this.minFiringDistance);
            tag.putFloat("RotationSpeed", this.rotationSpeed);
            tag.putFloat("PositionSmoothing", this.positionSmoothing);
            tag.putFloat("MaxPitch", this.maxPitch);
            tag.putFloat("MinPitch", this.minPitch);
            tag.putInt("PredictionMultiplier", this.predictionMultiplier);
            tag.putBoolean("RequiresLineOfSight", this.requiresLineOfSight);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Range", Tag.TAG_ANY_NUMERIC)) {
                this.range = tag.getDouble("Range");
            }
            if (tag.contains("VerticalRange", Tag.TAG_ANY_NUMERIC)) {
                this.verticalRange = tag.getDouble("VerticalRange");
            }
            if (tag.contains("MinFiringDistance", Tag.TAG_ANY_NUMERIC)) {
                this.minFiringDistance = tag.getDouble("MinFiringDistance");
            }
            if (tag.contains("RotationSpeed", Tag.TAG_ANY_NUMERIC)) {
                this.rotationSpeed = tag.getFloat("RotationSpeed");
            }
            if (tag.contains("PositionSmoothing", Tag.TAG_ANY_NUMERIC)) {
                this.positionSmoothing = tag.getFloat("PositionSmoothing");
            }
            if (tag.contains("MaxPitch", Tag.TAG_ANY_NUMERIC)) {
                this.maxPitch = tag.getFloat("MaxPitch");
            }
            if (tag.contains("MinPitch", Tag.TAG_ANY_NUMERIC)) {
                this.minPitch = tag.getFloat("MinPitch");
            }
            if (tag.contains("PredictionMultiplier", Tag.TAG_ANY_NUMERIC)) {
                this.predictionMultiplier = tag.getInt("PredictionMultiplier");
            }
            if (tag.contains("RequiresLineOfSight", Tag.TAG_ANY_NUMERIC)) {
                this.requiresLineOfSight = tag.getBoolean("RequiresLineOfSight");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.range != 12.0) object.addProperty("range", this.range);
            if (this.verticalRange != 12.0) object.addProperty("verticalRange", this.verticalRange);
            if (this.minFiringDistance != 1.3) object.addProperty("minFiringDistance", this.minFiringDistance);
            if (this.rotationSpeed != 0.5F) object.addProperty("rotationSpeed", this.rotationSpeed);
            if (this.positionSmoothing != 0.2F) object.addProperty("positionSmoothing", this.positionSmoothing);
            if (this.maxPitch != 60.0F) object.addProperty("maxPitch", this.maxPitch);
            if (this.minPitch != -25.0F) object.addProperty("minPitch", this.minPitch);
            if (this.predictionMultiplier != 7) object.addProperty("predictionMultiplier", this.predictionMultiplier);
            if (!this.requiresLineOfSight) object.addProperty("requiresLineOfSight", false);
            return object;
        }

        public Targeting copy() {
            Targeting targeting = new Targeting();
            targeting.range = this.range;
            targeting.verticalRange = this.verticalRange;
            targeting.minFiringDistance = this.minFiringDistance;
            targeting.rotationSpeed = this.rotationSpeed;
            targeting.positionSmoothing = this.positionSmoothing;
            targeting.maxPitch = this.maxPitch;
            targeting.minPitch = this.minPitch;
            targeting.predictionMultiplier = this.predictionMultiplier;
            targeting.requiresLineOfSight = this.requiresLineOfSight;
            return targeting;
        }

        public double getRange() { return this.range; }
        public double getVerticalRange() { return this.verticalRange; }
        public double getMinFiringDistance() { return this.minFiringDistance; }
        public float getRotationSpeed() { return this.rotationSpeed; }
        public float getPositionSmoothing() { return this.positionSmoothing; }
        public float getMaxPitch() { return this.maxPitch; }
        public float getMinPitch() { return this.minPitch; }
        public int getPredictionMultiplier() { return this.predictionMultiplier; }
        public boolean requiresLineOfSight() { return this.requiresLineOfSight; }
        public void setRange(double range) { this.range = range; }
        public void setVerticalRange(double verticalRange) { this.verticalRange = verticalRange; }
        public void setMinFiringDistance(double minFiringDistance) { this.minFiringDistance = minFiringDistance; }
        public void setRotationSpeed(float rotationSpeed) { this.rotationSpeed = rotationSpeed; }
        public void setPositionSmoothing(float positionSmoothing) { this.positionSmoothing = positionSmoothing; }
        public void setMaxPitch(float maxPitch) { this.maxPitch = maxPitch; }
        public void setMinPitch(float minPitch) { this.minPitch = minPitch; }
        public void setPredictionMultiplier(int predictionMultiplier) { this.predictionMultiplier = predictionMultiplier; }
        public void setRequiresLineOfSight(boolean requiresLineOfSight) { this.requiresLineOfSight = requiresLineOfSight; }
    }

    public static class Combat implements INBTSerializable<CompoundTag> {
        private int cooldown = 16;
        private float inaccuracy = 0.05F;
        @Optional
        private int pelletCount = 1;
        @Optional
        private float spreadAngle = 0.0F;
        private float recoilMax = 4.0F;
        private float recoilSpeed = 0.3F;
        @Optional
        private int damageModifier = 0;
        private double projectileSpeed = 3.0;
        @Optional
        private ResourceLocation fireSound;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Cooldown", this.cooldown);
            tag.putFloat("Inaccuracy", this.inaccuracy);
            tag.putInt("PelletCount", this.pelletCount);
            tag.putFloat("SpreadAngle", this.spreadAngle);
            tag.putFloat("RecoilMax", this.recoilMax);
            tag.putFloat("RecoilSpeed", this.recoilSpeed);
            tag.putInt("DamageModifier", this.damageModifier);
            tag.putDouble("ProjectileSpeed", this.projectileSpeed);
            if (this.fireSound != null) {
                tag.putString("FireSound", this.fireSound.toString());
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Cooldown", Tag.TAG_ANY_NUMERIC)) {
                this.cooldown = tag.getInt("Cooldown");
            }
            if (tag.contains("Inaccuracy", Tag.TAG_ANY_NUMERIC)) {
                this.inaccuracy = tag.getFloat("Inaccuracy");
            }
            if (tag.contains("PelletCount", Tag.TAG_ANY_NUMERIC)) {
                this.pelletCount = tag.getInt("PelletCount");
            }
            if (tag.contains("SpreadAngle", Tag.TAG_ANY_NUMERIC)) {
                this.spreadAngle = tag.getFloat("SpreadAngle");
            }
            if (tag.contains("RecoilMax", Tag.TAG_ANY_NUMERIC)) {
                this.recoilMax = tag.getFloat("RecoilMax");
            }
            if (tag.contains("RecoilSpeed", Tag.TAG_ANY_NUMERIC)) {
                this.recoilSpeed = tag.getFloat("RecoilSpeed");
            }
            if (tag.contains("DamageModifier", Tag.TAG_ANY_NUMERIC)) {
                this.damageModifier = tag.getInt("DamageModifier");
            }
            if (tag.contains("ProjectileSpeed", Tag.TAG_ANY_NUMERIC)) {
                this.projectileSpeed = tag.getDouble("ProjectileSpeed");
            }
            if (tag.contains("FireSound", Tag.TAG_STRING)) {
                this.fireSound = new ResourceLocation(tag.getString("FireSound"));
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.cooldown != 16) object.addProperty("cooldown", this.cooldown);
            if (this.inaccuracy != 0.05F) object.addProperty("inaccuracy", this.inaccuracy);
            if (this.pelletCount != 1) object.addProperty("pelletCount", this.pelletCount);
            if (this.spreadAngle != 0.0F) object.addProperty("spreadAngle", this.spreadAngle);
            if (this.recoilMax != 4.0F) object.addProperty("recoilMax", this.recoilMax);
            if (this.recoilSpeed != 0.3F) object.addProperty("recoilSpeed", this.recoilSpeed);
            if (this.damageModifier != 0) object.addProperty("damageModifier", this.damageModifier);
            if (this.projectileSpeed != 3.0) object.addProperty("projectileSpeed", this.projectileSpeed);
            if (this.fireSound != null) object.addProperty("fireSound", this.fireSound.toString());
            return object;
        }

        public Combat copy() {
            Combat combat = new Combat();
            combat.cooldown = this.cooldown;
            combat.inaccuracy = this.inaccuracy;
            combat.pelletCount = this.pelletCount;
            combat.spreadAngle = this.spreadAngle;
            combat.recoilMax = this.recoilMax;
            combat.recoilSpeed = this.recoilSpeed;
            combat.damageModifier = this.damageModifier;
            combat.projectileSpeed = this.projectileSpeed;
            combat.fireSound = this.fireSound;
            return combat;
        }

        public int getCooldown() { return this.cooldown; }
        public float getInaccuracy() { return this.inaccuracy; }
        public int getPelletCount() { return this.pelletCount; }
        public float getSpreadAngle() { return this.spreadAngle; }
        public float getRecoilMax() { return this.recoilMax; }
        public float getRecoilSpeed() { return this.recoilSpeed; }
        public int getDamageModifier() { return this.damageModifier; }
        public double getProjectileSpeed() { return this.projectileSpeed; }
        @Nullable
        public ResourceLocation getFireSound() { return this.fireSound; }
        public void setCooldown(int cooldown) { this.cooldown = cooldown; }
        public void setInaccuracy(float inaccuracy) { this.inaccuracy = inaccuracy; }
        public void setPelletCount(int pelletCount) { this.pelletCount = pelletCount; }
        public void setSpreadAngle(float spreadAngle) { this.spreadAngle = spreadAngle; }
        public void setRecoilMax(float recoilMax) { this.recoilMax = recoilMax; }
        public void setRecoilSpeed(float recoilSpeed) { this.recoilSpeed = recoilSpeed; }
        public void setDamageModifier(int damageModifier) { this.damageModifier = damageModifier; }
        public void setProjectileSpeed(double projectileSpeed) { this.projectileSpeed = projectileSpeed; }
        public void setFireSound(ResourceLocation fireSound) { this.fireSound = fireSound; }
    }

    public static class Ammunition implements INBTSerializable<CompoundTag> {
        private List<AmmoType> acceptedAmmo = new ArrayList<>();
        @Optional
        private float casingEjectChance = 0.65F;

        public static class AmmoType implements INBTSerializable<CompoundTag> {
            private ResourceLocation item;
            private ResourceLocation bulletType;
            @Optional
            private ResourceLocation casingType;
            private double damage = 5.0; // Default damage value
            @Optional
            private float armorPenetration = 0.0F;

            @Override
            public CompoundTag serializeNBT() {
                CompoundTag tag = new CompoundTag();
                tag.putString("Item", this.item.toString());
                tag.putString("BulletType", this.bulletType.toString());
                if (this.casingType != null) {
                    tag.putString("CasingType", this.casingType.toString());
                }
                tag.putDouble("Damage", this.damage);
                tag.putFloat("ArmorPenetration", this.armorPenetration);
                return tag;
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                if (tag.contains("Item", Tag.TAG_STRING)) {
                    this.item = new ResourceLocation(tag.getString("Item"));
                }
                if (tag.contains("BulletType", Tag.TAG_STRING)) {
                    this.bulletType = new ResourceLocation(tag.getString("BulletType"));
                }
                if (tag.contains("CasingType", Tag.TAG_STRING)) {
                    this.casingType = new ResourceLocation(tag.getString("CasingType"));
                }
                if (tag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
                    this.damage = tag.getDouble("Damage");
                }
                if (tag.contains("ArmorPenetration", Tag.TAG_ANY_NUMERIC)) {
                    this.armorPenetration = tag.getFloat("ArmorPenetration");
                }
            }

            public JsonObject toJsonObject() {
                JsonObject object = new JsonObject();
                object.addProperty("item", this.item.toString());
                object.addProperty("bulletType", this.bulletType.toString());
                if (this.casingType != null) {
                    object.addProperty("casingType", this.casingType.toString());
                }
                object.addProperty("damage", this.damage);
                if (this.armorPenetration != 0.0F) {
                    object.addProperty("armorPenetration", this.armorPenetration);
                }
                return object;
            }

            public AmmoType copy() {
                AmmoType ammo = new AmmoType();
                ammo.item = this.item;
                ammo.bulletType = this.bulletType;
                ammo.casingType = this.casingType;
                ammo.damage = this.damage;
                ammo.armorPenetration = this.armorPenetration;
                return ammo;
            }

            @Nullable
            public Item getItem() {
                return ForgeRegistries.ITEMS.getValue(this.item);
            }

            public ResourceLocation getBulletType() { return this.bulletType; }
            @Nullable
            public ResourceLocation getCasingType() { return this.casingType; }
            public double getDamage() { return this.damage; }
            public float getArmorPenetration() { return this.armorPenetration; }

            public void setItem(ResourceLocation item) { this.item = item; }
            public void setBulletType(ResourceLocation bulletType) { this.bulletType = bulletType; }
            public void setCasingType(ResourceLocation casingType) { this.casingType = casingType; }
            public void setDamage(double damage) { this.damage = damage; }
            public void setArmorPenetration(float armorPenetration) { this.armorPenetration = armorPenetration; }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            CompoundTag ammoTag = new CompoundTag();
            for (int i = 0; i < this.acceptedAmmo.size(); i++) {
                ammoTag.put("Ammo" + i, this.acceptedAmmo.get(i).serializeNBT());
            }
            tag.put("AcceptedAmmo", ammoTag);
            tag.putFloat("CasingEjectChance", this.casingEjectChance);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("AcceptedAmmo", Tag.TAG_COMPOUND)) {
                CompoundTag ammoTag = tag.getCompound("AcceptedAmmo");
                this.acceptedAmmo.clear();
                int i = 0;
                while (ammoTag.contains("Ammo" + i)) {
                    AmmoType ammo = new AmmoType();
                    ammo.deserializeNBT(ammoTag.getCompound("Ammo" + i));
                    this.acceptedAmmo.add(ammo);
                    i++;
                }
            }
            if (tag.contains("CasingEjectChance", Tag.TAG_ANY_NUMERIC)) {
                this.casingEjectChance = tag.getFloat("CasingEjectChance");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (!this.acceptedAmmo.isEmpty()) {
                com.google.gson.JsonArray ammoArray = new com.google.gson.JsonArray();
                for (AmmoType ammo : this.acceptedAmmo) {
                    ammoArray.add(ammo.toJsonObject());
                }
                object.add("acceptedAmmo", ammoArray);
            }
            if (this.casingEjectChance != 0.65F) {
                object.addProperty("casingEjectChance", this.casingEjectChance);
            }
            return object;
        }

        public Ammunition copy() {
            Ammunition ammunition = new Ammunition();
            for (AmmoType ammo : this.acceptedAmmo) {
                ammunition.acceptedAmmo.add(ammo.copy());
            }
            ammunition.casingEjectChance = this.casingEjectChance;
            return ammunition;
        }

        public List<AmmoType> getAcceptedAmmo() { return this.acceptedAmmo; }
        public float getCasingEjectChance() { return this.casingEjectChance; }
        public void clearAcceptedAmmo() { this.acceptedAmmo.clear(); }
        public void addAmmoType(AmmoType ammo) { this.acceptedAmmo.add(ammo); }
        public void setCasingEjectChance(float casingEjectChance) { this.casingEjectChance = casingEjectChance; }
    }

    public static class Behavior implements INBTSerializable<CompoundTag> {
        private float restingYaw = 0.0F;
        private float restingPitch = -30.0F;
        private int disableTime = 200;
        @Optional
        private boolean hasOpenAnimation = false;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("RestingYaw", this.restingYaw);
            tag.putFloat("RestingPitch", this.restingPitch);
            tag.putInt("DisableTime", this.disableTime);
            tag.putBoolean("HasOpenAnimation", this.hasOpenAnimation);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("RestingYaw", Tag.TAG_ANY_NUMERIC)) {
                this.restingYaw = tag.getFloat("RestingYaw");
            }
            if (tag.contains("RestingPitch", Tag.TAG_ANY_NUMERIC)) {
                this.restingPitch = tag.getFloat("RestingPitch");
            }
            if (tag.contains("DisableTime", Tag.TAG_ANY_NUMERIC)) {
                this.disableTime = tag.getInt("DisableTime");
            }
            if (tag.contains("HasOpenAnimation", Tag.TAG_ANY_NUMERIC)) {
                this.hasOpenAnimation = tag.getBoolean("HasOpenAnimation");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.restingYaw != 0.0F) object.addProperty("restingYaw", this.restingYaw);
            if (this.restingPitch != -30.0F) object.addProperty("restingPitch", this.restingPitch);
            if (this.disableTime != 200) object.addProperty("disableTime", this.disableTime);
            if (this.hasOpenAnimation) object.addProperty("hasOpenAnimation", true);
            return object;
        }

        public Behavior copy() {
            Behavior behavior = new Behavior();
            behavior.restingYaw = this.restingYaw;
            behavior.restingPitch = this.restingPitch;
            behavior.disableTime = this.disableTime;
            behavior.hasOpenAnimation = this.hasOpenAnimation;
            return behavior;
        }

        public float getRestingYaw() { return this.restingYaw; }
        public float getRestingPitch() { return this.restingPitch; }
        public int getDisableTime() { return this.disableTime; }
        public boolean hasOpenAnimation() { return this.hasOpenAnimation; }

        public void setRestingYaw(float restingYaw) { this.restingYaw = restingYaw; }
        public void setRestingPitch(float restingPitch) { this.restingPitch = restingPitch; }
        public void setDisableTime(int disableTime) { this.disableTime = disableTime; }
        public void setHasOpenAnimation(boolean hasOpenAnimation) { this.hasOpenAnimation = hasOpenAnimation; }
    }

    public static class Display implements INBTSerializable<CompoundTag> {
        @Optional
        private double muzzleLength = 1.0;
        @Optional
        private double muzzleOffsetY = 1.4;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("MuzzleLength", this.muzzleLength);
            tag.putDouble("MuzzleOffsetY", this.muzzleOffsetY);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("MuzzleLength", Tag.TAG_ANY_NUMERIC)) {
                this.muzzleLength = tag.getDouble("MuzzleLength");
            }
            if (tag.contains("MuzzleOffsetY", Tag.TAG_ANY_NUMERIC)) {
                this.muzzleOffsetY = tag.getDouble("MuzzleOffsetY");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.muzzleLength != 1.0) object.addProperty("muzzleLength", this.muzzleLength);
            if (this.muzzleOffsetY != 1.4) object.addProperty("muzzleOffsetY", this.muzzleOffsetY);
            return object;
        }

        public Display copy() {
            Display display = new Display();
            display.muzzleLength = this.muzzleLength;
            display.muzzleOffsetY = this.muzzleOffsetY;
            return display;
        }

        public double getMuzzleLength() { return this.muzzleLength; }
        public double getMuzzleOffsetY() { return this.muzzleOffsetY; }

        public void setMuzzleLength(double muzzleLength) { this.muzzleLength = muzzleLength; }
        public void setMuzzleOffsetY(double muzzleOffsetY) { this.muzzleOffsetY = muzzleOffsetY; }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Targeting", this.targeting.serializeNBT());
        tag.put("Combat", this.combat.serializeNBT());
        tag.put("Ammunition", this.ammunition.serializeNBT());
        tag.put("Behavior", this.behavior.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Targeting", Tag.TAG_COMPOUND)) {
            this.targeting.deserializeNBT(tag.getCompound("Targeting"));
        }
        if (tag.contains("Combat", Tag.TAG_COMPOUND)) {
            this.combat.deserializeNBT(tag.getCompound("Combat"));
        }
        if (tag.contains("Ammunition", Tag.TAG_COMPOUND)) {
            this.ammunition.deserializeNBT(tag.getCompound("Ammunition"));
        }
        if (tag.contains("Behavior", Tag.TAG_COMPOUND)) {
            this.behavior.deserializeNBT(tag.getCompound("Behavior"));
        }
        if (tag.contains("Display", Tag.TAG_COMPOUND)) {
            this.display.deserializeNBT(tag.getCompound("Display"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.add("targeting", this.targeting.toJsonObject());
        object.add("combat", this.combat.toJsonObject());
        object.add("ammunition", this.ammunition.toJsonObject());
        object.add("behavior", this.behavior.toJsonObject());
        object.add("display", this.display.toJsonObject());
        return object;
    }

    public static Turret create(CompoundTag tag) {
        Turret turret = new Turret();
        turret.deserializeNBT(tag);
        return turret;
    }

    public Turret copy() {
        Turret turret = new Turret();
        turret.targeting = this.targeting.copy();
        turret.combat = this.combat.copy();
        turret.ammunition = this.ammunition.copy();
        turret.behavior = this.behavior.copy();
        turret.display = this.display.copy();
        return turret;
    }
}