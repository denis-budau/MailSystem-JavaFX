package com.unito.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
    private static final int MAX_THREADS = 10; // Numero massimo di thread nel pool
    private ExecutorService executorService;
    private static ThreadPoolManager instance;

    public ThreadPoolManager() {
        // Crea un thread pool con 10 thread
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    // Metodo per inviare una richiesta al thread pool
    public void submitRequest(ClientRequest clientRequest) {
        executorService.submit(clientRequest); // Aggiungi ClientRequest al pool
    }

    public void submit(Runnable task) {
        executorService.submit(task);
    }

    // Metodo per arrestare il pool quando non è più necessario
    public void shutDown() {
        executorService.shutdown();
    }
}