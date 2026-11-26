package de.wirezat.realisticores.ore;

import de.wirezat.realisticores.coal.CoalBasin;
import de.wirezat.realisticores.kimberlite.KimberlitePipe;
import java.util.ArrayList;
import java.util.List;

/** All deposit kinds of the mod, in the order they are carved into a chunk. */
public final class Deposits {
    public static final List<DepositType> ALL = List.of(KimberlitePipe.INSTANCE, CoalBasin.INSTANCE);

    private Deposits() {
    }

    /**
     * Checks the shipped data against the deposit kinds: every kind has a data file and every file a kind, and
     * every mineral names only zones its deposit knows.
     *
     * @param registry the shipped ores and deposits
     * @param types the deposit kinds
     * @throws IllegalStateException listing every problem found
     */
    public static void validate(DepositRegistry registry, List<DepositType> types) {
        List<String> problems = new ArrayList<>();
        for (DepositType type : types) {
            Deposit deposit = registry.deposits().get(type.id());
            if (deposit == null) {
                problems.add("no data file deposits/" + type.id() + ".json");
                continue;
            }
            for (Mineral mineral : deposit.minerals()) {
                mineral.zones().stream().filter(zone -> !type.zones().contains(zone)).forEach(zone ->
                        problems.add(type.id() + ": " + mineral.ore() + " names unknown zone " + zone));
                mineral.zoneBlocks().keySet().stream().filter(zone -> !mineral.zones().contains(zone)).forEach(zone ->
                        problems.add(type.id() + ": " + mineral.ore() + " has a block for zone " + zone
                                + " it does not occur in"));
            }
        }
        registry.deposits().keySet().stream()
                .filter(id -> types.stream().noneMatch(type -> type.id().equals(id)))
                .forEach(id -> problems.add("deposits/" + id + ".json has no deposit kind in the mod"));
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Invalid deposit data: " + String.join("; ", problems));
        }
    }
}
