package me.ramidzkh.mekae2.ae2.stack;

import ae2.api.behaviors.StackImportStrategy;
import ae2.api.behaviors.StackTransferContext;
import ae2.api.config.Actionable;
import ae2.core.AELog;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import me.ramidzkh.mekae2.ae2.AEGasKey;
import me.ramidzkh.mekae2.ae2.AEGasKeyType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

@SuppressWarnings("UnstableApiUsage")
public final class MekanismStackImportStrategy implements StackImportStrategy {

    private final WorldServer level;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    public MekanismStackImportStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
        this.level = level;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Override
    public boolean transfer(StackTransferContext context) {
        if (!context.isKeyTypeEnabled(AEGasKeyType.TYPE)) {
            return false;
        }

        IGasHandler handler = getAdjacentHandler();
        if (handler == null) {
            return false;
        }

        long remainingTransferAmount = context.getOperationsRemaining()
                * (long) AEGasKeyType.TYPE.getAmountPerOperation();
        var inv = context.getInternalStorage();

        for (var tank : handler.getTankInfo()) {
            if (tank == null || remainingTransferAmount <= 0) {
                continue;
            }

            GasStack stack = tank.getGas();
            AEGasKey resource = AEGasKey.of(stack);
            if (resource == null || context.isInFilter(resource) == context.isInverted()
                    || !handler.canDrawGas(fromSide, resource.getGas())) {
                continue;
            }

            long amountForThisResource = inv.getInventory().insert(resource, remainingTransferAmount,
                    Actionable.SIMULATE, context.getActionSource());
            GasStack extractedStack = handler.drawGas(fromSide, clampToInt(amountForThisResource), true);
            if (extractedStack == null || extractedStack.amount <= 0) {
                continue;
            }

            long inserted = inv.getInventory().insert(resource, extractedStack.amount, Actionable.MODULATE,
                    context.getActionSource());

            if (inserted < extractedStack.amount) {
                long leftover = extractedStack.amount - inserted;
                leftover -= handler.receiveGas(fromSide, resource.toStack(leftover), true);

                if (leftover > 0) {
                    AELog.warn("Extracted %dx%s from adjacent gas storage and voided it because network refused insert",
                            leftover, resource);
                }
            }

            long opsUsed = Math.max(1, inserted / AEGasKeyType.TYPE.getAmountPerOperation());
            context.reduceOperationsRemaining(opsUsed);
            remainingTransferAmount -= inserted;
        }

        return context.hasDoneWork();
    }

    private IGasHandler getAdjacentHandler() {
        TileEntity tile = level.getTileEntity(fromPos);
        return tile == null ? null : tile.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, fromSide);
    }

    private static int clampToInt(long amount) {
        if (amount <= 0) {
            return 0;
        }
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
