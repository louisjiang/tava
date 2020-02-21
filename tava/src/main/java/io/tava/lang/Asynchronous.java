package io.tava.lang;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface Asynchronous<T> extends Synchronous<T> {

    default CompletableFuture<T> async(ExecutorService executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Asynchronous.this.apply();
            } catch (Throwable cause) {
                throw new RuntimeException(cause);
            }
        }, executor);
    }

}
