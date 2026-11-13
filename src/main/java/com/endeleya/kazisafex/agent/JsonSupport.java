package com.endeleya.kazisafex.agent;

import java.util.Map;

/**
 * Small JSON helpers shared by the orchestrator components.
 */
public final class JsonSupport {

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private JsonSupport() {
    }

    /**
     * Parses a tool-arguments JSON payload into a map; never returns {@code null}.
     *
     * @param json raw JSON, possibly {@code null} or blank
     * @return the parsed map (empty when unparseable)
     */
    public static Map<String, Object> parseArguments(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> parsed = JSON.readValue(
                    json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            return parsed == null ? Map.of() : parsed;
        } catch (Exception ex) {
            return Map.of();
        }
    }

    /**
     * Attempts to parse a raw output as JSON (object or array), returning the parsed
     * structure, or the raw value when it is not JSON.
     *
     * @param value raw legacy output
     * @return normalized structure
     */
    public static Object tryParse(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String text) {
            String trimmed = text.strip();
            if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                    || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                try {
                    return JSON.readValue(trimmed, Object.class);
                } catch (Exception ignored) {
                    return value;
                }
            }
            return value;
        }
        return value;
    }

    /**
     * Caps a raw payload for LLM consumption.
     *
     * @param text  input
     * @param limit maximum characters
     * @return the truncated payload
     */
    public static String trimTo(String text, int limit) {
        if (text == null || text.length() <= limit) {
            return text == null ? "" : text;
        }
        return text.substring(0, limit) + "\n...[tronque]";
    }

    /**
     * Extrait l'objet JSON brut d'une réponse LLM, même entouré de balises Markdown
     * (```json ... ```) ou de texte. Retourne le premier {@code {...}} équilibré, ou
     * {@code null} si la réponse ne contient aucun objet JSON.
     *
     * @param text réponse du modèle
     * @return le fragment JSON, ou {@code null}
     */
    public static String rawJsonObject(String text) {
        if (text == null) {
            return null;
        }
        String s = text.strip();
        if (s.startsWith("```")) {
            s = s.replaceFirst("(?s)^```[a-zA-Z0-9]*\\s*", "").strip();
            s = s.replaceFirst("(?s)\\s*```$", "").strip();
        }
        int start = s.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        int end = -1;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    end = i;
                    break;
                }
            }
        }
        return end < 0 ? null : s.substring(start, end + 1);
    }
}