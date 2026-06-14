package me.ramidzkh.mekae2.item;

import java.util.Objects;
import java.util.function.Function;

import ae2.api.parts.PartModels;
import ae2.api.parts.IPart;
import ae2.api.parts.IPartItem;
import ae2.core.definitions.ItemDefinition;
import ae2.items.parts.PartItem;
import ae2.items.parts.PartModelsHelper;
import ae2.items.materials.MaterialItem;
import ae2.items.storage.StorageTier;
import me.ramidzkh.mekae2.AppliedMekanistics;
import me.ramidzkh.mekae2.util.Reference;
import me.ramidzkh.mekae2.ae2.ChemicalP2PTunnelPart;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class AMItems {

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return CHEMICAL_CELL_64K.stack();
        }
    };

    public static final ItemDefinition<MaterialItem> CHEMICAL_CELL_HOUSING = new ItemDefinition<>(
        AppliedMekanistics.id("chemical_cell_housing"), new MaterialItem(), CREATIVE_TAB);

    public static final ItemDefinition<ChemicalStorageCell> CHEMICAL_CELL_1K = new ItemDefinition<>(
        AppliedMekanistics.id("chemical_storage_cell_1k"), new ChemicalStorageCell(StorageTier.SIZE_1K), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalStorageCell> CHEMICAL_CELL_4K = new ItemDefinition<>(
        AppliedMekanistics.id("chemical_storage_cell_4k"), new ChemicalStorageCell(StorageTier.SIZE_4K), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalStorageCell> CHEMICAL_CELL_16K = new ItemDefinition<>(
        AppliedMekanistics.id("chemical_storage_cell_16k"), new ChemicalStorageCell(StorageTier.SIZE_16K), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalStorageCell> CHEMICAL_CELL_64K = new ItemDefinition<>(
        AppliedMekanistics.id("chemical_storage_cell_64k"), new ChemicalStorageCell(StorageTier.SIZE_64K), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalStorageCell> CHEMICAL_CELL_256K = new ItemDefinition<>(
        AppliedMekanistics.id("chemical_storage_cell_256k"), new ChemicalStorageCell(StorageTier.SIZE_256K),
        CREATIVE_TAB);

    public static final ItemDefinition<PartItem<ChemicalP2PTunnelPart>> CHEMICAL_P2P_TUNNEL = createPart(
        AppliedMekanistics.id("chemical_p2p_tunnel"), ChemicalP2PTunnelPart.class, ChemicalP2PTunnelPart::new);

    public static final ItemDefinition<ChemicalPortableCellItem> PORTABLE_CHEMICAL_CELL_1K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_chemical_cell_1k"),
        new ChemicalPortableCellItem(18, StorageTier.SIZE_1K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalPortableCellItem> PORTABLE_CHEMICAL_CELL_4K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_chemical_cell_4k"),
        new ChemicalPortableCellItem(18, StorageTier.SIZE_4K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalPortableCellItem> PORTABLE_CHEMICAL_CELL_16K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_chemical_cell_16k"),
        new ChemicalPortableCellItem(18, StorageTier.SIZE_16K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalPortableCellItem> PORTABLE_CHEMICAL_CELL_64K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_chemical_cell_64k"),
        new ChemicalPortableCellItem(18, StorageTier.SIZE_64K, 20000, 0x80caff), CREATIVE_TAB);
    public static final ItemDefinition<ChemicalPortableCellItem> PORTABLE_CHEMICAL_CELL_256K = new ItemDefinition<>(
        AppliedMekanistics.id("portable_chemical_cell_256k"),
        new ChemicalPortableCellItem(18, StorageTier.SIZE_256K, 20000, 0x80caff), CREATIVE_TAB);

    private static final ItemDefinition<?>[] ITEMS = {
        CHEMICAL_CELL_HOUSING,
        CHEMICAL_CELL_1K,
        CHEMICAL_CELL_4K,
        CHEMICAL_CELL_16K,
        CHEMICAL_CELL_64K,
        CHEMICAL_CELL_256K,
        PORTABLE_CHEMICAL_CELL_1K,
        PORTABLE_CHEMICAL_CELL_4K,
        PORTABLE_CHEMICAL_CELL_16K,
        PORTABLE_CHEMICAL_CELL_64K,
        PORTABLE_CHEMICAL_CELL_256K,
        CHEMICAL_P2P_TUNNEL
    };

    private AMItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (ItemDefinition<?> definition : ITEMS) {
            event.getRegistry().register(Objects.requireNonNull(definition.item()));
        }
    }

    public static ItemDefinition<ChemicalStorageCell> get(Tier tier) {
        return switch (tier) {
            case _1K -> CHEMICAL_CELL_1K;
            case _4K -> CHEMICAL_CELL_4K;
            case _16K -> CHEMICAL_CELL_16K;
            case _64K -> CHEMICAL_CELL_64K;
            case _256K -> CHEMICAL_CELL_256K;
        };
    }

    public enum Tier {
        _1K,
        _4K,
        _16K,
        _64K,
        _256K
    }

    public static ItemDefinition<ChemicalPortableCellItem> getPortableCell(Tier tier) {
        return switch (tier) {
            case _1K -> PORTABLE_CHEMICAL_CELL_1K;
            case _4K -> PORTABLE_CHEMICAL_CELL_4K;
            case _16K -> PORTABLE_CHEMICAL_CELL_16K;
            case _64K -> PORTABLE_CHEMICAL_CELL_64K;
            case _256K -> PORTABLE_CHEMICAL_CELL_256K;
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends IPart> ItemDefinition<PartItem<T>> createPart(
        ResourceLocation id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        PartItem<T> item = new PartItem<>(partClass, factory);
        item.setCreativeTab(CREATIVE_TAB);
        return new ItemDefinition<>(id, item, CREATIVE_TAB);
    }
}
