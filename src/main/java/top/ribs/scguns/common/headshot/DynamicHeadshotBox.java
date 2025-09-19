package top.ribs.scguns.common.headshot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.interfaces.IHeadshotBox;

import javax.annotation.Nullable;

import static top.ribs.scguns.ScorchedGuns.LOGGER;

public class DynamicHeadshotBox<T extends LivingEntity> implements IHeadshotBox<T> {
    private static final double DEFAULT_HEAD_SCALE = 0.6;
    private static final double MIN_HEAD_SIZE = 6.0;
    private static final double MAX_HEAD_SIZE = 12.0;

    @Nullable
    @Override
    public AABB getHeadshotBox(T entity) {
        if (entity == null) return null;

        double entityWidth = entity.getBbWidth();
        double entityHeight = entity.getBbHeight();
        double eyeHeight = entity.getEyeHeight();
        double baseHeadSize = Math.max(entityWidth, entityHeight * 0.25) * DEFAULT_HEAD_SCALE;

        double headSizePixels = baseHeadSize * 16.0;

        headSizePixels = Math.max(MIN_HEAD_SIZE, Math.min(MAX_HEAD_SIZE, headSizePixels));

        return getHeadBox(entity, headSizePixels, eyeHeight);
    }

    private static <T extends LivingEntity> @NotNull AABB getHeadBox(T entity, double headSizePixels, double eyeHeight) {
        double headSize = headSizePixels * 0.0625;
        double halfHeadSize = headSize / 2.0;

        AABB headBox = new AABB(
                -halfHeadSize,
                0,
                -halfHeadSize,
                halfHeadSize,
                headSize,
                halfHeadSize
        );

        double headBottom = eyeHeight - (headSize * 0.2);

        headBox = headBox.move(0, headBottom, 0);

        if (entity.isBaby()) {
            double babyScale = 0.75;
            Vec3 center = headBox.getCenter();
            headBox = new AABB(
                    center.x - (halfHeadSize * babyScale),
                    center.y - (headSize * babyScale * 0.5),
                    center.z - (halfHeadSize * babyScale),
                    center.x + (halfHeadSize * babyScale),
                    center.y + (headSize * babyScale * 0.5),
                    center.z + (halfHeadSize * babyScale)
            );
        }
        return headBox;
    }
}