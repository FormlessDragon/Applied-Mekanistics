package me.ramidzkh.mekae2.ae2.stack;

import ae2.api.behaviors.ExternalStorageStrategy;
import ae2.api.storage.MEStorage;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.Nullable;

public final class MekanismExternalStorageStrategy implements ExternalStorageStrategy {

    private final WorldServer level;
    private final BlockPos fromPos;
    private final EnumFacing fromSide;

    public MekanismExternalStorageStrategy(WorldServer level, BlockPos fromPos, EnumFacing fromSide) {
        this.level = level;
        this.fromPos = fromPos;
        this.fromSide = fromSide;
    }

    @Nullable
    @Override
    public MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback) {
        IGasHandler storage = getAdjacentHandler();
        if (storage == null) {
            return null;
        }

        return new ChemicalHandlerFacade(storage, fromSide, extractableOnly, injectOrExtractCallback);
    }

    private IGasHandler getAdjacentHandler() {
        TileEntity tile = level.getTileEntity(fromPos);
        return tile == null ? null : tile.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, fromSide);
    }
}
