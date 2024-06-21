package top.ribs.scguns.client.handler;



public class ChargeHandler {
    private static int chargeTime = 0;

    public static void setChargeTime(int time) {
        chargeTime = time;
    }

    public static int getChargeTime() {
        return chargeTime;
    }

    public static void updateChargeTime(int gun, boolean isCharging) {
        if (isCharging) {
            chargeTime++;
            if (chargeTime > gun) {
                chargeTime = gun;
            }
        } else {
            chargeTime = 0;
        }

    }
}

