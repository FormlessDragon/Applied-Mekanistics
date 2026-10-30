package me.ramidzkh.mekae2.ae2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.items.parts.PartModels;
import ae2.parts.p2p.CapabilityP2PTunnelPart;
import ae2.parts.p2p.P2PModels;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import me.ramidzkh.mekae2.AppliedMekanistics;
import net.minecraft.util.EnumFacing;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class GasP2PTunnelPart extends CapabilityP2PTunnelPart<GasP2PTunnelPart, IGasHandler> {

    private static final P2PModels MODELS = new P2PModels(
        AppliedMekanistics.id("part/p2p/p2p_tunnel_gas"),
        AppliedMekanistics.id("part/p2p_tunnel_gas"));
    private static final IGasHandler NULL_GAS_HANDLER = new NullGasHandler();
    private static final GasTankInfo[] INPUT_TANKS = { new GasTankInfo() {
        @Override
        @Nullable
        public GasStack getGas() {
            return null;
        }

        @Override
        public int getStored() {
            return 0;
        }

        @Override
        public int getMaxGas() {
            return Integer.MAX_VALUE;
        }
    } };

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public GasP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.GAS_HANDLER_CAPABILITY);
        this.inputHandler = new InputGasHandler();
        this.outputHandler = new OutputGasHandler();
        this.emptyHandler = NULL_GAS_HANDLER;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputGasHandler implements IGasHandler {
        @Override
        public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
            var outputs = getOutputs();
            int outputTunnels = outputs.size();
            int amount = stack.amount;

            if (outputTunnels == 0 || amount <= 0) {
                return 0;
            }

            int amountPerOutput = amount / outputTunnels;
            int overflow = amount % outputTunnels;
            int total = 0;

            for (var target : outputs) {
                try (var capabilityGuard = target.getAdjacentCapability()) {
                    var output = capabilityGuard.get();
                    int toSend = amountPerOutput + overflow;
                    if (toSend <= 0) {
                        break;
                    }

                    @SuppressWarnings("DataFlowIssue")
                    int sent = GasP2PTunnelPart.receiveGas(stack, adjacentSide(target), doTransfer, output,
                        toSend);
                    overflow = toSend - sent;
                    total += sent;
                }
            }

            if (doTransfer && total > 0) {
                deductTransportCost(total, AEGasKeyType.TYPE);
            }

            return total;
        }

        @Override
        @Nullable
        public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
            return null;
        }

        @Override
        public boolean canReceiveGas(EnumFacing side, Gas gas) {
            return true;
        }

        @Override
        public boolean canDrawGas(EnumFacing side, Gas gas) {
            return false;
        }

        @Override
        @Nonnull
        public GasTankInfo[] getTankInfo() {
            return INPUT_TANKS;
        }
    }

    private static int receiveGas(GasStack stack, EnumFacing side, boolean doTransfer, IGasHandler output, int amount) {
        GasStack split = stack.copy();
        split.amount = amount;
        return output.receiveGas(side, split, doTransfer);
    }

    private static EnumFacing adjacentSide(GasP2PTunnelPart output) {
        EnumFacing side = output.getSide();
        return side == null ? null : side.getOpposite();
    }

    private class OutputGasHandler implements IGasHandler {
        @Override
        public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
            return 0;
        }

        @Override
        @Nullable
        public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
            for (var input : getInputs()) {
                try (var inputCapability = input.getAdjacentCapability()) {
                    var result = inputCapability.get().drawGas(side, amount, doTransfer);
                    if (result == null) {
                        continue;
                    }

                    if (doTransfer) {
                        deductTransportCost(result.amount, AEGasKeyType.TYPE);
                    }

                    return result;
                }
            }

            return null;
        }

        @Override
        public boolean canReceiveGas(EnumFacing side, Gas gas) {
            return false;
        }

        @Override
        public boolean canDrawGas(EnumFacing side, Gas gas) {
            for (var input : getInputs()) {
                try (var inputCapability = input.getAdjacentCapability()) {
                    if (inputCapability.get().canDrawGas(side, gas)) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        @Nonnull
        public GasTankInfo[] getTankInfo() {
            var tanks = new ArrayList<GasTankInfo>();
            for (var input : getInputs()) {
                try (var inputCapability = input.getAdjacentCapability()) {
                    Collections.addAll(tanks, inputCapability.get().getTankInfo());
                }
            }

            return tanks.toArray(new GasTankInfo[0]);
        }
    }

    private static class NullGasHandler implements IGasHandler {
        @Override
        public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
            return 0;
        }

        @Override
        @Nullable
        public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
            return null;
        }

        @Override
        public boolean canReceiveGas(EnumFacing side, Gas gas) {
            return false;
        }

        @Override
        public boolean canDrawGas(EnumFacing side, Gas gas) {
            return false;
        }

        @Override
        @Nonnull
        public GasTankInfo[] getTankInfo() {
            return IGasHandler.NONE;
        }
    }
}
