package de.wirezat.realisticores;

import de.wirezat.realisticores.indicator.PanItem;
import de.wirezat.realisticores.survey.KimberliteMapItem;
import java.util.Collection;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry of the mod's items. */
public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RealisticOres.MOD_ID);

    public static final DeferredItem<Item> PERIDOT = ITEMS.registerSimpleItem("peridot");
    public static final DeferredItem<Item> GRAPHITE = ITEMS.registerSimpleItem("graphite");
    public static final DeferredItem<Item> NATURAL_DIAMOND = ITEMS.registerSimpleItem("natural_diamond");
    public static final DeferredItem<Item> PYROPE = ITEMS.registerSimpleItem("pyrope");
    public static final DeferredItem<Item> CHROMITE = ITEMS.registerSimpleItem("chromite");
    public static final DeferredItem<Item> ILMENITE = ITEMS.registerSimpleItem("ilmenite");
    public static final DeferredItem<Item> KIMBERLITE_MAP = ITEMS.register("kimberlite_map",
            () -> new KimberliteMapItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> IRON_PAN = ITEMS.register("iron_pan",
            () -> new PanItem(new Item.Properties().durability(128)));

    private ModItems() {
    }

    /**
     * Registers the items and their creative tab entries on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    /**
     * Gives all plain items of the mod in registration order.
     *
     * @return items for the creative tab
     */
    public static Collection<DeferredHolder<Item, ? extends Item>> items() {
        return ITEMS.getEntries();
    }
}
