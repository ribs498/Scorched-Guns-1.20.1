package top.ribs.scguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import javax.annotation.Nullable;

public class ClientUtil {
    public static LocalPlayer getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static ClientLevel getClientWorld() {
        return Minecraft.getInstance().level;
    }

    public static boolean isPaused(){
        return Minecraft.getInstance().isPaused();
    }

    public static float getPartialTick(){
        return Minecraft.getInstance().getPartialTick();
    }

    public static void setScreen(@Nullable Screen pGuiScreen) {
        Minecraft.getInstance().setScreen(pGuiScreen);
    }
}
