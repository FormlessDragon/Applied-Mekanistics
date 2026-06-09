package me.ramidzkh.mekae2.ae2;

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
import org.jetbrains.annotations.Nullable;

public class ChemicalP2PTunnelPart extends CapabilityP2PTunnelPart<ChemicalP2PTunnelPart, IGasHandler> {

    private static final P2PModels MODELS = new P2PModels(AppliedMekanistics.id("part/chemical_p2p_tunnel"));
    private static final IGasHandler NULL_GAS_HANDLER = new NullGasHandler();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public ChemicalP2PTunnelPart(IPartItem<?> partItem) {
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
            int amount = stack == null ? 0 : stack.amount;

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

                    int sent = ChemicalP2PTunnelPart.receiveGas(stack, adjacentSide(target), doTransfer, output,
                        toSend);
                    overflow = toSend - sent;
                    total += sent;
                }
            }

            if (doTransfer) {
                deductTransportCost(total, MekanismKeyType.TYPE);
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
        public GasTankInfo[] getTankInfo() {
            return IGasHandler.NONE;
        }
    }

    static int distributeGas(GasStack stack, boolean doTransfer, List<? extends IGasHandler> outputs) {
        int outputTunnels = outputs.size();
        int amount = stack == null ? 0 : stack.amount;

        if (outputTunnels == 0 || amount <= 0) {
            return 0;
        }

        int amountPerOutput = amount / outputTunnels;
        int overflow = amount % outputTunnels;
        int total = 0;

        for (IGasHandler output : outputs) {
            int toSend = amountPerOutput + overflow;
            if (toSend <= 0) {
                break;
            }

            int sent = receiveGas(stack, null, doTransfer, output, toSend);
            overflow = toSend - sent;
            total += sent;
        }

        return total;
    }

    private static int receiveGas(GasStack stack, EnumFacing side, boolean doTransfer, IGasHandler output, int amount) {
        GasStack split = stack.copy();
        split.amount = amount;
        return output.receiveGas(side, split, doTransfer);
    }

    private static EnumFacing adjacentSide(ChemicalP2PTunnelPart output) {
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
            try (var input = getInputCapability()) {
                var result = input.get().drawGas(side, amount, doTransfer);

                if (doTransfer && result != null) {
                    deductTransportCost(result.amount, MekanismKeyType.TYPE);
                }

                return result;
            }
        }

        @Override
        public boolean canReceiveGas(EnumFacing side, Gas gas) {
            return false;
        }

        @Override
        public boolean canDrawGas(EnumFacing side, Gas gas) {
            try (var input = getInputCapability()) {
                return input.get().canDrawGas(side, gas);
            }
        }

        @Override
        public GasTankInfo[] getTankInfo() {
            try (var input = getInputCapability()) {
                return input.get().getTankInfo();
            }
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
        public GasTankInfo[] getTankInfo() {
            return IGasHandler.NONE;
        }
    }
}
