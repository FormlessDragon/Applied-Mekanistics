package me.ramidzkh.mekae2;

import me.ramidzkh.mekae2.ae2.AMP2PAttunements;
import me.ramidzkh.mekae2.ae2.AMPartCapabilities;
import me.ramidzkh.mekae2.ae2.AMStackWorldBehaviors;
import me.ramidzkh.mekae2.ae2.AMStorageIntegration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:ae2@[1.0.3,);" +
        "required-after:mekanism;"
)
public final class AppliedMekanistics {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static AppliedMekanistics INSTANCE;

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} preInit", Tags.MOD_NAME);
        AMStorageIntegration.init();
        AMPartCapabilities.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("{} init", Tags.MOD_NAME);
        AMStackWorldBehaviors.init();
        AMP2PAttunements.init();
    }
}
