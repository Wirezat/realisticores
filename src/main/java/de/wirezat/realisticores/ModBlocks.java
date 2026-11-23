package de.wirezat.realisticores;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
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
    private static final List<DeferredItem<? extends Item>> BLOCK_ITEMS = new ArrayList<>();

    public static final DeferredBlock<Block> KIMBERLITE =
            register("kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE));
    public static final DeferredBlock<Block> KIMBERLITE_BRECCIA =
            register("kimberlite_breccia", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
    public static final DeferredBlock<Block> VOLCANICLASTIC_KIMBERLITE =
            register("volcaniclastic_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).strength(1.0F, 3.0F));
    public static final DeferredBlock<Block> WEATHERED_KIMBERLITE =
            register("weathered_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY));
    public static final DeferredBlock<Block> SERPENTINIZED_KIMBERLITE =
            register("serpentinized_kimberlite", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(1.2F, 4.0F));
    public static final DeferredBlock<Block> PERIDOTITE =
            register("peridotite", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.5F, 9.0F));

    private ModBlocks() {
    }

    private static DeferredBlock<Block> register(String name, BlockBehaviour.Properties properties) {
        DeferredBlock<Block> block = BLOCKS.registerSimpleBlock(name, properties);
        BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(name, block));
        return block;
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
            BLOCK_ITEMS.forEach(item -> event.accept(item.get()));
        }
    }
}
