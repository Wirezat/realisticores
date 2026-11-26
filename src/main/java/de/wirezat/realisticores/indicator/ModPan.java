package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.RealisticOres;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components and water tank of the pan and the tag of the blocks it can wash. */
public final class ModPan {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RealisticOres.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> ID =
            COMPONENTS.registerComponentType("pan_id",
                    builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Grains>> LAST =
            COMPONENTS.registerComponentType("last_grains",
                    builder -> builder.persistent(Grains.CODEC).networkSynchronized(Grains.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GradeTone>> TONE =
            COMPONENTS.registerComponentType("last_tone",
                    builder -> builder.persistent(GradeTone.CODEC).networkSynchronized(GradeTone.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Grains>> CONTENT =
            COMPONENTS.registerComponentType("pan_content",
                    builder -> builder.persistent(Grains.CODEC).networkSynchronized(Grains.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> WATER =
            COMPONENTS.registerComponentType("pan_water", builder -> builder.persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    public static final TagKey<Block> PANNABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(RealisticOres.MOD_ID, "pannable"));

    private ModPan() {
    }

    /**
     * Registers the components and the water tank on the mod event bus.
     *
     * @param modBus the mod event bus
     */
    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new PanFluidHandler(stack), ModItems.IRON_PAN.get()));
    }

    /** Water tank of a pan that takes nothing but water. */
    static final class PanFluidHandler extends FluidHandlerItemStack {
        /**
         * Creates the tank of a pan.
         *
         * @param pan the pan
         */
        PanFluidHandler(ItemStack pan) {
            super(WATER, pan, PanItem.CAPACITY);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return stack.is(Fluids.WATER);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return isFluidValid(0, fluid);
        }
    }
}
