package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.RealisticOres;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components of the pan and the tag of the blocks it can wash. */
public final class ModPan {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RealisticOres.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> ID =
            COMPONENTS.registerComponentType("pan_id",
                    builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Grains>> LAST =
            COMPONENTS.registerComponentType("last_grains",
                    builder -> builder.persistent(Grains.CODEC).networkSynchronized(Grains.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Grains>> CONTENT =
            COMPONENTS.registerComponentType("pan_content",
                    builder -> builder.persistent(Grains.CODEC).networkSynchronized(Grains.STREAM_CODEC));

    public static final TagKey<Block> PANNABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "pannable"));

    private ModPan() {
    }

    /**
     * Registers the components on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
