package me.ramidzkh.mekae2;

import java.lang.reflect.Field;
import java.util.Map;

import ae2.api.behaviors.GenericInternalInventoryAdapters;
import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.features.P2PTunnelAttunement;
import ae2.api.parts.RegisterPartCapabilitiesEvent;
import ae2.api.parts.RegisterPartCapabilitiesEventInternal;
import ae2.api.stacks.AEKeyType;
import ae2.api.stacks.AEKeyTypes;
import ae2.parts.automation.StackWorldBehaviors;
import ae2.parts.crafting.PatternProviderPart;
import ae2.parts.misc.InterfacePart;
import me.ramidzkh.mekae2.ae2.ChemicalContainerItemStrategy;
import me.ramidzkh.mekae2.ae2.GenericStackChemicalStorage;
import me.ramidzkh.mekae2.ae2.ChemicalP2PTunnelPart;
import me.ramidzkh.mekae2.ae2.MekanismKeyType;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import me.ramidzkh.mekae2.ae2.stack.MekanismExternalStorageStrategy;
import me.ramidzkh.mekae2.ae2.stack.MekanismStackExportStrategy;
import me.ramidzkh.mekae2.ae2.stack.MekanismStackImportStrategy;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
    acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:ae2;required-after:mekanism")
public final class AppliedMekanistics {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);
    private static boolean p2PAttunementsRegistered;
    private static boolean partCapabilitiesRegistered;

    @Mod.Instance(Reference.MOD_ID)
    public static AppliedMekanistics INSTANCE;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Reference.MOD_ID, path);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} preInit", Reference.MOD_NAME);
        AEKeyTypes.register(MekanismKeyType.TYPE);
        GenericSlotCapacitiesBridge.registerGasCapacity();
        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability != null) {
            GenericInternalInventoryAdapters.register(gasCapability, GenericStackChemicalStorage::new);
        } else {
            LOGGER.warn("Mekanism gas capability is not available during AppMek preInit");
        }
        ContainerItemStrategy.register(MekanismKeyType.TYPE, MekanismKey.class, new ChemicalContainerItemStrategy());
        registerDeferredPartCapabilities();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} init", Reference.MOD_NAME);
        StackWorldBehaviors.registerImportStrategy(MekanismKeyType.TYPE, MekanismStackImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(MekanismKeyType.TYPE, MekanismStackExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(MekanismKeyType.TYPE, MekanismExternalStorageStrategy::new);
        registerP2PAttunements();
    }

    public static synchronized void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability == null) {
            return;
        }
        registerPartCapabilities(event, gasCapability);
        partCapabilitiesRegistered = true;
    }

    static synchronized void registerDeferredPartCapabilities() {
        if (partCapabilitiesRegistered) {
            return;
        }

        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability == null) {
            return;
        }

        RegisterPartCapabilitiesEvent event = new RegisterPartCapabilitiesEvent();
        mergeExistingPartCapabilityRegistration(event, gasCapability);
        registerPartCapabilities(event, gasCapability);
        RegisterPartCapabilitiesEventInternal.register(event);
        partCapabilitiesRegistered = true;
    }

    @SuppressWarnings("unchecked")
    private static void mergeExistingPartCapabilityRegistration(RegisterPartCapabilitiesEvent event,
                                                               Capability<IGasHandler> gasCapability) {
        try {
            Map<Capability<?>, ?> internalRegistrations = (Map<Capability<?>, ?>)
                fieldValue(RegisterPartCapabilitiesEventInternal.class, "REGISTRATIONS", null);
            Object gasRegistration = internalRegistrations.get(gasCapability);
            if (gasRegistration == null) {
                return;
            }

            Map<Capability<?>, Object> eventRegistrations = (Map<Capability<?>, Object>)
                fieldValue(RegisterPartCapabilitiesEvent.class, "capabilityRegistrations", event);
            eventRegistrations.put(gasCapability, gasRegistration);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to merge existing AE2 part capability registrations", e);
        }
    }

    private static Object fieldValue(Class<?> type, String name, Object instance) throws ReflectiveOperationException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static void registerPartCapabilities(RegisterPartCapabilitiesEvent event, Capability<IGasHandler> gasCapability) {
        event.register(gasCapability, (part, side) -> part.getExposedApi(), ChemicalP2PTunnelPart.class);
        event.register(gasCapability,
            (part, side) -> GenericInternalInventoryAdapters.getCapability(part.getLogic().getReturnInv(), gasCapability),
            PatternProviderPart.class);
        event.register(gasCapability,
            (part, side) -> GenericInternalInventoryAdapters.getCapability(part.getInterfaceLogic().getStorage(), gasCapability),
            InterfacePart.class);
    }

    public static synchronized void registerP2PAttunements() {
        if (p2PAttunementsRegistered) {
            return;
        }
        if (Capabilities.GAS_HANDLER_CAPABILITY == null) {
            LOGGER.warn("Mekanism gas capability is not available during AppMek P2P attunement registration");
            return;
        }
        p2PAttunementsRegistered = registerP2PAttunement(AMItems.CHEMICAL_P2P_TUNNEL.id());
    }

    static boolean registerP2PAttunement(ResourceLocation tunnelPart) {
        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability == null) {
            LOGGER.warn("Mekanism gas capability is not available during AppMek P2P attunement registration");
            return false;
        }
        P2PTunnelAttunement.registerAttunementApi(tunnelPart, gasCapability, AMText.GASES.text());
        return true;
    }

    private static final class GenericSlotCapacitiesBridge {
        private GenericSlotCapacitiesBridge() {
        }

        private static void registerGasCapacity() {
            var fluidCapacity = ae2.api.behaviors.GenericSlotCapacities.getMap().get(AEKeyType.fluids());
            if (fluidCapacity != null) {
                ae2.api.behaviors.GenericSlotCapacities.register(MekanismKeyType.TYPE, fluidCapacity);
            }
        }
    }
}
