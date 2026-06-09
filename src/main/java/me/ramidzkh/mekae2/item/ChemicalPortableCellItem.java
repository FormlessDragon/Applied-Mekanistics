package me.ramidzkh.mekae2.item;

import java.util.Objects;

import ae2.api.stacks.AEKey;
import ae2.container.GuiIds;
import ae2.items.storage.StorageTier;
import ae2.items.tools.powered.PortableCellItem;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ChemicalPortableCellItem extends PortableCellItem {

    public ChemicalPortableCellItem(int totalTypes, StorageTier tier, double powerCapacity, int defaultColor) {
        super(MekanismKeyType.TYPE, totalTypes, GuiIds.GuiKey.PORTABLE_FLUID_CELL, tier, powerCapacity, defaultColor);
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, AEKey requestedAddition) {
        return !(requestedAddition instanceof MekanismKey);
    }

    @Override
    public ResourceLocation getRecipeId() {
        return Objects.requireNonNull(getRegistryName());
    }
}
