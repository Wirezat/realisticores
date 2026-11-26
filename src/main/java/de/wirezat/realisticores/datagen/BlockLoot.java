package de.wirezat.realisticores.datagen;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import de.wirezat.realisticores.coal.CoalPiecesBlock;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Loot tables of all blocks of the mod. */
final class BlockLoot extends BlockLootSubProvider {
    private static final float BLACKBAND_COAL_CHANCE = 0.25F;

    /**
     * Creates the provider.
     *
     * @param registries registries of the generated pack
     */
    BlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        Resources.RESOURCES.forEach(resource -> dropSelf(resource.block().get()));
        Resources.ROCKS.stream().map(rock -> rock.block().get())
                .filter(block -> block != ModBlocks.COALY_GRASS_BLOCK.get()
                        && block != ModBlocks.LIGNITIC_GRASS_BLOCK.get())
                .forEach(this::dropSelf);
        add(ModBlocks.COALY_GRASS_BLOCK.get(),
                block -> createSingleItemTableWithSilkTouch(block, ModBlocks.COALY_DIRT.get()));
        add(ModBlocks.LIGNITIC_GRASS_BLOCK.get(),
                block -> createSingleItemTableWithSilkTouch(block, ModBlocks.LIGNITIC_DIRT.get()));
        Set<Block> special = Set.of(ModBlocks.BURNING_COAL_SEAM.get(), ModBlocks.BURNING_LIGNITE_SEAM.get(),
                ModBlocks.BLACKBAND_IRONSTONE.get());
        Resources.ORES.stream().filter(ore -> !special.contains(ore.block().get()))
                .forEach(ore -> add(ore.block().get(), block -> oreDrop(block, ore)));
        add(ModBlocks.BURNING_COAL_SEAM.get(), block -> burningSeam(block, ModBlocks.COAL_SEAM.get(), Items.COAL));
        add(ModBlocks.BURNING_LIGNITE_SEAM.get(),
                block -> burningSeam(block, ModBlocks.LIGNITE_SEAM.get(), ModItems.LIGNITE.get()));
        add(ModBlocks.BLACKBAND_IRONSTONE.get(), block -> createOreDrop(block, ModItems.RAW_SIDERITE.get())
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.COAL))
                        .when(doesNotHaveSilkTouch())
                        .when(LootItemRandomChanceCondition.randomChance(BLACKBAND_COAL_CHANCE))
                        .when(ExplosionCondition.survivesExplosion())));
        for (CoalPiecesBlock pieces : ModBlocks.coalPieces()) {
            add(pieces, this::pieces);
        }
    }

    /**
     * Builds the table of an ore: the ore itself with silk touch, its mineral otherwise.
     *
     * @param block the ore block
     * @param ore what the ore drops
     * @return the loot table
     */
    private LootTable.Builder oreDrop(Block block, Resources.Ore ore) {
        if (ore.maxDrops() == 1) {
            return createOreDrop(block, ore.drop().asItem());
        }
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(ore.drop())
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, ore.maxDrops())))
                .apply(ApplyBonusCount.addOreBonusCount(
                        registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)))));
    }

    /**
     * Builds the table of a burning seam: the unburnt seam with silk touch, its coal otherwise.
     *
     * @param block the burning seam
     * @param seam the seam block dropped with silk touch
     * @param coal the coal dropped otherwise, multiplied by fortune
     * @return the loot table
     */
    private LootTable.Builder burningSeam(Block block, Block seam, Item coal) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(seam).when(hasSilkTouch()).otherwise(applyExplosionDecay(block,
                        LootItem.lootTableItem(coal).apply(ApplyBonusCount.addOreBonusCount(
                                registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)))))));
    }

    /**
     * Builds the table of loose coal: one item per piece lying on the block.
     *
     * @param block the loose coal block
     * @return the loot table
     */
    private LootTable.Builder pieces(Block block) {
        LootItem.Builder<?> entry = LootItem.lootTableItem(((CoalPiecesBlock) block).item());
        for (int count = 2; count <= CoalPiecesBlock.MAX_PIECES; count++) {
            entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                            StatePropertiesPredicate.Builder.properties().hasProperty(CoalPiecesBlock.PIECES,
                                    count))));
        }
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(applyExplosionDecay(block, entry)));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(RealisticOres.MOD_ID))
                .map(Map.Entry::getValue).toList();
    }
}
