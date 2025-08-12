package wirezat.realisticores.blocks.machines.cubicpress;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import wirezat.realisticores.ModBlocks;

public class CubicPressScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    // SERVER-side constructor: inventory and propertyDelegate are provided by the BlockEntity
    public CubicPressScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModBlocks.CUBIC_PRESS_SCREEN_HANDLER_TYPE, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        // Input slots
        this.addSlot(new Slot(inventory, 0, 56, 17));
        this.addSlot(new Slot(inventory, 1, 56, 53));
        this.addSlot(new Slot(inventory, 2, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        // Register PropertyDelegate so values (processingTime, total) are synced to the client.
        if (this.propertyDelegate != null) {
            this.addProperties(this.propertyDelegate);
        }
    }

    // CLIENT-side constructor: use a dummy inventory and an ArrayPropertyDelegate to receive synced values.
    public CubicPressScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        // call the primary constructor first
        this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(2));
        // consume server-sent BlockPos (server writes it); we don't need it client-side here
        try {
            buf.readBlockPos();
        } catch (Exception ignored) {
            // defensive: if server didn't send it for some reason, ignore — but log in dev if needed
        }
    }

    /**
     * Returns the progress width in pixels for the progress arrow.
     * arrowWidth must match the full arrow width in your GUI texture.
     */
    public int getScaledProgress() {
        int progress = this.propertyDelegate.get(0);
        int max = this.propertyDelegate.get(1);
        int arrowWidth = 24;

        if (max <= 0 || progress <= 0) {
            return 0;
        }

        // Use floating-point division for more precise scaling
        // and cast the result back to an int.
        return (int) (((float) progress / (float) max) * arrowWidth);
    }

    @Override
    public ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int slotIndex) {
        ItemStack originalStack = ItemStack.EMPTY;
        // defensive null check
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack newStack = slot.getStack();
            originalStack = newStack.copy();

            if (slotIndex < this.inventory.size()) {
                if (!this.insertItem(newStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(newStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (newStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return originalStack;
    }

    @Override
    public boolean canUse(net.minecraft.entity.player.PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
