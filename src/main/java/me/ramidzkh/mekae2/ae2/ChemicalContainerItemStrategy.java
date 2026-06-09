package me.ramidzkh.mekae2.ae2;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.config.Actionable;
import ae2.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

public class ChemicalContainerItemStrategy
    implements ContainerItemStrategy<MekanismKey, ChemicalContainerItemStrategy.Context> {

    @Override
    @Nullable
    public GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        GasContainer container = findGasContainer(stack);
        if (container == null) {
            return null;
        }

        GasStack gas = container.getContainedGas();
        MekanismKey key = MekanismKey.of(gas);
        return key == null ? null : new GenericStack(key, gas.amount);
    }

    @Override
    @Nullable
    public Context findCarriedContext(EntityPlayer player, Container container) {
        ItemStack carried = player.inventory.getItemStack();
        if (hasGasHandler(carried)) {
            return new CarriedContext(player);
        }
        return null;
    }

    @Override
    @Nullable
    public Context findPlayerSlotContext(EntityPlayer player, int slot) {
        ItemStack stack = player.inventory.getStackInSlot(slot);
        if (hasGasHandler(stack)) {
            return new PlayerInvContext(player, slot);
        }
        return null;
    }

    @Override
    public long extract(Context context, MekanismKey what, long amount, Actionable mode) {
        ItemStack stack = context.getStack();
        ItemStack copy = stack.copy();
        copy.setCount(1);
        GasContainer container = findGasContainer(copy);
        if (container == null || !container.canExtract(what)) {
            return 0;
        }

        GasStack extracted = container.extract(what, Ints.saturatedCast(amount), mode == Actionable.MODULATE);
        int extractedAmount = extracted != null && extracted.getGas() == what.getGas() ? extracted.amount : 0;
        if (extractedAmount > 0 && mode == Actionable.MODULATE) {
            stack.shrink(1);
            context.addOverflow(copy);
        }
        return extractedAmount;
    }

    @Override
    public long insert(Context context, MekanismKey what, long amount, Actionable mode) {
        ItemStack stack = context.getStack();
        ItemStack copy = stack.copy();
        copy.setCount(1);
        GasContainer container = findGasContainer(copy);
        if (container == null || !container.canInsert(what)) {
            return 0;
        }

        int inserted = container.insert(what.toStack(amount), mode == Actionable.MODULATE);
        if (inserted > 0 && mode == Actionable.MODULATE) {
            stack.shrink(1);
            context.addOverflow(copy);
        }
        return inserted;
    }

    @Override
    public void playFillSound(EntityPlayer player, MekanismKey what) {
        player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
    }

    @Override
    public void playEmptySound(EntityPlayer player, MekanismKey what) {
        player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0F, 1.0F);
    }

    @Override
    @Nullable
    public GenericStack getExtractableContent(Context context) {
        return getContainedStack(context.getStack());
    }

    private static boolean hasGasHandler(ItemStack stack) {
        return findGasContainer(stack) != null;
    }

    @Nullable
    private static GasContainer findGasContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        IGasHandler handler = stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);
        if (handler != null) {
            return new GasHandlerContainer(handler);
        }

        if (stack.getItem() instanceof IGasItem item) {
            return new GasItemContainer(stack, item);
        }

        return null;
    }

    private interface GasContainer {

        @Nullable
        GasStack getContainedGas();

        boolean canInsert(MekanismKey what);

        int insert(GasStack stack, boolean execute);

        boolean canExtract(MekanismKey what);

        @Nullable
        GasStack extract(MekanismKey what, int amount, boolean execute);
    }

    private record GasHandlerContainer(IGasHandler handler) implements GasContainer {

        @Override
        public GasStack getContainedGas() {
            return this.handler.drawGas(EnumFacing.UP, Integer.MAX_VALUE, false);
        }

        @Override
        public boolean canInsert(MekanismKey what) {
            return this.handler.canReceiveGas(EnumFacing.UP, what.getGas());
        }

        @Override
        public int insert(GasStack stack, boolean execute) {
            return this.handler.receiveGas(EnumFacing.UP, stack, execute);
        }

        @Override
        public boolean canExtract(MekanismKey what) {
            return this.handler.canDrawGas(EnumFacing.UP, what.getGas());
        }

        @Override
        public GasStack extract(MekanismKey what, int amount, boolean execute) {
            GasStack extracted = this.handler.drawGas(EnumFacing.UP, amount, execute);
            return extracted != null && extracted.getGas() == what.getGas() ? extracted : null;
        }
    }

    private record GasItemContainer(ItemStack stack, IGasItem item) implements GasContainer {

        @Override
        public GasStack getContainedGas() {
            return this.item.getGas(this.stack);
        }

        @Override
        public boolean canInsert(MekanismKey what) {
            return this.item.canReceiveGas(this.stack, what.getGas());
        }

        @Override
        public int insert(GasStack stack, boolean execute) {
            if (!execute) {
                ItemStack copy = this.stack.copy();
                copy.setCount(1);
                return this.item.addGas(copy, stack);
            }
            return this.item.addGas(this.stack, stack);
        }

        @Override
        public boolean canExtract(MekanismKey what) {
            return this.item.canProvideGas(this.stack, what.getGas());
        }

        @Override
        public GasStack extract(MekanismKey what, int amount, boolean execute) {
            ItemStack target = this.stack;
            if (!execute) {
                target = this.stack.copy();
                target.setCount(1);
            }

            GasStack extracted = this.item.removeGas(target, amount);
            return extracted != null && extracted.getGas() == what.getGas() ? extracted : null;
        }
    }

    interface Context {
        ItemStack getStack();

        void addOverflow(ItemStack stack);
    }

    private record CarriedContext(EntityPlayer player) implements Context {

        @Override
        public ItemStack getStack() {
            return this.player.inventory.getItemStack();
        }

        @Override
        public void addOverflow(ItemStack stack) {
            if (this.player.inventory.getItemStack().isEmpty()) {
                this.player.inventory.setItemStack(stack);
            } else {
                this.player.inventory.addItemStackToInventory(stack);
            }
        }
    }

    private record PlayerInvContext(EntityPlayer player, int slot) implements Context {

        @Override
        public ItemStack getStack() {
            return this.player.inventory.getStackInSlot(this.slot);
        }

        @Override
        public void addOverflow(ItemStack stack) {
            this.player.inventory.addItemStackToInventory(stack);
        }
    }
}
