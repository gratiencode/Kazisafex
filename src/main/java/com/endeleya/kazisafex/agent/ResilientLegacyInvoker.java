package com.endeleya.kazisafex.agent;

import com.endeleya.kazisafex.agent.legacy.LegacyExecutionException;
import com.endeleya.kazisafex.agent.legacy.LegacyToolDescriptor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.core.functions.CheckedSupplier;

/**
 * Guards every legacy call with resilience and virtual threads.
 *
 * <p>Legacy workflows are synchronous and can be slow or failing (timeouts, empty
 * results, DB hiccups). This invoker wraps each call in:</p>
 * <ol>
 *   <li>a <b>virtual thread</b> so the caller thread (a pinned platform thread of the
 *       graph runner) never blocks while the legacy call runs;</li>
 *   <li>a <b>circuit breaker</b> (per tool) that opens after repeated failures and
 *       short-circuits quickly instead of piling up calls on a degraded legacy;</li>
 *   <li>a <b>retry</b> policy that retries transient failures with backoff;</li>
 *   <li>a <b>fallback</b> converting failures ({@code null} / exceptions) into a
 *       structured error payload so the Critic node can react.</li>
 * </ol>
 *
 * <p>Instances are thread-safe and shared across graph executions.</p>
 */
public final class ResilientLegacyInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(ResilientLegacyInvoker.class);
    private static final Duration INVOCATION_TIMEOUT = Duration.ofSeconds(60);

    private final ExecutorService virtualThreads;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();
    private final Map<String, Retry> retries = new ConcurrentHashMap<>();

    /**
     * Creates the invoker owning a dedicated virtual-thread executor.
     */
    public ResilientLegacyInvoker() {
        this(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Creates the invoker over a provided executor (allows sharing/cleanup control).
     *
     * @param virtualThreads executor used to run blocking legacy calls
     */
    public ResilientLegacyInvoker(ExecutorService virtualThreads) {
        this.virtualThreads = virtualThreads;
        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .build());
        this.retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .retryExceptions(LegacyExecutionException.class)
                .build());
    }

    /**
     * Runs a legacy capability with retry, circuit breaker and virtual thread dispatch.
     *
     * @param tool        the legacy descriptor to invoke
     * @param arguments   resolved arguments
     * @return the raw legacy result, or a fallback failure payload when it fails
     */
    public Object invoke(LegacyToolDescriptor tool, Map<String, Object> arguments) {
        CircuitBreaker breaker = breakers.computeIfAbsent(tool.name(), circuitBreakerRegistry::circuitBreaker);
        Retry retry = retries.computeIfAbsent(tool.name(), retryRegistry::retry);

        CheckedSupplier<Object> guarded = CircuitBreaker.decorateCheckedSupplier(breaker,
                Retry.decorateCheckedSupplier(retry,
                        () -> runOnVirtualThread(tool, arguments)));

        try {
            return guarded.get();
        } catch (Throwable ex) {
            LOG.warn("Echec legacy {} -> {}", tool.name(), ex.toString());
            return failurePayload(tool, ex);
        }
    }

    private Object runOnVirtualThread(LegacyToolDescriptor tool, Map<String, Object> arguments) throws LegacyExecutionException {
        Future<Object> future = virtualThreads.submit(() -> {
            try {
                return tool.invocation().apply(arguments);
            } catch (Exception ex) {
                throw new LegacyExecutionException("Echec du workflow '" + tool.name() + "'", ex);
            }
        });
        try {
            Object result = future.get(INVOCATION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (result == null) {
                throw new LegacyExecutionException("Le workflow '" + tool.name() + "' a retourne null");
            }
            return result;
        } catch (java.util.concurrent.TimeoutException ex) {
            future.cancel(true);
            throw new LegacyExecutionException("Timeout ("
                    + INVOCATION_TIMEOUT.toMillis() + " ms) sur le workflow '" + tool.name() + "'", ex);
        } catch (java.util.concurrent.ExecutionException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof LegacyExecutionException legacy) {
                throw legacy;
            }
            throw new LegacyExecutionException("Echec du workflow '" + tool.name() + "'", cause);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new LegacyExecutionException("Interruption du workflow '" + tool.name() + "'", ex);
        }
    }

    private Object failurePayload(LegacyToolDescriptor tool, Throwable ex) {
        return "{\"error\":\"legacy-invocation-failed\",\"tool\":\"" + tool.name()
                + "\",\"message\":\"" + sanitize(ex.getMessage()) + "\"}";
    }

    private String sanitize(String message) {
        if (message == null) {
            return "cause inconnue";
        }
        return message.replace("\"", "'").replace("\n", " ").replace("\r", " ");
    }

    /** Shuts down the owned virtual-thread executor. */
    public void close() {
        virtualThreads.shutdown();
    }
}