package me.ramidzkh.mekae2.item;

import ae2.api.stacks.AEKey;
import ae2.items.storage.BasicStorageCell;
import ae2.items.storage.StorageTier;
import me.ramidzkh.mekae2.ae2.AEGasKey;
import me.ramidzkh.mekae2.ae2.AEGasKeyType;
import net.minecraft.item.ItemStack;

public class GasStorageCell extends BasicStorageCell {

    public GasStorageCell(StorageTier tier) {
        super(tier.idleDrain(), tier.bytes() / 1024, tier.bytes() / 128, 5, AEGasKeyType.TYPE);
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        return !(requestedAddition instanceof AEGasKey);
    }
}
