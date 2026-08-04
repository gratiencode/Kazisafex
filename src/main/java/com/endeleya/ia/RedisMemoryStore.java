package com.endeleya.ia;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisMemoryStore {

    private static final Logger LOGGER = Logger.getLogger(RedisMemoryStore.class.getName());
    private static final int DEFAULT_MAX_MESSAGES = 10;
    private static final int DEFAULT_TTL_SECONDS = 60 * 60 * 24 * 14;

    private final String host;
    private final int port;
    private final int timeoutMillis;
    private final int maxMessages;
    private final int ttlSeconds;
    private final boolean enabled;
    private final InMemoryStorage fallback;
    private volatile boolean redisAvailable;

    public RedisMemoryStore() {
        this(DEFAULT_MAX_MESSAGES);
    }

    /**
     * @param maxMessages limite de messages partagee avec le fallback : la meme
     *                   valeur est appliquee sur Redis et sur InMemoryStorage.
     */
    public RedisMemoryStore(int maxMessages) {
        this.host = setting("kazisafex.redis.host", "REDIS_HOST", "127.0.0.1");
        this.port = intSetting("kazisafex.redis.port", "REDIS_PORT", 6379);
        this.timeoutMillis = intSetting("kazisafex.redis.timeout.ms", "REDIS_TIMEOUT_MS", 1500);
        this.maxMessages = Math.max(1, maxMessages);
        this.ttlSeconds = intSetting("kazisafex.redis.ttl.seconds", "REDIS_TTL_SECONDS", DEFAULT_TTL_SECONDS);
        this.enabled = booleanSetting("kazisafex.redis.enabled", "REDIS_ENABLED", true);
        this.fallback = new InMemoryStorage(this.maxMessages);
        this.redisAvailable = enabled && ping();
    }

    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    /**
     * Re-teste la disponibilite de Redis (appele apres un bootstrap/install).
     * Permet de rebasculer sur Redis si celui-ci demarre en cours de session.
     */
    public void recheck() {
        this.redisAvailable = enabled && ping();
    }

    public void append(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank() || content == null || content.isBlank()) {
            return;
        }
        String key = key(sessionId);
        String payload = serialize(role, content);
        if (enabled && redisAvailable) {
            try {
                command("RPUSH", key, payload);
                command("LTRIM", key, "-" + maxMessages, "-1");
                command("EXPIRE", key, String.valueOf(ttlSeconds));
                return;
            } catch (IOException | RuntimeException ex) {
                redisAvailable = false;
                LOGGER.log(Level.WARNING, "Memoire Redis indisponible, fallback memoire locale: {0}", ex.getMessage());
            }
        }
        appendFallback(key, payload);
    }

    public List<String> recent(String sessionId, int limit) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }
        String key = key(sessionId);
        int safeLimit = Math.max(1, Math.min(limit, maxMessages));
        if (enabled && redisAvailable) {
            try {
                Object value = command("LRANGE", key, "-" + safeLimit, "-1");
                if (value instanceof List<?> values) {
                    List<String> messages = new ArrayList<>();
                    for (Object item : values) {
                        if (item != null) {
                            messages.add(String.valueOf(item));
                        }
                    }
                    return messages;
                }
            } catch (IOException | RuntimeException ex) {
                redisAvailable = false;
                LOGGER.log(Level.WARNING, "Lecture Redis echouee, fallback memoire locale: {0}", ex.getMessage());
            }
        }
        List<String> queue = fallback.recent(key, safeLimit);
        return new ArrayList<>(queue);
    }

    public void replaceRaw(String sessionId, List<String> payloads) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        String key = key(sessionId);
        List<String> safePayloads = payloads == null ? List.of() : payloads;
        if (enabled && redisAvailable) {
            try {
                command("DEL", key);
                for (String payload : safePayloads) {
                    command("RPUSH", key, payload == null ? "" : payload);
                }
                command("LTRIM", key, "-" + maxMessages, "-1");
                command("EXPIRE", key, String.valueOf(ttlSeconds));
                return;
            } catch (IOException | RuntimeException ex) {
                redisAvailable = false;
                LOGGER.log(Level.WARNING, "Ecriture brute Redis echouee, fallback memoire locale: {0}", ex.getMessage());
            }
        }
        fallback.replaceRaw(key, safePayloads);
    }

    public List<String> recentRaw(String sessionId, int limit) {
        return recent(sessionId, limit);
    }

    public void clear(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        String key = key(sessionId);
        if (enabled && redisAvailable) {
            try {
                command("DEL", key);
                return;
            } catch (IOException | RuntimeException ex) {
                redisAvailable = false;
                LOGGER.log(Level.WARNING, "Suppression Redis echouee, fallback memoire locale: {0}", ex.getMessage());
            }
        }
        fallback.clear(key);
    }

    public boolean ping() {
        if (!enabled) {
            return false;
        }
        try {
            Object pong = command("PING");
            return "PONG".equalsIgnoreCase(String.valueOf(pong));
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.INFO, "Redis non disponible pour Gratien: {0}", ex.getMessage());
            return false;
        }
    }

    private void appendFallback(String key, String payload) {
        fallback.append(key, payload);
    }

    private Object command(String... parts) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            OutputStream out = socket.getOutputStream();
            out.write(encode(parts));
            out.flush();
            return readReply(new BufferedInputStream(socket.getInputStream()));
        }
    }

    private byte[] encode(String... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeAscii(out, "*" + parts.length + "\r\n");
        for (String part : parts) {
            byte[] bytes = String.valueOf(part).getBytes(StandardCharsets.UTF_8);
            writeAscii(out, "$" + bytes.length + "\r\n");
            out.writeBytes(bytes);
            writeAscii(out, "\r\n");
        }
        return out.toByteArray();
    }

    private Object readReply(BufferedInputStream in) throws IOException {
        int prefix = in.read();
        if (prefix == -1) {
            throw new IOException("Reponse Redis vide");
        }
        return switch ((char) prefix) {
            case '+' -> readLine(in);
            case '-' -> throw new IOException(readLine(in));
            case ':' -> Long.parseLong(readLine(in));
            case '$' -> readBulk(in);
            case '*' -> readArray(in);
            default -> throw new IOException("Reponse Redis inconnue: " + (char) prefix);
        };
    }

    private String readBulk(BufferedInputStream in) throws IOException {
        int length = Integer.parseInt(readLine(in));
        if (length < 0) {
            return null;
        }
        byte[] data = in.readNBytes(length);
        in.read();
        in.read();
        return new String(data, StandardCharsets.UTF_8);
    }

    private List<Object> readArray(BufferedInputStream in) throws IOException {
        int length = Integer.parseInt(readLine(in));
        if (length < 0) {
            return Collections.emptyList();
        }
        List<Object> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(readReply(in));
        }
        return values;
    }

    private String readLine(BufferedInputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int previous = -1;
        int current;
        while ((current = in.read()) != -1) {
            if (previous == '\r' && current == '\n') {
                byte[] bytes = out.toByteArray();
                return new String(bytes, 0, Math.max(0, bytes.length - 1), StandardCharsets.UTF_8);
            }
            out.write(current);
            previous = current;
        }
        throw new IOException("Fin de flux Redis inattendue");
    }

    private String serialize(String role, String content) {
        return serializePayload(role, content);
    }

    /**
     * Forme canonique d'un message memoire : {@code {at=..., role=..., content=...}}.
     * Partagee par Redis, le fallback et le compactage de contexte.
     */
    public static String serializePayload(String role, String content) {
        String safeContent = content == null || content.isBlank()
                ? ""
                : content.replace("\r", " ").trim();
        String safeRole = role == null || role.isBlank() ? "system" : role.toLowerCase(Locale.ROOT);
        return "{at=" + Instant.now() + ", role=" + safeRole + ", content=" + safeContent + "}";
    }

    /**
     * Decoupe un payload memoire en {@code [role, content]}. Retourne
     * {@code ["", payload]} si le format n'est pas reconnu.
     */
    public static String[] parsePayload(String payload) {
        if (payload == null || !payload.trim().startsWith("{")) {
            return new String[]{"", payload == null ? "" : payload};
        }
        String s = payload.trim();
        int roleIdx = s.indexOf("role=");
        int contentIdx = roleIdx < 0 ? -1 : s.indexOf("content=", roleIdx);
        if (roleIdx < 0 || contentIdx < 0) {
            return new String[]{"", s};
        }
        String role = s.substring(roleIdx + "role=".length(), contentIdx)
                .replace(",", "").trim();
        String content = s.substring(contentIdx + "content=".length());
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1);
        }
        return new String[]{role, content};
    }

    private String key(String sessionId) {
        return "kazisafex:Gratien:memory:" + sessionId.trim();
    }

    private static void writeAscii(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static String setting(String property, String env, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(env);
        }
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int intSetting(String property, String env, int fallback) {
        try {
            return Integer.parseInt(setting(property, env, String.valueOf(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static boolean booleanSetting(String property, String env, boolean fallback) {
        String value = setting(property, env, String.valueOf(fallback));
        return !"false".equalsIgnoreCase(value) && !"0".equals(value);
    }
}
