package me.ramidzkh.mekae2.ae2;

import ae2.api.behaviors.GenericInternalInventoryAdapters;
import ae2.api.parts.RegisterPartCapabilitiesEvent;
import ae2.api.parts.RegisterPartCapabilitiesEventInternal;
import ae2.parts.crafting.PatternProviderPart;
import ae2.parts.misc.InterfacePart;
import ae2.tile.networking.TileCableBus;
import me.ramidzkh.mekae2.AppliedMekanistics;
import me.ramidzkh.mekae2.Tags;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("UnstableApiUsage")
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class AMPartCapabilities {

    private static boolean registered;

    private AMPartCapabilities() {
    }

    public static synchronized void init() {
        if (registered) {
            return;
        }

        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability == null) {
            AppliedMekanistics.LOGGER.warn("Mekanism gas capability is not available during AppMek part capability registration");
            return;
        }

        RegisterPartCapabilitiesEvent event = new RegisterPartCapabilitiesEvent();
        register(event, gasCapability);
        RegisterPartCapabilitiesEventInternal.register(event);
        registered = true;
    }

    @SubscribeEvent
    public static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability == null) {
            return;
        }

        register(event, gasCapability);
        registered = true;
    }

    private static void register(RegisterPartCapabilitiesEvent event, Capability<IGasHandler> gasCapability) {
        event.addHostType(TileCableBus.class);
        event.register(gasCapability, (part, _) -> part.getExposedApi(), GasP2PTunnelPart.class);
        event.register(gasCapability,
            (part, _) -> GenericInternalInventoryAdapters.getCapability(part.getLogic().getReturnInv(), gasCapability),
            PatternProviderPart.class);
        event.register(gasCapability,
            (part, _) -> GenericInternalInventoryAdapters.getCapability(part.getInterfaceLogic().getStorage(), gasCapability),
            InterfacePart.class);
    }
}
