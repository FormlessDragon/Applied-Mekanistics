package me.ramidzkh.mekae2.item;

import java.util.Objects;
import java.util.function.Function;

import ae2.api.parts.PartModels;
import ae2.api.parts.IPartItem;
import ae2.core.definitions.ItemDefinition;
import ae2.items.parts.P2PPartItem;
import ae2.items.parts.PartModelsHelper;
import ae2.items.materials.MaterialItem;
import ae2.items.storage.StorageTier;
import ae2.parts.p2p.P2PTunnelPart;
import me.ramidzkh.mekae2.AppliedMekanistics;
import me.ramidzkh.mekae2.ae2.GasP2PTunnelPart;
import me.ramidzkh.mekae2.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class AMItems {

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(Tags.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return GAS_CELL_64K.stack();
        }
    };

    public static final ItemDefinition<MaterialItem> GAS_CELL_HOUSING = new ItemDefinition<>(
        AppliedMekanistics.id("gas_cell_housing"), new MaterialItem(), CREATIVE_TAB);

    public static final ItemDefinition<GasStorageCell> GAS_CELL_1K = new ItemDefinition<>(
        AppliedMekanistics.id("gas_storage_cell_1k"), new GasStorageCell(StorageTier.SIZE_1K), CREATIVE_TAB);
    public static final ItemDefinition<GasStorageCell> GAS_CELL_4K = new ItemDefinition<>(
        AppliedMekanistics.id("gas_storage_cell_4k"), new GasStorageCell(StorageTier.SIZE_4K), CREATIVE_TAB);
    public static final ItemDefinition<GasStorageCell> GAS_CELL_16K = new ItemDefinition<>(
        AppliedMekanistics.id("gas_storage_cell_16k"), new GasStorageCell(StorageTier.SIZE_16K), CREATIVE_TAB);
    public static final ItemDefinition<GasStorageCell> GAS_CELL_64K = new ItemDefinition<>(
        AppliedMekanistics.id("gas_storage_cell_64k"), new GasStorageCell(StorageTier.SIZE_64K), CREATIVE_TAB);
    public static final ItemDefinition<GasStorageCell> GAS_CELL_256K = new ItemDefinition<>(
        AppliedMekanistics.id("gas_storage_cell_256k"), new GasStorageCell(StorageTier.SIZE_256K),
        CREATIVE_TAB);

    public static final ItemDefinition<P2PPartItem<GasP2PTunnelPart>> GAS_P2P_TUNNEL = createP2PPart(
        AppliedMekanistics.id("gas_p2p_tunnel"), GasP2PTunnelPart.class, GasP2PTunnelPart::new);

    public static final ItemDefinition<GasPortableCellItem> PORTABLE_GAS_CELL_1K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_gas_cell_1k"),
        new GasPortableCellItem(18, StorageTier.SIZE_1K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<GasPortableCellItem> PORTABLE_GAS_CELL_4K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_gas_cell_4k"),
        new GasPortableCellItem(18, StorageTier.SIZE_4K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<GasPortableCellItem> PORTABLE_GAS_CELL_16K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_gas_cell_16k"),
        new GasPortableCellItem(18, StorageTier.SIZE_16K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<GasPortableCellItem> PORTABLE_GAS_CELL_64K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_gas_cell_64k"),
        new GasPortableCellItem(18, StorageTier.SIZE_64K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<GasPortableCellItem> PORTABLE_GAS_CELL_256K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_gas_cell_256k"),
        new GasPortableCellItem(18, StorageTier.SIZE_256K, 20000, 0x80caff), CREATIVE_TAB);

    private static final ItemDefinition<?>[] ITEMS = {
        GAS_CELL_HOUSING,
        GAS_CELL_1K,
        GAS_CELL_4K,
        GAS_CELL_16K,
        GAS_CELL_64K,
        GAS_CELL_256K,
        PORTABLE_GAS_CELL_1K,
        PORTABLE_GAS_CELL_4K,
        PORTABLE_GAS_CELL_16K,
        PORTABLE_GAS_CELL_64K,
        PORTABLE_GAS_CELL_256K,
        GAS_P2P_TUNNEL
    };

    private AMItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (ItemDefinition<?> definition : ITEMS) {
            event.getRegistry().register(Objects.requireNonNull(definition.item()));
        }
    }

    public static ItemDefinition<GasStorageCell> get(Tier tier) {
        return switch (tier) {
            case _1K -> GAS_CELL_1K;
            case _4K -> GAS_CELL_4K;
            case _16K -> GAS_CELL_16K;
            case _64K -> GAS_CELL_64K;
            case _256K -> GAS_CELL_256K;
        };
    }

    public enum Tier {
        _1K,
        _4K,
        _16K,
        _64K,
        _256K
    }

    public static ItemDefinition<GasPortableCellItem> getPortableCell(Tier tier) {
        return switch (tier) {
            case _1K -> PORTABLE_GAS_CELL_1K;
            case _4K -> PORTABLE_GAS_CELL_4K;
            case _16K -> PORTABLE_GAS_CELL_16K;
            case _64K -> PORTABLE_GAS_CELL_64K;
            case _256K -> PORTABLE_GAS_CELL_256K;
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends P2PTunnelPart<T>> ItemDefinition<P2PPartItem<T>> createP2PPart(
        ResourceLocation id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        P2PPartItem<T> item = new P2PPartItem<>(partClass, factory);
        item.setCreativeTab(CREATIVE_TAB);
        return new ItemDefinition<>(id, item, CREATIVE_TAB);
    }
}
