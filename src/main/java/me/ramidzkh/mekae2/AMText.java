package me.ramidzkh.mekae2;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum AMText {
    GASES("gas"),
    CREATIVE_TAB("creative_tab");

    public final String key;

    AMText(String key) {
        this.key = "text." + Reference.MOD_ID + "." + key;
    }

    public ITextComponent text(Object... params) {
        return new TextComponentTranslation(this.key, params);
    }
}
