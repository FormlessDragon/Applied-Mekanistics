package me.ramidzkh.mekae2.ae2;

import java.util.Objects;
import java.util.stream.Stream;

import ae2.api.stacks.AEFluidKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import me.ramidzkh.mekae2.util.AMText;
import me.ramidzkh.mekae2.AppliedMekanistics;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public final class AEGasKeyType extends AEKeyType {

    public static final AEKeyType TYPE = new AEGasKeyType();

    private AEGasKeyType() {
        super(AppliedMekanistics.id("gas"), AEGasKey.class, AMText.GASES.text());
    }

    @Override
    public int getAmountPerOperation() {
        return AEFluidKey.AMOUNT_BUCKET * 125 / 1000;
    }

    @Override
    public int getAmountPerByte() {
        return 8 * AEFluidKey.AMOUNT_BUCKET;
    }

    @Override
    public AEGasKey readFromPacket(PacketBuffer input) {
        Objects.requireNonNull(input);
        return AEGasKey.fromPacket(input);
    }

    @Override
    public AEKey loadKeyFromTag(NBTTagCompound tag) {
        return AEGasKey.fromTag(tag);
    }

    @Override
    public int getAmountPerUnit() {
        return AEFluidKey.AMOUNT_BUCKET;
    }

    @Override
    public String getUnitSymbol() {
        return "B";
    }

    @Override
    public Stream<String> getTagNames() {
        return Stream.empty();
    }
}
