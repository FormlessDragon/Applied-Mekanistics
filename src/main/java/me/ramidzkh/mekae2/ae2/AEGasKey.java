package me.ramidzkh.mekae2.ae2;

import java.util.List;
import java.util.Objects;

import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import ae2.core.AELog;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class AEGasKey extends AEKey {

    private final Gas gas;
    private final int hashCode;

    private AEGasKey(Gas gas) {
        this.gas = Objects.requireNonNull(gas, "gas");
        this.hashCode = gas.hashCode();
    }

    @Nullable
    public static AEGasKey of(@Nullable GasStack stack) {
        if (stack == null || stack.getGas() == null || stack.amount <= 0) {
            return null;
        }
        return new AEGasKey(stack.getGas());
    }

    @Nullable
    public static AEGasKey of(@Nullable Gas gas) {
        return gas == null ? null : new AEGasKey(gas);
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
            return fromTag(data.readCompoundTag());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read Mekanism gas key", e);
        }
    }

    public GasStack toStack(long amount) {
        return new GasStack(this.gas, clampToInt(amount));
    }

    public Gas getGas() {
        return this.gas;
    }

    @Override
    public AEKeyType getType() {
        return AEGasKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public NBTTagCompound toTag() {
        return toStack(1).write(new NBTTagCompound());
    }

    @Override
    public Object getPrimaryKey() {
        return this.gas;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("mekanism", this.gas.getName());
    }

    @Override
    public void writeToPacket(PacketBuffer data) {
        data.writeCompoundTag(toTag());
    }

    @Override
    public GasStack getReadOnlyStack() {
        return toStack(1);
    }

    @Override
    protected ITextComponent computeDisplayName() {
        String name = this.gas.getLocalizedName();
        return new TextComponentString(name == null || name.isEmpty() ? this.gas.getName() : name);
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, World level, BlockPos pos) {
        // Gases are voided when their container is broken.
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
        return this == o || o instanceof AEGasKey that && this.gas == that.gas;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "MekanismKey{" + this.gas.getName() + '}';
    }

    private static int clampToInt(long amount) {
        if (amount <= 0) {
            return 0;
        }
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
