package top.ribs.scguns.common;



public class ChargeHandler {
    private static int chargeTime = 0;

    // Get the current charge time
    public static int getChargeTime() {
        return chargeTime;
    }

    // Update charge time based on whether the weapon is charging
    public static void updateChargeTime(int maxChargeTime, boolean isCharging) {
        if (isCharging) {
            chargeTime++;
            if (chargeTime > maxChargeTime) {
                chargeTime = maxChargeTime; // Ensure chargeTime does not exceed maxChargeTime
            }
        } else {
            chargeTime = 0; // Reset chargeTime when not charging
        }
    }
}

