package me.ramidzkh.mekae2.ae2;

import ae2.api.features.P2PTunnelAttunement;
import me.ramidzkh.mekae2.item.AMItems;
import me.ramidzkh.mekae2.util.AMText;
import me.ramidzkh.mekae2.AppliedMekanistics;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public final class AMP2PAttunements {

    private static boolean registered;

    private AMP2PAttunements() {
    }

    public static synchronized void init() {
        if (registered) {
            return;
        }
        registered = registerGasAttunement(AMItems.CHEMICAL_P2P_TUNNEL.id());
    }

    static boolean registerGasAttunement(ResourceLocation tunnelPart) {
        Capability<IGasHandler> gasCapability = Capabilities.GAS_HANDLER_CAPABILITY;
        if (gasCapability == null) {
            AppliedMekanistics.LOGGER.warn("Mekanism gas capability is not available during AppMek P2P attunement registration");
            return false;
        }
        P2PTunnelAttunement.registerAttunementApi(tunnelPart, gasCapability, AMText.GASES.text(), true);
        return true;
    }
}
