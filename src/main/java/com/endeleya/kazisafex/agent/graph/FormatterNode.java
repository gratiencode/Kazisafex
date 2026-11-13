package com.endeleya.kazisafex.agent.graph;

import com.endeleya.kazisafex.agent.JsonSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Final node: converts the raw legacy outputs into the strongly-typed
 * {@link StructuredAnswer}.
 *
 * <p>Raw payloads are normalized (JSON parsed when possible) into {@code data}, a
 * human-readable Markdown answer is assembled, and anomalies are listed in {@code
 * warnings}. The result is stored in {@code finalStructuredOutput}.</p>
 */
public final class FormatterNode implements NodeAction<OrchestratorState> {

    private static final Logger LOG = LoggerFactory.getLogger(FormatterNode.class);

    private final AgentAssistants.Assistants assistants;

    /**
     * Creates the formatter.
     *
     * @param assistants AiServices proxies (may be disabled)
     */
    public FormatterNode(AgentAssistants.Assistants assistants) {
        this.assistants = assistants;
    }

    @Override
    public Map<String, Object> apply(OrchestratorState state) {
        Map<String, Object> context = state.legacyContext();
        StructuredAnswer answer = assistants.enabled()
                ? formatFromLlm(context)
                : formatDeterministic(context);
        LOG.info("Formateur -> {}{}", answer.summary(),
                answer.warnings().isEmpty() ? "" : " warnings=" + answer.warnings());

        return Map.of(
                "finalStructuredOutput", answer,
                "messages", List.of(TranscriptEntry.system(
                        "Reponse finale structuree de l'orchestrateur.")));
    }

    private StructuredAnswer formatDeterministic(Map<String, Object> context) {
        Map<String, Object> data = new LinkedHashMap<>();
        StringBuilder answer = new StringBuilder();
        java.util.ArrayList<String> warnings = new java.util.ArrayList<>();

        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String tool = entry.getKey();
            if ("iterations".equals(tool)) {
                continue;
            }
            Object parsed = JsonSupport.tryParse(entry.getValue());
            data.put(tool, parsed);
            appendToolSection(answer, warnings, tool, parsed);
        }

        String summary = "Execution de " + data.size() + " etape(s) legacy validee par le critique.";
        if (context.containsKey("iterations")) {
            summary += " (iterations: " + context.get("iterations") + ")";
        }
        return new StructuredAnswer(summary, answer.toString().strip(),
                Map.copyOf(data), List.copyOf(warnings));
    }

    private void appendToolSection(StringBuilder out, java.util.ArrayList<String> warnings,
            String tool, Object parsed) {
        out.append("## ").append(tool).append('\n');
        if (parsed instanceof List<?> rows) {
            appendTable(out, rows);
        } else if (parsed instanceof Map<?, ?> row) {
            row.forEach((key, value) -> out.append("- ").append(key).append(" : ")
                    .append(value).append('\n'));
        } else {
            out.append(parsed).append('\n');
        }
        out.append('\n');
    }

    private void appendTable(StringBuilder out, List<?> rows) {
        String header = rows.isEmpty() ? "-" : tableHeader(rows.get(0));
        out.append(header).append('\n');
        out.append(header.replaceAll("[^|]", "-").replace("-", "---")).append('\n');
        if (!rows.isEmpty()) {
            out.append(dataRow(rows.get(0)));
        }
    }

    private String tableHeader(Object firstRow) {
        if (firstRow instanceof Map<?, ?> map) {
            List<String> headers = map.keySet().stream().map(String::valueOf).collect(Collectors.toList());
            return "| " + String.join(" | ", headers) + " |";
        }
        return "| valeur |";
    }

    private String dataRow(Object firstRow) {
        if (firstRow instanceof Map<?, ?> map) {
            List<String> values = map.values().stream().map(String::valueOf).collect(Collectors.toList());
            return "| " + String.join(" | ", values) + " |";
        }
        return "| " + firstRow + " |";
    }

    private StructuredAnswer formatFromLlm(Map<String, Object> context) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if ("iterations".equals(entry.getKey())) {
                continue;
            }
            data.put(entry.getKey(), JsonSupport.tryParse(entry.getValue()));
        }
        String results = context.entrySet().stream()
                .filter(e -> !"iterations".equals(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));
        AgentAssistants.FormatDraft draft = assistants.formatter().format(
                JsonSupport.trimTo(results, 12_000));
        String summary = draft == null ? "Reponse formatee." : draft.summary();
        String answerText = draft == null || draft.answerText() == null ? "" : draft.answerText();
        return new StructuredAnswer(summary, answerText, Map.copyOf(data), List.of());
    }
}