package me.ramidzkh.mekae2.ae2;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.behaviors.GenericInternalInventoryAdapters;
import ae2.api.behaviors.GenericSlotCapacities;
import ae2.api.stacks.AEKeyType;
import ae2.api.stacks.AEKeyTypes;
import me.ramidzkh.mekae2.AppliedMekanistics;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraftforge.common.capabilities.Capability;

@SuppressWarnings("UnstableApiUsage")
public final class AMStorageIntegration {

    private AMStorageIntegration() {
    }

    public static void init() {
        AEKeyTypes.register(MekanismKeyType.TYPE);
        registerGasCapacity();
        registerGenericGasStorageAdapter();
        ContainerItemStrategy.register(MekanismKeyType.TYPE, MekanismKey.class, new ChemicalContainerItemStrategy());
    }

    private static void registerGasCapacity() {
        var fluidCapacity = GenericSlotCapacities.getMap().getOrDefault(AEKeyType.fluids(), 0);
        if (fluidCapacity != 0) {
            GenericSlotCapacities.register(MekanismKeyType.TYPE, fluidCapacity);
        }
    }

    private static void registerGenericGasStorageAdapter() {
        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability != null) {
            GenericInternalInventoryAdapters.register(gasCapability, GenericStackChemicalStorage::new);
        } else {
            AppliedMekanistics.LOGGER.warn("Mekanism gas capability is not available during AppMek storage setup");
        }
    }
}
