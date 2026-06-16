package me.ramidzkh.mekae2.ae2.stack;

import ae2.api.config.Actionable;
import ae2.api.networking.security.IActionSource;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.MEStorage;
import ae2.core.localization.GuiText;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import me.ramidzkh.mekae2.util.AMText;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public record GasHandlerFacade(IGasHandler handler, EnumFacing side, boolean extractableOnly,
        Runnable changeListener) implements MEStorage {

    private static final long MAX_REPORTED_AMOUNT = 1L << 42;

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof MekanismKey key) || amount <= 0) {
            return 0;
        }

        int inserted = handler.receiveGas(side, key.toStack(amount), mode == Actionable.MODULATE);

        if (inserted > 0 && mode == Actionable.MODULATE) {
            this.changeListener.run();
        }

        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof MekanismKey key) || amount <= 0 || !handler.canDrawGas(side, key.getGas())) {
            return 0;
        }

        GasStack extracted = handler.drawGas(side, clampToInt(amount), mode == Actionable.MODULATE);
        if (extracted == null || !extracted.isGasEqual(key.toStack(1))) {
            return 0;
        }

        if (extracted.amount > 0 && mode == Actionable.MODULATE) {
            this.changeListener.run();
        }

        return extracted.amount;
    }

    @Override
    public ITextComponent getDescription() {
        return new TextComponentTranslation(GuiText.ExternalStorage.getTranslationKey(), AMText.GASES.text());
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var tank : handler.getTankInfo()) {
            if (tank == null) {
                continue;
            }

            GasStack stack = tank.getGas();
            MekanismKey key = MekanismKey.of(stack);
            if (key == null) {
                continue;
            }

            if (extractableOnly && !handler.canDrawGas(side, key.getGas())) {
                continue;
            }

            out.add(key, Math.min(stack.amount, MAX_REPORTED_AMOUNT));
        }
    }

    private static int clampToInt(long amount) {
        if (amount <= 0) {
            return 0;
        }
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
