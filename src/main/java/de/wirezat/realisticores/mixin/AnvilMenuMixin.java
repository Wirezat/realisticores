package de.wirezat.realisticores.mixin;

import de.wirezat.realisticores.ModItems;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Lets the natural diamond repair everything on the anvil that the synthetic diamond repairs. */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    /**
     * Presents a natural diamond as a synthetic one to the repair check.
     *
     * @param repair stack in the repair slot
     * @return a synthetic diamond for a natural one, else the stack itself
     */
    @ModifyArg(method = "createResult", index = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;isValidRepairItem(Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/item/ItemStack;)Z"))
    private ItemStack realisticores$repairWithNaturalDiamond(ItemStack repair) {
        return repair.is(ModItems.NATURAL_DIAMOND.get()) ? new ItemStack(Items.DIAMOND) : repair;
    }
}
