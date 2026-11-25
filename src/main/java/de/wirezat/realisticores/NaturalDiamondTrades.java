package de.wirezat.realisticores;

import java.util.Map;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

/** Lets the villagers that buy synthetic diamonds buy natural ones under the same terms. */
public final class NaturalDiamondTrades {
    private static final int COUNT = 1;
    private static final int MAX_USES = 12;
    private static final Map<VillagerProfession, Terms> TERMS = Map.of(
            VillagerProfession.ARMORER, new Terms(3, 20),
            VillagerProfession.TOOLSMITH, new Terms(4, 30),
            VillagerProfession.WEAPONSMITH, new Terms(4, 30));

    private NaturalDiamondTrades() {
    }

    private record Terms(int level, int experience) {
    }

    /**
     * Adds the trade to the villagers that buy diamonds.
     *
     * @param event the villager trades event
     */
    public static void onTrades(VillagerTradesEvent event) {
        Terms terms = TERMS.get(event.getType());
        if (terms != null) {
            event.getTrades().get(terms.level()).add(new VillagerTrades.EmeraldForItems(
                    ModItems.NATURAL_DIAMOND, COUNT, MAX_USES, terms.experience()));
        }
    }
}
