package de.wirezat.realisticores;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.wirezat.realisticores.coal.Basin;
import de.wirezat.realisticores.coal.Coal;
import de.wirezat.realisticores.coal.ResolvedBasin;
import de.wirezat.realisticores.indicator.LevelTerrainProbe;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.phys.Vec3;

/** Operator command that lists the nearest coal basins. */
public final class BasinCommand {
    private static final int SEARCH_LIMIT = 100000;

    private BasinCommand() {
    }

    private record Target(ResolvedBasin basin, int y) {
    }

    /**
     * Registers {@code /realisticores basins}.
     *
     * @param dispatcher the server command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(RealisticOres.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("basins").executes(BasinCommand::listBasins)));
    }

    private static int listBasins(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 origin = source.getPosition();
        CompletableFuture.supplyAsync(() -> nearest(level, origin), Coal.surveyor())
                .thenAcceptAsync(targets -> report(source, origin, targets), level.getServer())
                .whenComplete(Kimberlite::logFailure);
        return 1;
    }

    private static void report(CommandSourceStack source, Vec3 origin, List<Target> targets) {
        if (targets.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.realisticores.basins.none"), false);
            return;
        }
        source.sendSuccess(() -> Component.translatable("command.realisticores.basins.header", targets.size()), false);
        for (Target target : targets) {
            ResolvedBasin resolved = target.basin();
            Basin basin = resolved.basin();
            String teleport = "/tp @s " + basin.x() + " " + target.y() + " " + basin.z();
            Component type = Component.translatable("command.realisticores.basins.type."
                    + resolved.type().name().toLowerCase(Locale.ROOT));
            Component entry = Component.translatable("command.realisticores.basins.entry", basin.x(), basin.z(), type,
                    (int) (2.0 * Math.sqrt(basin.longRadius() * basin.shortRadius())), (int) distance(resolved, origin))
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleport)));
            source.sendSuccess(() -> entry, false);
        }
    }

    private static List<Target> nearest(ServerLevel level, Vec3 origin) {
        if (level.dimension() != Level.OVERWORLD
                || !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return List.of();
        }
        LevelTerrainProbe probe = new LevelTerrainProbe(level, generator);
        return Coal.nearest(level.getSeed(), probe, OreConfig.resolved().coalBlocks().keySet(), (int) origin.x,
                (int) origin.z, Math.min(SEARCH_LIMIT, OreConfig.commandSearchLimit()), OreConfig.commandMaxResults())
                .stream()
                .map(basin -> new Target(basin, Math.max(level.getSeaLevel(),
                        probe.terrainHeight(basin.basin().x(), basin.basin().z()) + 1)))
                .toList();
    }

    private static double distance(ResolvedBasin basin, Vec3 origin) {
        return Math.hypot(basin.basin().x() - origin.x, basin.basin().z() - origin.z);
    }
}
