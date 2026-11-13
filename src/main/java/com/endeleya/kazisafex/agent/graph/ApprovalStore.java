package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.EnterpriseIdentity;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * In-memory, thread-safe registry of plans awaiting human approval.
 *
 * <p>A proposal {@code plan(...)} stores a {@link PendingApproval} under a random
 * token; {@code executeApproved(token, identity)} consumes it exactly once and refuses
 * any token that is unknown, expired or whose stored identity does not match the
 * caller. The registry is deliberately local to the process: the orchestrator is an
 * embedded runtime behind the desktop chat, so a single node suffices.</p>
 */
public final class ApprovalStore {

    /** Maximum age of a pending approval before it is refused. */
    public static final long MAX_AGE_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private final ConcurrentHashMap<String, PendingApproval> pending = new ConcurrentHashMap<>();

    /**
     * Stores a new approval and returns its single-use token.
     *
     * @param userQuery original user input
     * @param plan      proposal awaiting consent
     * @param identity  identity the run will be confined to
     * @return the opaque approval token to show to the user
     */
    public String store(String userQuery, ExecutionPlan plan, EnterpriseIdentity identity) {
        String token = UUID.randomUUID().toString();
        pending.put(token, PendingApproval.of(userQuery, plan, identity));
        return token;
    }

    /**
     * Atomically consumes a pending approval.
     *
     * @param token the approval token
     * @return the pending approval, or empty when unknown
     */
    public Optional<PendingApproval> take(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(pending.remove(token));
    }

    /** Whether the approval is too old to be honoured. */
    public static boolean expired(PendingApproval approval) {
        return System.currentTimeMillis() - approval.createdAt() > MAX_AGE_MILLIS;
    }

    /** Number of proposals still awaiting consent (telemetry). */
    public int pendingCount() {
        return pending.size();
    }
}