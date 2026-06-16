package me.ramidzkh.mekae2.ae2;

import ae2.api.behaviors.ContainerItemStrategy;
import ae2.api.config.Actionable;
import ae2.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTankInfo;
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

@SuppressWarnings("UnstableApiUsage")
public class GasContainerItemStrategy
    implements ContainerItemStrategy<MekanismKey, GasContainerItemStrategy.Context> {

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

        GasStack extracted = container.extract(what, getOperationAmount(context, amount, mode),
            mode == Actionable.MODULATE);
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

        int inserted = container.insert(what.toStack(getOperationAmount(context, amount, mode)),
            mode == Actionable.MODULATE);
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

    private static int getOperationAmount(Context context, long amount, Actionable mode) {
        if (amount <= 0) {
            return 0;
        }
        if (mode == Actionable.SIMULATE && context.reportContainerLimitInSimulation()) {
            return Integer.MAX_VALUE;
        }
        return Ints.saturatedCast(amount);
    }

    @Nullable
    private static GasContainer findGasContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.getItem() instanceof IGasItem item) {
            return new GasItemContainer(stack, item);
        }

        if (Capabilities.GAS_HANDLER_CAPABILITY != null) {
            IGasHandler handler = stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);
            if (handler != null) {
                return new GasHandlerContainer(handler);
            }
        }

        return null;
    }

    private interface GasContainer {

        @Nullable
        GasStack getContainedGas();

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean canInsert(MekanismKey what);

        int insert(GasStack stack, boolean execute);

        boolean canExtract(MekanismKey what);

        @Nullable
        GasStack extract(MekanismKey what, int amount, boolean execute);
    }

    private record GasHandlerContainer(IGasHandler handler) implements GasContainer {

        @Override
        public GasStack getContainedGas() {
            for (GasTankInfo tank : this.handler.getTankInfo()) {
                if (tank == null) {
                    continue;
                }

                GasStack gas = tank.getGas();
                if (MekanismKey.of(gas) != null) {
                    return gas;
                }
            }
            return this.handler.drawGas(EnumFacing.UP, Integer.MAX_VALUE, false);
        }

        @Override
        public boolean canInsert(MekanismKey what) {
            return this.handler.canReceiveGas(EnumFacing.UP, what.getGas());
        }

        @Override
        public int insert(GasStack stack, boolean execute) {
            int insertable = getInsertableAmount(stack);
            if (!execute || insertable <= 0) {
                return insertable;
            }

            int inserted = 0;
            while (inserted < insertable) {
                int before = getStoredAmount(stack.getGas());
                int step = this.handler.receiveGas(EnumFacing.UP, stack.copy().withAmount(insertable - inserted), true);
                if (step <= 0) {
                    break;
                }

                inserted += Math.min(step, insertable - inserted);
                int after = getStoredAmount(stack.getGas());
                if (after <= before) {
                    break;
                }
            }
            return inserted;
        }

        @Override
        public boolean canExtract(MekanismKey what) {
            return this.handler.canDrawGas(EnumFacing.UP, what.getGas());
        }

        @Override
        public GasStack extract(MekanismKey what, int amount, boolean execute) {
            int extractable = getExtractableAmount(what, amount);
            if (extractable <= 0) {
                return null;
            }

            if (!execute) {
                return what.toStack(extractable);
            }

            int extractedAmount = 0;
            while (extractedAmount < extractable) {
                int before = getStoredAmount(what.getGas());
                GasStack extracted = this.handler.drawGas(EnumFacing.UP, extractable - extractedAmount, true);
                if (extracted == null || extracted.getGas() != what.getGas() || extracted.amount <= 0) {
                    break;
                }

                extractedAmount += Math.min(extracted.amount, extractable - extractedAmount);
                int after = getStoredAmount(what.getGas());
                if (after >= before) {
                    if (isFullGasHandler(what.getGas())) {
                        extractedAmount = extractable;
                    }
                    break;
                }
            }
            return extractedAmount > 0 ? what.toStack(extractedAmount) : null;
        }

        private int getInsertableAmount(GasStack stack) {
            MekanismKey key = MekanismKey.of(stack);
            if (key == null || stack.amount <= 0 || !canInsert(key)) {
                return 0;
            }

            int capacity = 0;
            int storedAmount = 0;
            boolean foundCompatibleTank = false;
            for (GasTankInfo tank : this.handler.getTankInfo()) {
                if (tank == null) {
                    continue;
                }

                GasStack stored = tank.getGas();
                if (stored != null && stored.getGas() != stack.getGas()) {
                    continue;
                }

                foundCompatibleTank = true;
                capacity = saturatingAdd(capacity, Math.max(0, tank.getMaxGas()));
                storedAmount = saturatingAdd(storedAmount, stored == null ? 0 : Math.max(0, stored.amount));
            }

            if (!foundCompatibleTank) {
                return this.handler.receiveGas(EnumFacing.UP, stack, false);
            }
            return Math.clamp(capacity - storedAmount, 0, stack.amount);
        }

        private int getExtractableAmount(MekanismKey what, int amount) {
            if (amount <= 0 || !canExtract(what)) {
                return 0;
            }

            int storedAmount = 0;
            boolean foundCompatibleTank = false;
            for (GasTankInfo tank : this.handler.getTankInfo()) {
                if (tank == null) {
                    continue;
                }

                GasStack stored = tank.getGas();
                if (stored == null || stored.getGas() != what.getGas()) {
                    continue;
                }

                foundCompatibleTank = true;
                storedAmount = saturatingAdd(storedAmount, Math.max(0, stored.amount));
            }

            if (!foundCompatibleTank) {
                GasStack extracted = this.handler.drawGas(EnumFacing.UP, amount, false);
                return extracted != null && extracted.getGas() == what.getGas() ? extracted.amount : 0;
            }
            return Math.clamp(storedAmount, 0, amount);
        }

        private int getStoredAmount(Gas gas) {
            int storedAmount = 0;
            for (GasTankInfo tank : this.handler.getTankInfo()) {
                if (tank == null) {
                    continue;
                }

                GasStack stored = tank.getGas();
                if (stored != null && stored.getGas() == gas) {
                    storedAmount = saturatingAdd(storedAmount, Math.max(0, stored.amount));
                }
            }
            return storedAmount;
        }

        private boolean isFullGasHandler(Gas gas) {
            for (GasTankInfo tank : this.handler.getTankInfo()) {
                if (tank == null) {
                    continue;
                }

                GasStack stored = tank.getGas();
                if (stored != null && stored.getGas() == gas && tank.getMaxGas() > 0
                    && tank.getStored() >= tank.getMaxGas()) {
                    return true;
                }
            }
            return false;
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
            int insertable = getInsertableAmount(stack);
            if (!execute || insertable <= 0) {
                return insertable;
            }

            ItemStack target = this.stack;
            int inserted = 0;
            while (inserted < insertable) {
                int before = getStoredAmount(target, stack.getGas());
                int step = this.item.addGas(target, stack.copy().withAmount(insertable - inserted));
                if (step <= 0) {
                    break;
                }

                inserted += Math.min(step, insertable - inserted);
                int after = getStoredAmount(target, stack.getGas());
                if (after <= before) {
                    break;
                }
            }
            return inserted;
        }

        @Override
        public boolean canExtract(MekanismKey what) {
            return this.item.canProvideGas(this.stack, what.getGas());
        }

        @Override
        public GasStack extract(MekanismKey what, int amount, boolean execute) {
            int extractable = getExtractableAmount(what, amount);
            if (extractable <= 0) {
                return null;
            }

            if (!execute) {
                return what.toStack(extractable);
            }

            ItemStack target = this.stack;
            int extractedAmount = 0;
            while (extractedAmount < extractable) {
                int before = getStoredAmount(target, what.getGas());
                GasStack extracted = this.item.removeGas(target, extractable - extractedAmount);
                if (extracted == null || extracted.getGas() != what.getGas() || extracted.amount <= 0) {
                    break;
                }

                extractedAmount += Math.min(extracted.amount, extractable - extractedAmount);
                int after = getStoredAmount(target, what.getGas());
                if (after >= before) {
                    if (isFullGasItem(target, before)) {
                        extractedAmount = extractable;
                    }
                    break;
                }
            }
            return extractedAmount > 0 ? what.toStack(extractedAmount) : null;
        }

        private int getInsertableAmount(GasStack stack) {
            MekanismKey key = MekanismKey.of(stack);
            if (key == null || stack.amount <= 0 || !canInsert(key)) {
                return 0;
            }

            GasStack stored = this.item.getGas(this.stack);
            if (stored != null && stored.getGas() != stack.getGas()) {
                return 0;
            }

            int storedAmount = stored == null ? 0 : Math.max(0, stored.amount);
            int capacity = Math.max(0, this.item.getMaxGas(this.stack));
            return Math.clamp(capacity - storedAmount, 0, stack.amount);
        }

        private int getExtractableAmount(MekanismKey what, int amount) {
            if (amount <= 0) {
                return 0;
            }

            GasStack stored = this.item.getGas(this.stack);
            if (stored == null || stored.getGas() != what.getGas()) {
                return 0;
            }

            return Math.clamp(stored.amount, 0, amount);
        }

        private int getStoredAmount(ItemStack stack, Gas gas) {
            GasStack stored = this.item.getGas(stack);
            return stored != null && stored.getGas() == gas ? Math.max(0, stored.amount) : 0;
        }

        private boolean isFullGasItem(ItemStack stack, int storedAmount) {
            int capacity = this.item.getMaxGas(stack);
            return capacity > 0 && storedAmount >= capacity;
        }

    }

    private static int saturatingAdd(int left, int right) {
        long result = (long) left + right;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }

    public interface Context {
        ItemStack getStack();

        void addOverflow(@Nullable ItemStack stack);

        default boolean reportContainerLimitInSimulation() {
            return false;
        }
    }

    private record CarriedContext(EntityPlayer player) implements Context {

        @Override
        public ItemStack getStack() {
            return this.player.inventory.getItemStack();
        }

        @Override
        public void addOverflow(@Nullable ItemStack stack) {
            if (this.player.inventory.getItemStack().isEmpty()) {
                this.player.inventory.setItemStack(stack);
            } else {
                this.player.inventory.addItemStackToInventory(stack);
            }
        }

        @Override
        public boolean reportContainerLimitInSimulation() {
            return true;
        }
    }

    private record PlayerInvContext(EntityPlayer player, int slot) implements Context {

        @Override
        public ItemStack getStack() {
            return this.player.inventory.getStackInSlot(this.slot);
        }

        @Override
        public void addOverflow(@Nullable ItemStack stack) {
            this.player.inventory.addItemStackToInventory(stack);
        }
    }
}
