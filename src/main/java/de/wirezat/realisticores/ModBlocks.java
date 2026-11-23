package de.wirezat.realisticores;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry of the mod's blocks and their block items. */
public final class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RealisticOres.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealisticOres.MOD_ID);

    public static final DeferredBlock<Block> KIMBERLITE =
            BLOCKS.registerSimpleBlock("kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));
    public static final DeferredItem<?> KIMBERLITE_ITEM = ITEMS.registerSimpleBlockItem("kimberlite", KIMBERLITE);

    private ModBlocks() {
    }

    /**
     * Registers blocks, items and the creative tab entries on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(ModBlocks::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(KIMBERLITE_ITEM.get());
        }
    }
}
