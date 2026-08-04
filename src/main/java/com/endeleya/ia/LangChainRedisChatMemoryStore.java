package com.endeleya.ia;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LangChainRedisChatMemoryStore implements ChatMemoryStore {

    private static final int MAX_MESSAGES = 10;
    private final RedisMemoryStore redisMemoryStore;

    public LangChainRedisChatMemoryStore(RedisMemoryStore redisMemoryStore) {
        this.redisMemoryStore = redisMemoryStore;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<String> payloads = redisMemoryStore.recentRaw(redisKey(memoryId), MAX_MESSAGES);
        List<ChatMessage> messages = new ArrayList<>();
        for (String payload : payloads) {
            ChatMessage message = deserialize(payload);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        List<String> payloads = new ArrayList<>();
        if (messages != null) {
            for (ChatMessage message : messages) {
                payloads.add(serialize(message));
            }
        }
        redisMemoryStore.replaceRaw(redisKey(memoryId), payloads);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisMemoryStore.clear(redisKey(memoryId));
    }

    private String serialize(ChatMessage message) {
        if (message instanceof SystemMessage system) {
            return "SYSTEM|" + encode(system.text());
        }
        if (message instanceof UserMessage user) {
            return "USER|" + encode(user.hasSingleText() ? user.singleText() : user.toString());
        }
        if (message instanceof AiMessage ai) {
            return "AI|" + encode(ai.text() == null ? "" : ai.text());
        }
        if (message instanceof ToolExecutionResultMessage tool) {
            return "TOOL|" + encode(tool.id()) + "|" + encode(tool.toolName()) + "|" + encode(tool.text());
        }
        return "USER|" + encode(message == null ? "" : message.toString());
    }

    private ChatMessage deserialize(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        String[] parts = payload.split("\\|", -1);
        if (parts.length < 2) {
            return null;
        }
        return switch (parts[0]) {
            case "SYSTEM" -> SystemMessage.from(decode(parts[1]));
            case "USER" -> UserMessage.from(decode(parts[1]));
            case "AI" -> AiMessage.from(decode(parts[1]));
            case "TOOL" -> parts.length >= 4
                    ? ToolExecutionResultMessage.from(decode(parts[1]), decode(parts[2]), decode(parts[3]))
                    : null;
            default -> null;
        };
    }

    private String redisKey(Object memoryId) {
        return "langchain:" + String.valueOf(memoryId == null ? "default" : memoryId);
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value == null ? "" : value), StandardCharsets.UTF_8);
    }
}
