package me.ramidzkh.mekae2.ae2;

import java.util.List;

import me.ramidzkh.mekae2.AppliedMekanistics;
import me.ramidzkh.mekae2.MekCapabilities;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.CapabilityP2PTunnelPart;
import appeng.parts.p2p.P2PModels;

public class ChemicalP2PTunnelPart extends CapabilityP2PTunnelPart<ChemicalP2PTunnelPart, IChemicalHandler> {

    private static final P2PModels MODELS = new P2PModels(AppliedMekanistics.id("part/chemical_p2p_tunnel"));
    private static final IChemicalHandler NULL_CHEMICAL_HANDLER = new NullChemicalHandler();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public ChemicalP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, MekCapabilities.CHEMICAL.block());
        inputHandler = new InputChemicalHandler();
        outputHandler = new OutputChemicalHandler();
        emptyHandler = NULL_CHEMICAL_HANDLER;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputChemicalHandler implements IChemicalHandler {

        @Override
        public int getChemicalTanks() {
            return 1;
        }

        @Override
        public ChemicalStack getChemicalInTank(int tank) {
            return ChemicalStack.EMPTY;
        }

        @Override
        public void setChemicalInTank(int tank, ChemicalStack stack) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isValid(int tank, ChemicalStack stack) {
            return true;
        }

        @Override
        public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
            var outputTunnels = getOutputs().size();
            var amount = stack.getAmount();

            if (outputTunnels == 0 || stack.isEmpty()) {
                return stack;
            }

            var amountPerOutput = amount / outputTunnels;
            var overflow = amountPerOutput == 0L ? amount : amount % amountPerOutput;
            var total = 0L;

            for (var target : getOutputs()) {
                try (var capabilityGuard = target.getAdjacentCapability()) {
                    var output = capabilityGuard.get();
                    var toSend = amountPerOutput + overflow;

                    overflow = output.insertChemical(stack.copyWithAmount(toSend), action).getAmount();
                    total += toSend - overflow;
                }
            }

            if (action.execute()) {
                deductTransportCost(total, MekanismKeyType.TYPE);
            }

            return stack.copyWithAmount(amount - total);
        }

        @Override
        public ChemicalStack extractChemical(int tank, long amount, Action action) {
            return ChemicalStack.EMPTY;
        }
    }

    private class OutputChemicalHandler implements IChemicalHandler {

        @Override
        public int getChemicalTanks() {
            try (var input = getInputCapability()) {
                return input.get().getChemicalTanks();
            }
        }

        @Override
        public ChemicalStack getChemicalInTank(int tank) {
            try (var input = getInputCapability()) {
                return input.get().getChemicalInTank(tank);
            }
        }

        @Override
        public void setChemicalInTank(int tank, ChemicalStack stack) {
            try (var input = getInputCapability()) {
                input.get().setChemicalInTank(tank, stack);
            }
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            try (var input = getInputCapability()) {
                return input.get().getChemicalTankCapacity(tank);
            }
        }

        @Override
        public boolean isValid(int tank, ChemicalStack stack) {
            try (var input = getInputCapability()) {
                return input.get().isValid(tank, stack);
            }
        }

        @Override
        public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
            return stack;
        }

        @Override
        public ChemicalStack extractChemical(int tank, long amount, Action action) {
            try (var input = getInputCapability()) {
                var result = input.get().extractChemical(tank, amount, action);

                if (action.execute()) {
                    deductTransportCost(result.getAmount(), MekanismKeyType.TYPE);
                }

                return result;
            }
        }
    }

    private static class NullChemicalHandler implements IChemicalHandler {
        @Override
        public int getChemicalTanks() {
            return 0;
        }

        @Override
        public ChemicalStack getChemicalInTank(int tank) {
            return ChemicalStack.EMPTY;
        }

        @Override
        public void setChemicalInTank(int tank, ChemicalStack stack) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isValid(int tank, ChemicalStack stack) {
            return false;
        }

        @Override
        public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
            return stack;
        }

        @Override
        public ChemicalStack extractChemical(int tank, long amount, Action action) {
            return ChemicalStack.EMPTY;
        }
    }
}
