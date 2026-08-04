package com.endeleya.ia;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memoire locale en memoire vive (fallback de Gratien quand Redis est
 * indisponible). Stocke par cle de session un historique borne aux
 * {@code maxMessages} derniers messages. Thread-safe.
 */
public class InMemoryStorage {

    private final int maxMessages;
    private final Map<String, Deque<String>> storage = new ConcurrentHashMap<>();

    public InMemoryStorage() {
        this(40);
    }

    public InMemoryStorage(int maxMessages) {
        this.maxMessages = Math.max(1, maxMessages);
    }

    public boolean isAvailable() {
        return true;
    }

    public void recheck() {
        // Toujours disponible : rien a tester.
    }

    public void append(String key, String payload) {
        if (key == null || key.isBlank() || payload == null || payload.isBlank()) {
            return;
        }
        Deque<String> queue = storage.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (queue) {
            queue.addLast(payload);
            while (queue.size() > maxMessages) {
                queue.removeFirst();
            }
        }
    }

    public List<String> recent(String key, int limit) {
        if (key == null || key.isBlank()) {
            return List.of();
        }
        Deque<String> queue = storage.getOrDefault(key, new ArrayDeque<>());
        synchronized (queue) {
            List<String> messages = new ArrayList<>(queue);
            int safeLimit = Math.max(1, Math.min(limit, maxMessages));
            return messages.size() <= safeLimit
                    ? messages
                    : messages.subList(messages.size() - safeLimit, messages.size());
        }
    }

    public void replaceRaw(String key, List<String> payloads) {
        if (key == null || key.isBlank()) {
            return;
        }
        List<String> safePayloads = payloads == null ? List.of() : payloads;
        Deque<String> queue = storage.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (queue) {
            queue.clear();
            for (String payload : safePayloads) {
                queue.addLast(payload == null ? "" : payload);
                while (queue.size() > maxMessages) {
                    queue.removeFirst();
                }
            }
        }
    }

    public void clear(String key) {
        if (key != null) {
            storage.remove(key);
        }
    }
}
