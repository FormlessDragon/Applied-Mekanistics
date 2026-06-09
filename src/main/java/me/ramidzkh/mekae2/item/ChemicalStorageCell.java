package me.ramidzkh.mekae2.item;

import ae2.api.stacks.AEKey;
import ae2.items.storage.BasicStorageCell;
import ae2.items.storage.StorageTier;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;
import net.minecraft.item.ItemStack;

public class ChemicalStorageCell extends BasicStorageCell {

    public ChemicalStorageCell(StorageTier tier) {
        super(tier.idleDrain(), tier.bytes() / 1024, tier.bytes() / 128, 5, MekanismKeyType.TYPE);
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        return !(requestedAddition instanceof MekanismKey);
    }
}
