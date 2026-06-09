package me.ramidzkh.mekae2;

import ae2.api.parts.RegisterPartCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class AMPartCapabilities {

    private AMPartCapabilities() {
    }

    @SubscribeEvent
    public static void registerPartCapabilities(RegisterPartCapabilitiesEvent event) {
        AppliedMekanistics.registerPartCapabilities(event);
    }
}
