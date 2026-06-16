package me.ramidzkh.mekae2.client;

import ae2.api.client.StorageCellModels;
import ae2.core.definitions.ItemDefinition;
import ae2.items.storage.BasicStorageCell;
import ae2.items.tools.powered.PortableCellItem;
import me.ramidzkh.mekae2.ae2.AMGasStackRenderer;
import me.ramidzkh.mekae2.Tags;
import me.ramidzkh.mekae2.item.AMItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class AppliedMekanisticsClient {

    private static boolean driveCellModelsRegistered;

    private AppliedMekanisticsClient() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerInventoryModel(AMItems.GAS_CELL_HOUSING);
        for (AMItems.Tier tier : AMItems.Tier.values()) {
            registerInventoryModel(AMItems.get(tier));
            registerInventoryModel(AMItems.getPortableCell(tier));
        }
        registerDriveCellModels();
        registerInventoryModel(AMItems.GAS_P2P_TUNNEL);
        AMGasStackRenderer.initialize();
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        ItemColors itemColors = event.getItemColors();
        IItemColor cells = BasicStorageCell::getColor;
        IItemColor portableCells = PortableCellItem::getColor;

        for (AMItems.Tier tier : AMItems.Tier.values()) {
            registerItemColor(itemColors, cells, AMItems.get(tier));
            registerItemColor(itemColors, portableCells, AMItems.getPortableCell(tier));
        }
    }

    private static void registerInventoryModel(ItemDefinition<?> definition) {
        Item item = definition.item();
        ResourceLocation id = definition.id();
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(id, "inventory"));
        }
    }

    private static void registerDriveCellModel(ItemDefinition<?> definition, AMItems.Tier tier) {
        Item item = definition.item();
        if (item != null) {
            StorageCellModels.registerModel(
                item,
                new ResourceLocation(Tags.MOD_ID, driveCellModelPath(tier)));
        }
    }

    private static void registerDriveCellModels() {
        if (driveCellModelsRegistered) {
            return;
        }
        driveCellModelsRegistered = true;
        for (AMItems.Tier tier : AMItems.Tier.values()) {
            registerDriveCellModel(AMItems.get(tier), tier);
            registerDriveCellModel(AMItems.getPortableCell(tier), tier);
        }
    }

    static String driveCellModelPath(AMItems.Tier tier) {
        return "block/drive/cells/gas_storage_cell_" + switch (tier) {
            case _1K -> "1k";
            case _4K -> "4k";
            case _16K -> "16k";
            case _64K -> "64k";
            case _256K -> "256k";
        };
    }

    private static void registerItemColor(ItemColors itemColors, IItemColor itemColor, ItemDefinition<?> definition) {
        Item item = definition.item();
        if (item != null) {
            itemColors.registerItemColorHandler(itemColor, item);
        }
    }
}
