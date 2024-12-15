package top.ribs.scguns.common.headshot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.interfaces.IHeadshotBox;

import javax.annotation.Nullable;

import static top.ribs.scguns.ScorchedGuns.LOGGER;

public class DynamicHeadshotBox<T extends LivingEntity> implements IHeadshotBox<T> {
    private static final double DEFAULT_HEAD_SCALE = 0.35;
    @Nullable
    @Override
    public AABB getHeadshotBox(T entity) {
        if (entity == null) return null;
        double entityWidth = entity.getBbWidth();
        double eyeHeight = entity.getEyeHeight();
        double headWidth = entityWidth * DEFAULT_HEAD_SCALE * 16.0;

        double halfWidth = headWidth / 2.0;
        AABB headBox = new AABB(
                -halfWidth * 0.0625,
                0,
                -halfWidth * 0.0625,
                halfWidth * 0.0625,
                headWidth * 0.0625,
                halfWidth * 0.0625
        );

        headBox = headBox.move(0, eyeHeight - (headWidth * 0.0625), 0);
        if (entity.isBaby()) {
            double babyScale = 0.75;
            headBox = new AABB(
                    headBox.minX * babyScale,
                    headBox.minY * babyScale,
                    headBox.minZ * babyScale,
                    headBox.maxX * babyScale,
                    headBox.maxY * babyScale,
                    headBox.maxZ * babyScale
            );
        }
        return headBox;
    }
}