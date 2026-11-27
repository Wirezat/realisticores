package de.wirezat.realisticores.survey;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/** Adds the survey maps to the cartographer's trades and to structure chests, and books to other chests. */
public final class SurveyEvents {
    private static final int TRADE_LEVEL = 3;
    private static final int TRADE_EMERALDS = 16;
    private static final int TRADE_MAX_USES = 2;
    private static final int TRADE_XP = 15;
    private static final float TRADE_PRICE_MULTIPLIER = 0.05F;

    private static final Set<ResourceLocation> MAP_CHESTS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
            ResourceLocation.withDefaultNamespace("chests/desert_pyramid"));
    private static final Set<ResourceLocation> BOOK_CHESTS = Set.of(
            ResourceLocation.withDefaultNamespace("chests/shipwreck_treasure"),
            ResourceLocation.withDefaultNamespace("chests/buried_treasure"));

    private static final List<ResourceKey<LootTable>> MAP_INJECTIONS =
            List.of(injection("kimberlite_map"), injection("porphyry_map"));
    private static final ResourceKey<LootTable> BOOK_INJECTION = injection("trail_sense_book");

    private SurveyEvents() {
    }

    /**
     * Names a loot table that is injected into vanilla chests.
     *
     * @param name table name
     * @return key of the table
     */
    private static ResourceKey<LootTable> injection(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "inject/" + name));
    }

    /**
     * Offers the survey maps at the cartographer.
     *
     * @param event the villager trades event
     */
    public static void onTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            for (Item map : List.of(ModItems.KIMBERLITE_MAP.get(), ModItems.PORPHYRY_MAP.get())) {
                event.getTrades().get(TRADE_LEVEL).add(new BasicItemListing(
                        new ItemStack(Items.EMERALD, TRADE_EMERALDS), new ItemStack(map), TRADE_MAX_USES, TRADE_XP,
                        TRADE_PRICE_MULTIPLIER));
            }
        }
    }

    /**
     * Adds the map and book pools to the matching vanilla chests.
     *
     * @param event the loot table load event
     */
    public static void onLoot(LootTableLoadEvent event) {
        if (MAP_CHESTS.contains(event.getName())) {
            MAP_INJECTIONS.forEach(map -> event.getTable().addPool(pool(map)));
        } else if (BOOK_CHESTS.contains(event.getName())) {
            event.getTable().addPool(pool(BOOK_INJECTION));
        }
    }

    /**
     * Creates a pool that rolls a nested loot table once.
     *
     * @param table the nested table
     * @return the pool
     */
    private static LootPool pool(ResourceKey<LootTable> table) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(NestedLootTable.lootTableReference(table)).build();
    }
}
