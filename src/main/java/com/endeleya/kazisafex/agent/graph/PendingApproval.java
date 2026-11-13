package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.EnterpriseIdentity;
import java.io.Serial;
import java.io.Serializable;

/**
 * A plan awaiting human approval in the human-in-the-loop flow.
 *
 * <p>Captured when {@code AgentEngine.plan(...)} produces a proposal: the query and
 * plan together with the enterprise identity that requested it. The approval token is
 * later presented to {@code AgentEngine.executeApproved(...)}, which only resumes the
 * graph when the stored identity matches the caller's identity — the plan can never be
 * executed under a different enterprise/engagement than the one that planned it.</p>
 *
 * @param userQuery  original user input
 * @param plan       proposed, not-yet-executed execution plan
 * @param identity   identity confinement of the run at planning time
 * @param createdAt  epoch millis used for TTL invalidation
 */
public record PendingApproval(
        String userQuery,
        ExecutionPlan plan,
        EnterpriseIdentity identity,
        long createdAt) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Factory capturing the current instant.
     *
     * @param userQuery original user input
     * @param plan      proposed plan
     * @param identity  identity confinement
     * @return a new pending approval stamped now
     */
    public static PendingApproval of(String userQuery, ExecutionPlan plan, EnterpriseIdentity identity) {
        return new PendingApproval(userQuery, plan, identity, System.currentTimeMillis());
    }
}