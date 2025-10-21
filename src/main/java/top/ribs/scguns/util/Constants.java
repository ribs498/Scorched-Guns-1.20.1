package top.ribs.scguns.util;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

public class Constants {

    public static final Rarity OCEANIC = Rarity.create("scguns.oceanic", ChatFormatting.BLUE);
    public static final Rarity UNIQUE = Rarity.create("scguns.unique", ChatFormatting.GREEN);
    public static final Rarity PIGLISH = Rarity.create("scguns.piglish", ChatFormatting.GOLD);
    public static final Rarity SCORCHED = Rarity.create("scguns.scorched", ChatFormatting.RED);
    public static final Rarity DEEP_DARK = Rarity.create("scguns.deep_dark", ChatFormatting.DARK_AQUA);
    public static final Rarity ENDISH = Rarity.create("scguns.endish", ChatFormatting.DARK_PURPLE);
    public static final Rarity BIZARRE = Rarity.create("scguns.bizarre", ChatFormatting.GRAY);
    public static final Rarity TREATED_BRASS = Rarity.create("scguns.treated_brass", ChatFormatting.YELLOW);
    public static final Rarity DIAMOND_STEEL = Rarity.create("scguns.diamond_steel", ChatFormatting.AQUA);
    public static final Rarity WRECKER = Rarity.create("scguns.wrecker", ChatFormatting.DARK_GRAY);
    public static final Rarity RUSTY = Rarity.create("scguns.rusty", style -> style.withColor(0xFF8C00));
}

