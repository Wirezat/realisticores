package de.wirezat.realisticores;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.Pipe;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Operator command that lists the nearest kimberlite pipes. */
public final class KimberliteCommand {

    private KimberliteCommand() {
    }

    /**
     * Registers {@code /realisticores pipes}.
     *
     * @param dispatcher the server command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(RealisticOres.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("pipes").executes(KimberliteCommand::listPipes)));
    }

    private static int listPipes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 origin = source.getPosition();
        List<Pipe> pipes = nearest(level.getSeed(), origin);
        if (pipes.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.realisticores.pipes.none"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.realisticores.pipes.header", pipes.size()), false);
        for (Pipe pipe : pipes) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pipe.x(), pipe.z());
            String teleport = "/tp @s " + pipe.x() + " " + y + " " + pipe.z();
            Component entry = Component.translatable("command.realisticores.pipes.entry",
                    pipe.x(), pipe.z(), pipe.diameter(), (int) distance(pipe, origin))
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleport)));
            source.sendSuccess(() -> entry, false);
        }
        return pipes.size();
    }

    private static List<Pipe> nearest(long seed, Vec3 origin) {
        int originX = (int) origin.x;
        int originZ = (int) origin.z;
        int maxResults = OreConfig.commandMaxResults();
        List<Pipe> pipes = List.of();
        for (int radius = OreConfig.commandStartRadius();
                radius <= OreConfig.commandSearchLimit() && pipes.size() < maxResults; radius *= 2) {
            int searchRadius = radius;
            pipes = Kimberlite.generator()
                    .pipesNear(seed, originX - radius, originZ - radius, originX + radius, originZ + radius)
                    .stream()
                    .filter(pipe -> distance(pipe, origin) <= searchRadius)
                    .sorted(Comparator.comparingDouble(pipe -> distance(pipe, origin)))
                    .limit(maxResults)
                    .toList();
        }
        return pipes;
    }

    private static double distance(Pipe pipe, Vec3 origin) {
        return Math.hypot(pipe.x() - origin.x, pipe.z() - origin.z);
    }
}
