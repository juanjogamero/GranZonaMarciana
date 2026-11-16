package com.granzonamarciana.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Cola compartida para las operaciones que deben mantener orden de escritura. */
public final class DatabaseExecutor {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private DatabaseExecutor() {}
    public static void execute(Runnable task) { EXECUTOR.execute(task); }
}
