package com.endeleya.kazisafex.agent.graph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Final, stable envelope returned by {@code AgentEngine.execute(...)} to the caller.
 *
 * @param status            outcome of the whole orchestration
 * @param message           final user-facing message
 * @param structuredOutput  formatted structured answer when the run succeeded
 * @param executionContext  raw legacy execution traces (per tool)
 * @param iterationsUsed    number of critic/planner iterations actually consumed
 * @param abortReason       blocking/failure reason when status is not SUCCESS
 * @param enterpriseUid     enterprise identity ({@code eUid}) the run was confined to
 * @param engagementId      engagement/contract id the run was confined to
 * @param proposedPlan      plan awaiting approval (PROPOSAL status only)
 * @param proposalToken     single-use token to confirm the proposal
 * @param stepStatuses      per-step todo feedback (pending/running/done/error)
 */
public record AgentResponse(
        Status status,
        String message,
        StructuredAnswer structuredOutput,
        Map<String, Object> executionContext,
        int iterationsUsed,
        String abortReason,
        String enterpriseUid,
        String engagementId,
        ExecutionPlan proposedPlan,
        String proposalToken,
        Map<String, String> stepStatuses) {

    /**
     * Outcome of an orchestration run.
     */
    public enum Status {
        /** Plan executed, critic validated and formatter produced a structured output. */
        SUCCESS,
        /** Input intercepted by the guardrail (security / policy violation). */
        BLOCKED,
        /** Execution failed, was abandoned, or the token budget ran out. */
        FAILED,
        /** A plan was produced and is awaiting human approval (human-in-the-loop). */
        PROPOSAL
    }

    /**
     * Builds a successful response.
     *
     * @param structuredOutput formatted answer
     * @param executionContext legacy traces
     * @param iterationsUsed   consumed iterations
     * @param enterpriseUid    enterprise identity the run was confined to
     * @param engagementId     engagement/contract id the run was confined to
     * @param stepStatuses     per-step todo statuses
     * @return a SUCCESS response
     */
    public static AgentResponse success(StructuredAnswer structuredOutput,
            Map<String, Object> executionContext, int iterationsUsed,
            String enterpriseUid, String engagementId, Map<String, String> stepStatuses) {
        return new AgentResponse(Status.SUCCESS,
                structuredOutput == null ? null : structuredOutput.answer(),
                structuredOutput, executionContext, iterationsUsed, null,
                enterpriseUid == null ? "" : enterpriseUid,
                engagementId == null ? "" : engagementId,
                null, null,
                stepStatuses == null ? Map.of() : stepStatuses);
    }

    /**
     * Builds a blocked response (guardrail interception).
     *
     * @param reason        blocking reason
     * @param enterpriseUid enterprise identity the run was confined to
     * @param engagementId  engagement/contract id the run was confined to
     * @return a BLOCKED response
     */
    public static AgentResponse blocked(String reason, String enterpriseUid, String engagementId) {
        return new AgentResponse(Status.BLOCKED, reason, null, Map.of(), 0, reason,
                enterpriseUid == null ? "" : enterpriseUid,
                engagementId == null ? "" : engagementId,
                null, null, Map.of());
    }

    /**
     * Builds a failed response (abandon, budget de tokens épuisé, ou erreur fatale).
     *
     * @param reason          failure reason
     * @param executionContext partial traces
     * @param iterationsUsed  consumed iterations
     * @param enterpriseUid   enterprise identity the run was confined to
     * @param engagementId    engagement/contract id the run was confined to
     * @param stepStatuses    per-step todo statuses
     * @return a FAILED response
     */
    public static AgentResponse failed(String reason, Map<String, Object> executionContext,
            int iterationsUsed, String enterpriseUid, String engagementId,
            Map<String, String> stepStatuses) {
        return new AgentResponse(Status.FAILED, reason, null, executionContext, iterationsUsed, reason,
                enterpriseUid == null ? "" : enterpriseUid,
                engagementId == null ? "" : engagementId,
                null, null,
                stepStatuses == null ? Map.of() : stepStatuses);
    }

    /**
     * Builds a proposal response (plan awaiting human approval). Every planned step
     * starts with a {@code pending} status so the UI can render the todo list.
     *
     * @param plan          the proposed execution plan
     * @param token         single-use approval token
     * @param enterpriseUid enterprise identity the run will be confined to
     * @param engagementId  engagement/contract id the run will be confined to
     * @return a PROPOSAL response
     */
    public static AgentResponse proposal(ExecutionPlan plan, String token,
            String enterpriseUid, String engagementId) {
        Map<String, String> statuses = new LinkedHashMap<>();
        if (plan != null && plan.hasSteps()) {
            plan.steps().forEach(step -> statuses.put(PlanStep.label(step),
                    LegacyExecutorNode.STATUS_PENDING));
        }
        return new AgentResponse(Status.PROPOSAL, "Plan propose en attente d'approbation.",
                null, Map.of(), 0, null,
                enterpriseUid == null ? "" : enterpriseUid,
                engagementId == null ? "" : engagementId,
                plan, token, statuses);
    }
}