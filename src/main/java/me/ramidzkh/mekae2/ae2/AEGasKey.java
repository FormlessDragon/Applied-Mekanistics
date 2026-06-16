package me.ramidzkh.mekae2.ae2;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import ae2.core.AELog;
import com.google.common.base.Preconditions;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.Nullable;

public final class AEGasKey extends AEKey {

    private final GasStack stack;
    private final int hashCode;

    private AEGasKey(GasStack stack) {
        Preconditions.checkArgument(stack != null && stack.getGas() != null && stack.amount > 0, "stack was empty");
        this.stack = stack.copy();
        this.stack.amount = 1;
        this.hashCode = hashStack(this.stack);
    }

    @Nullable
    public static AEGasKey of(@Nullable GasStack stack) {
        if (stack == null || stack.getGas() == null || stack.amount <= 0) {
            return null;
        }
        return new AEGasKey(stack);
    }

    @Nullable
    public static AEGasKey of(@Nullable Gas gas) {
        return gas == null ? null : of(new GasStack(gas, 1));
    }

    @Nullable
    public static AEGasKey fromTag(NBTTagCompound tag) {
        try {
            return of(GasStack.readFromNBT(tag));
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid Mekanism gas key from NBT: %s", tag, e);
            return null;
        }
    }

    public static AEGasKey fromPacket(PacketBuffer data) {
        try {
            NBTTagCompound tag = data.readCompoundTag();
            return tag == null ? null : fromTag(tag);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read Mekanism gas key", e);
        }
    }

    public GasStack toStack(long amount) {
        return this.stack.copy().withAmount(Math.clamp(amount, 0, Integer.MAX_VALUE));
    }

    public Gas getGas() {
        return this.stack.getGas();
    }

    @Override
    public AEKeyType getType() {
        return AEGasKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return of(getGas());
    }

    @Override
    public NBTTagCompound toTag() {
        return this.stack.write(new NBTTagCompound());
    }

    @Override
    public Object getPrimaryKey() {
        return getGas();
    }

    @Override
    public String getModId() {
        return "mekanism";
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("mekanism", getGas().getName());
    }

    @Override
    public void writeToPacket(PacketBuffer data) {
        data.writeCompoundTag(toTag());
    }

    @Override
    public GasStack getReadOnlyStack() {
        return this.stack;
    }

    @Override
    protected ITextComponent computeDisplayName() {
        String name = getGas().getLocalizedName();
        return new TextComponentString(name == null || name.isEmpty() ? getGas().getName() : name);
    }

    @Override
    public boolean isTagged(String tag) {
        return false;
    }

    @Override
    public @Nullable NBTBase get(String componentId) {
        return null;
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AEGasKey that = (AEGasKey) o;
        return this.hashCode == that.hashCode && matches(that.stack);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return getId().toString();
    }

    private static int hashStack(GasStack stack) {
        return stack.getGas().hashCode();
    }

    private boolean matches(GasStack variant) {
        return variant != null && this.stack.isGasEqual(variant);
    }

}
