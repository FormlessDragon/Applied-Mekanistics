package me.ramidzkh.mekae2.ae2;

import ae2.parts.automation.StackWorldBehaviors;
import me.ramidzkh.mekae2.ae2.stack.MekanismExternalStorageStrategy;
import me.ramidzkh.mekae2.ae2.stack.MekanismStackExportStrategy;
import me.ramidzkh.mekae2.ae2.stack.MekanismStackImportStrategy;

@SuppressWarnings("UnstableApiUsage")
public final class AMStackWorldBehaviors {

    private AMStackWorldBehaviors() {
    }

    public static void init() {
        StackWorldBehaviors.registerImportStrategy(AEGasKeyType.TYPE, MekanismStackImportStrategy::new);
        StackWorldBehaviors.registerExportStrategy(AEGasKeyType.TYPE, MekanismStackExportStrategy::new);
        StackWorldBehaviors.registerExternalStorageStrategy(AEGasKeyType.TYPE, MekanismExternalStorageStrategy::new);
    }
}
