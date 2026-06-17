package me.ramidzkh.mekae2.ae2;

import ae2.api.integrations.hei.IngredientConverter;
import ae2.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.recipe.IIngredientType;
import org.jetbrains.annotations.Nullable;

public final class AMGasIngredientConverter implements IngredientConverter<GasStack> {

    @Override
    public IIngredientType<GasStack> getIngredientType() {
        return MekanismJEI.TYPE_GAS;
    }

    @Nullable
    @Override
    public GasStack getIngredientFromStack(GenericStack stack) {
        if (stack == null || stack.amount() <= 0) {
            return null;
        }

        if (stack.what() instanceof AEGasKey gasKey) {
            return gasKey.toStack(Math.max(1, Ints.saturatedCast(stack.amount())));
        }
        return null;
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(GasStack ingredient) {
        AEGasKey key = AEGasKey.of(ingredient);
        if (key == null) {
            return null;
        }

        return new GenericStack(key, ingredient.amount);
    }
}
