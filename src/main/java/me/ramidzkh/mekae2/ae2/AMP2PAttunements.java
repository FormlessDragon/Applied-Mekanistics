package me.ramidzkh.mekae2.ae2;

import ae2.api.features.P2PTunnelAttunement;
import me.ramidzkh.mekae2.item.AMItems;
import mekanism.common.MekanismBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public final class AMP2PAttunements {

    private static boolean registered;

    private AMP2PAttunements() {
    }

    public static synchronized void init() {
        if (registered) {
            return;
        }
        registered = registerGasAttunement(AMItems.GAS_P2P_TUNNEL.id());
    }

    static boolean registerGasAttunement(ResourceLocation tunnelId) {
        P2PTunnelAttunement.registerAttunementTag(tunnelId, true);
        String oreName = P2PTunnelAttunement.getAttunementTag(tunnelId);
        OreDictionary.registerOre(oreName,
            new ItemStack(MekanismBlocks.GasTank, 1, OreDictionary.WILDCARD_VALUE));
        return true;
    }
}
