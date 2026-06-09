package me.ramidzkh.mekae2;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;

public final class AppliedMekanisticsLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public @Nullable String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public @Nullable String getModContainerClass() {
        return null;
    }

    @Override
    public @Nullable String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public @Nullable String getAccessTransformerClass() {
        return null;
    }
}
