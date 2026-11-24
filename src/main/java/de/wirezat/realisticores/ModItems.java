package de.wirezat.realisticores;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry of the mod's items. */
public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealisticOres.MOD_ID);

    public static final DeferredItem<Item> PERIDOT = ITEMS.registerSimpleItem("peridot");
    public static final DeferredItem<Item> GRAPHITE = ITEMS.registerSimpleItem("graphite");
    public static final DeferredItem<Item> NATURAL_DIAMOND = ITEMS.registerSimpleItem("natural_diamond");

    private ModItems() {
    }

    /**
     * Registers the items and their creative tab entries on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(PERIDOT.get());
            event.accept(NATURAL_DIAMOND.get());
            event.accept(GRAPHITE.get());
        }
    }
}
