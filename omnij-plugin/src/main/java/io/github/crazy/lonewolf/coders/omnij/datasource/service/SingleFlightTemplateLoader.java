package io.github.crazy.lonewolf.coders.omnij.datasource.service;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.Template;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.beans.TemplateKey;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class SingleFlightTemplateLoader {

    private final ConcurrentHashMap<TemplateKey, CompletableFuture<Object>> inFlight = new ConcurrentHashMap<>();

    public Object loadCoalesced(TemplateKey key, Supplier<Object> dbAndResolverLoader) {
        CompletableFuture<Object> newFuture = new CompletableFuture<>();
        // Atomic check-and-insert: returns null ONLY for Thread #1!
        CompletableFuture<Object> existingFuture = inFlight.putIfAbsent(key, newFuture);

        if (existingFuture != null) {

            return existingFuture.join();
        }

        // Only Thread #1 reaches here
        try {
            Object result = dbAndResolverLoader.get();
            newFuture.complete(result);
            return result;
        } catch (Throwable ex) {
            newFuture.completeExceptionally(ex);
            throw ex;
        } finally {
            // Always clean up so memory never leaks and future refreshes work
            inFlight.remove(key, newFuture);
        }
    }
}
