package me.ramidzkh.mekae2.ae2;

import ae2.api.behaviors.GenericInternalInventory;
import ae2.api.config.Actionable;
import com.google.common.primitives.Ints;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import net.minecraft.util.EnumFacing;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class GenericStackGasStorage implements IGasHandler {

    private final GenericInternalInventory inv;

    public GenericStackGasStorage(GenericInternalInventory inv) {
        this.inv = inv;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        var what = AEGasKey.of(stack);
        if (what == null) {
            return 0;
        }

        int inserted = 0;
        for (int i = 0; i < this.inv.size() && inserted < stack.amount; i++) {
            inserted += Ints.saturatedCast(this.inv.insert(i, what, stack.amount - inserted,
                Actionable.ofSimulate(!doTransfer)));
        }
        return inserted;
    }

    @Override
    @Nullable
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        for (int i = 0; i < this.inv.size(); i++) {
            if (this.inv.getKey(i) instanceof AEGasKey what) {
                return extract(what, amount, doTransfer);
            }
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas gas) {
        var what = AEGasKey.of(gas);
        if (what == null || !this.inv.canInsert()) {
            return false;
        }

        for (int i = 0; i < this.inv.size(); i++) {
            if (this.inv.isAllowedIn(i, what)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas gas) {
        if (!this.inv.canExtract()) {
            return false;
        }

        var what = AEGasKey.of(gas);
        if (what == null) {
            return false;
        }

        for (int i = 0; i < this.inv.size(); i++) {
            if (what.equals(this.inv.getKey(i)) && this.inv.getAmount(i) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        if (!this.inv.isSupportedType(AEGasKeyType.TYPE)) {
            return IGasHandler.NONE;
        }

        GasTankInfo[] info = new GasTankInfo[this.inv.size()];
        for (int i = 0; i < this.inv.size(); i++) {
            final int slot = i;
            info[i] = new GasTankInfo() {
                @Override
                @Nullable
                public GasStack getGas() {
                    if (inv.getKey(slot) instanceof AEGasKey what) {
                        return what.toStack(inv.getAmount(slot));
                    }
                    return null;
                }

                @Override
                public int getStored() {
                    return Ints.saturatedCast(inv.getAmount(slot));
                }

                @Override
                public int getMaxGas() {
                    return Ints.saturatedCast(inv.getCapacity(AEGasKeyType.TYPE));
                }
            };
        }
        return info;
    }

    @Nullable
    private GasStack extract(AEGasKey what, int amount, boolean doTransfer) {
        int extracted = 0;
        for (int i = 0; i < this.inv.size() && extracted < amount; i++) {
            extracted += Ints.saturatedCast(this.inv.extract(i, what, amount - extracted,
                Actionable.ofSimulate(!doTransfer)));
        }

        return extracted > 0 ? what.toStack(extracted) : null;
    }
}
