package me.ramidzkh.mekae2.ae2;

import java.util.Objects;
import java.util.stream.Stream;

import ae2.api.stacks.AEFluidKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.AEKeyType;
import me.ramidzkh.mekae2.AMText;
import me.ramidzkh.mekae2.AppliedMekanistics;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public final class MekanismKeyType extends AEKeyType {

    public static final AEKeyType TYPE = new MekanismKeyType();

    private MekanismKeyType() {
        super(AppliedMekanistics.id("gas"), MekanismKey.class, AMText.GASES.text());
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
    public MekanismKey readFromPacket(PacketBuffer input) {
        Objects.requireNonNull(input);
        return MekanismKey.fromPacket(input);
    }

    @Override
    public AEKey loadKeyFromTag(NBTTagCompound tag) {
        return MekanismKey.fromTag(tag);
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
