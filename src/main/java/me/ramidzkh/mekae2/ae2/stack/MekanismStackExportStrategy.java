package me.ramidzkh.mekae2.ae2.stack;

import ae2.api.behaviors.StackExportStrategy;
import ae2.api.behaviors.StackTransferContext;
import ae2.api.config.Actionable;
import ae2.api.stacks.AEKey;
import ae2.api.storage.StorageHelper;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MekanismStackExportStrategy implements StackExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(MekanismStackExportStrategy.class);

    private final WorldServer level;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    public MekanismStackExportStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
        this.level = level;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Override
    public long transfer(StackTransferContext context, AEKey what, long amount) {
        if (!(what instanceof MekanismKey mekanismKey)) {
            return 0;
        }

        IGasHandler storage = getAdjacentHandler();
        if (storage == null) {
            return 0;
        }

        var inv = context.getInternalStorage();
        long extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv.getInventory(), what, amount,
                context.getActionSource(), Actionable.SIMULATE);
        long wasInserted = storage.receiveGas(fromSide, mekanismKey.toStack(extracted), false);

        if (wasInserted > 0) {
            extracted = StorageHelper.poweredExtraction(context.getEnergySource(), inv.getInventory(), what,
                    wasInserted, context.getActionSource(), Actionable.MODULATE);
            wasInserted = storage.receiveGas(fromSide, mekanismKey.toStack(extracted), true);

            if (wasInserted < extracted) {
                long leftover = extracted - wasInserted;
                leftover -= inv.getInventory().insert(what, leftover, Actionable.MODULATE, context.getActionSource());

                if (leftover > 0) {
                    LOGGER.error("Storage export: adjacent gas handler unexpectedly refused insert, voided {}x{}",
                            leftover, what);
                }
            }
        }

        return wasInserted;
    }

    @Override
    public long push(AEKey what, long amount, Actionable mode) {
        if (!(what instanceof MekanismKey mekanismKey)) {
            return 0;
        }

        IGasHandler storage = getAdjacentHandler();
        if (storage == null) {
            return 0;
        }

        return storage.receiveGas(fromSide, mekanismKey.toStack(amount), mode == Actionable.MODULATE);
    }

    private IGasHandler getAdjacentHandler() {
        TileEntity tile = level.getTileEntity(fromPos);
        return tile == null ? null : tile.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, fromSide);
    }
}
