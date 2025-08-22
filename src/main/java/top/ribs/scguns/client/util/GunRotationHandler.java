package top.ribs.scguns.client.util;

public class GunRotationHandler {
    private static final float ROTATION_LERP_SPEED = 0.6f;

    private float currentCylinderRotation = 0.0f;
    private float currentMagazineRotation = 0.0f;

    private float targetCylinderRotation = 0.0f;
    private float targetMagazineRotation = 0.0f;

    public void updateRotations(float partialTick) {
        currentCylinderRotation = lerpRotation(currentCylinderRotation, targetCylinderRotation, ROTATION_LERP_SPEED);
        currentMagazineRotation = lerpRotation(currentMagazineRotation, targetMagazineRotation, ROTATION_LERP_SPEED);
        currentCylinderRotation = normalizeRotation(currentCylinderRotation);
        currentMagazineRotation = normalizeRotation(currentMagazineRotation);
    }

    public void incrementCylinderRotation(float amount) {
        targetCylinderRotation = normalizeRotation(targetCylinderRotation + amount);
    }

    public void incrementMagazineRotation(float amount) {
        targetMagazineRotation = normalizeRotation(targetMagazineRotation + amount);
    }

    public float getCurrentCylinderRotation() {
        return currentCylinderRotation;
    }

    public float getCurrentMagazineRotation() {
        return currentMagazineRotation;
    }

    private float lerpRotation(float current, float target, float speed) {
        float shortestDistance = ((((target - current) % 360) + 540) % 360) - 180;
        return current + shortestDistance * speed;
    }

    private float normalizeRotation(float rotation) {
        rotation = rotation % 360;
        if (rotation < 0) {
            rotation += 360;
        }
        return rotation;
    }
}