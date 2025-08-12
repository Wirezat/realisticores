package wirezat.realisticores.blocks.machines.cubicpress;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import net.minecraft.registry.DynamicRegistryManager;
import wirezat.realisticores.RealisticOres;

public class CubicPressRecipe implements Recipe<Inventory> {
    public static final RecipeType<CubicPressRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "cubic_press";
        }
    };

    private final Identifier id;
    private final ItemStack input;
    private final ItemStack catalyst;
    private final ItemStack output;
    private final int processingTime;

    public CubicPressRecipe(Identifier id, ItemStack input, ItemStack catalyst, ItemStack output, int processingTime) {
        this.id = id;
        this.input = input;
        this.catalyst = catalyst;
        this.output = output;
        this.processingTime = processingTime;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        boolean hasInput = ItemStack.areItemsEqual(inventory.getStack(0), input) &&
                inventory.getStack(0).getCount() >= input.getCount();

        // Überprüft, ob ein Katalysator erforderlich ist. Wenn der Katalysator des Rezepts leer ist, wird keiner benötigt.
        boolean hasCatalyst = true;
        if (!catalyst.isEmpty()) {
            hasCatalyst = ItemStack.areItemsEqual(inventory.getStack(1), catalyst) &&
                    inventory.getStack(1).getCount() >= catalyst.getCount();
        }

        return hasInput && hasCatalyst;
    }

    // Getter für Input (mit Kopie)
    public ItemStack getInput() { return input.copy(); }
    // Neuer Getter für die Anzahl des Inputs
    public int getInputCount() { return input.getCount(); }

    // Getter für Katalysator (mit Kopie)
    public ItemStack getCatalyst() { return catalyst.copy(); }
    // Neuer Getter für die Anzahl des Katalysators
    public int getCatalystCount() { return catalyst.getCount(); }


    // Umbenannt, um Verwechslungen mit der Interface-Methode zu vermeiden.
    // Dies ist ein einfacher Getter für den Output-ItemStack.
    public ItemStack getOutputItem() { return output.copy(); }

    public int getProcessingTime() { return processingTime; }

    @Override
    public Identifier getId() { return id; }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return this.output.copy();
    }

    // Diese Methode wurde von 'getResult' in 'getOutput' umbenannt, um die erforderliche
    // Methode des Recipe-Interfaces in Fabric 1.20.1 korrekt zu implementieren.
    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }

    @Override
    public RecipeType<?> getType() { return TYPE; }

    @Override
    public boolean isIgnoredInRecipeBook() { return true; }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    public static class Serializer implements RecipeSerializer<CubicPressRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public CubicPressRecipe read(Identifier id, JsonObject json) {
            // Fügt eine Logger-Nachricht hinzu, um zu überprüfen, ob das Rezept geladen wird.
            RealisticOres.LOGGER.info("Attempting to read Cubic Press recipe: " + id.toString());

            JsonObject inputJson = json.getAsJsonObject("input");
            ItemStack input = new ItemStack(
                    JsonHelper.getItem(inputJson, "item"),
                    JsonHelper.getInt(inputJson, "count", 1)
            );

            // Liest den Katalysator, der jetzt optional ist.
            ItemStack catalyst = ItemStack.EMPTY;
            if (json.has("catalyst")) {
                JsonObject catalystJson = json.getAsJsonObject("catalyst");
                catalyst = new ItemStack(
                        JsonHelper.getItem(catalystJson, "item"),
                        JsonHelper.getInt(catalystJson, "count", 1)
                );
            }

            JsonObject outputJson = json.getAsJsonObject("output");
            ItemStack output = new ItemStack(
                    JsonHelper.getItem(outputJson, "item"),
                    JsonHelper.getInt(outputJson, "count", 1)
            );

            int processingTime = JsonHelper.getInt(json, "processingTime", 200);

            return new CubicPressRecipe(id, input, catalyst, output, processingTime);
        }

        @Override
        public CubicPressRecipe read(Identifier id, PacketByteBuf buf) {
            ItemStack input = buf.readItemStack();
            ItemStack catalyst = buf.readItemStack();
            ItemStack output = buf.readItemStack();
            int processingTime = buf.readInt();
            return new CubicPressRecipe(id, input, catalyst, output, processingTime);
        }

        @Override
        public void write(PacketByteBuf buf, CubicPressRecipe recipe) {
            buf.writeItemStack(recipe.input);
            buf.writeItemStack(recipe.catalyst);
            buf.writeItemStack(recipe.output);
            buf.writeInt(recipe.processingTime);
        }
    }
}
