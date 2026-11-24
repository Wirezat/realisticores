package de.wirezat.realisticores;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registry of the mod's creative tab. */
public final class ModTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RealisticOres.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RealisticOres.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.NATURAL_DIAMOND.get()))
                    .displayItems((parameters, output) -> {
                        ModBlocks.blockItems().forEach(item -> output.accept(item.get()));
                        ModItems.items().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    private ModTabs() {
    }

    /**
     * Registers the creative tab on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
