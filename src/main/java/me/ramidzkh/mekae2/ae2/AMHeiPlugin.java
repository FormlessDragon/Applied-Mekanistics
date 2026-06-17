package me.ramidzkh.mekae2.ae2;

import ae2.api.integrations.hei.IngredientConverters;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

import javax.annotation.ParametersAreNonnullByDefault;

@JEIPlugin
@ParametersAreNonnullByDefault
public final class AMHeiPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IngredientConverters.register(new AMGasIngredientConverter());
    }
}
