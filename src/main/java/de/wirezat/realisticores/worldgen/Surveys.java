package de.wirezat.realisticores.worldgen;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Background thread for surveys that measure many deposits, and the log of failed background tasks. */
public final class Surveys {
    private static final Logger LOGGER = LoggerFactory.getLogger(Surveys.class);
    private static final Executor EXECUTOR = new ForkJoinPool(1);

    private Surveys() {
    }

    /**
     * Gives the background thread for surveys such as the locate command.
     *
     * @return executor with one thread
     */
    public static Executor executor() {
        return EXECUTOR;
    }

    /**
     * Logs the failure of a background task; meant for {@link java.util.concurrent.CompletableFuture#whenComplete}.
     *
     * @param result result of the task, ignored
     * @param failure what went wrong, or null if the task succeeded
     */
    public static void logFailure(Object result, Throwable failure) {
        if (failure != null) {
            LOGGER.error("Background survey task failed", failure);
        }
    }
}
