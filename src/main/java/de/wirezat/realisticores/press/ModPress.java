package de.wirezat.realisticores.press;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.RealisticOres;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/** Registries of the crystal press: block entity, menu, recipe type and seed crystals. */
public final class ModPress {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RealisticOres.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, RealisticOres.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RealisticOres.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RealisticOres.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalPressBlockEntity>>
            PRESS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("crystal_press", () -> BlockEntityType.Builder
                    .of(CrystalPressBlockEntity::new, ModBlocks.CRYSTAL_PRESS.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<CrystalPressMenu>> PRESS_MENU =
            MENUS.register("crystal_press", () -> IMenuTypeExtension.create(CrystalPressMenu::new));
    public static final DeferredHolder<RecipeType<?>, RecipeType<PressRecipe>> PRESS_TYPE =
            RECIPE_TYPES.register("crystal_press",
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "crystal_press")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressRecipe>> PRESS_SERIALIZER =
            RECIPE_SERIALIZERS.register("crystal_press", PressRecipe.Serializer::new);

    public static final DataMapType<Item, PressSeed> SEEDS = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "press_seed"), Registries.ITEM, PressSeed.CODEC)
            .synced(PressSeed.CODEC, true).build();

    private ModPress() {
    }

    /**
     * Registers the press registries, the seed data and the item handler capability on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener((RegisterDataMapTypesEvent event) -> event.register(SEEDS));
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, PRESS_BLOCK_ENTITY.get(),
                (entity, side) -> PressSides.handlerFor(entity.inventory(), side,
                        entity.getBlockState().getValue(CrystalPressBlock.FACING))));
    }
}
