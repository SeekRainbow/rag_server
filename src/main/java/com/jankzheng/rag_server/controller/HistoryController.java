package com.jankzheng.rag_server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Value("${app.history.dir}")
    private String historyDir;

    @Autowired
    private ChatMemory chatMemory;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private File getHistoryFile(String id) {
        return new File(historyDir, id + ".json");
    }

    @PostConstruct
    public void init() {
        File dir = new File(historyDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> result = new ArrayList<>();
        File dir = new File(historyDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return result;

        for (File file : files) {
            try {
                ObjectNode node = (ObjectNode) objectMapper.readTree(file);
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("id", node.get("id").asText());
                meta.put("title", node.get("title").asText());
                meta.put("createdAt", node.get("createdAt").asText());
                meta.put("updatedAt", node.get("updatedAt").asText());
                meta.put("messageCount", node.get("messages").size());
                result.add(meta);
            } catch (IOException e) {
                // skip corrupted files
            }
        }
        result.sort((a, b) -> b.get("updatedAt").toString().compareTo(a.get("updatedAt").toString()));
        return result;
    }

    @GetMapping("/get/{id}")
    public Map<String, Object> get(@PathVariable String id) throws IOException {
        File file = getHistoryFile(id);
        if (!file.exists()) {
            throw new RuntimeException("会话不存在");
        }
        return objectMapper.readValue(file, LinkedHashMap.class);
    }

    @PostMapping("/save")
    public String save(@RequestParam String conversationId, @RequestParam(required = false) String title) throws IOException {
        List<Message> messages = chatMemory.get(conversationId);
        if (messages.isEmpty()) {
            return "empty";
        }

        File file = getHistoryFile(conversationId);
        Map<String, Object> data;
        if (file.exists()) {
            data = objectMapper.readValue(file, LinkedHashMap.class);
        } else {
            data = new LinkedHashMap<>();
            data.put("id", conversationId);
            data.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (title == null || title.isBlank()) {
                String firstUserMsg = messages.stream()
                        .filter(m -> m instanceof UserMessage)
                        .map(Message::getText)
                        .filter(t -> !t.isBlank())
                        .findFirst()
                        .orElse(conversationId);
                title = firstUserMsg.length() > 30 ? firstUserMsg.substring(0, 30) + "..." : firstUserMsg;
            }
            data.put("title", title);
        }

        data.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        List<Map<String, String>> messageList = messages.stream()
                .map(m -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    if (m instanceof UserMessage) {
                        entry.put("role", "user");
                    } else if (m instanceof AssistantMessage) {
                        entry.put("role", "assistant");
                    } else {
                        entry.put("role", "system");
                    }
                    entry.put("content", m.getText());
                    return entry;
                })
                .collect(Collectors.toList());
        data.put("messages", messageList);

        objectMapper.writeValue(file, data);
        return "ok";
    }

    @PostMapping("/load/{id}")
    public Map<String, Object> load(@PathVariable String id, @RequestParam String conversationId) throws IOException {
        File file = getHistoryFile(id);
        if (!file.exists()) {
            throw new RuntimeException("会话不存在");
        }

        Map<String, Object> data = objectMapper.readValue(file, LinkedHashMap.class);
        List<Map<String, String>> rawMessages = (List<Map<String, String>>) data.get("messages");

        chatMemory.clear(conversationId);
        List<Message> messages = rawMessages.stream()
                .map(m -> {
                    String role = m.get("role");
                    String content = m.get("content");
                    if ("user".equals(role)) {
                        return (Message) new UserMessage(content);
                    } else if ("assistant".equals(role)) {
                        return (Message) new AssistantMessage(content);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        chatMemory.add(conversationId, messages);

        data.put("conversationId", conversationId);
        return data;
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        File file = getHistoryFile(id);
        if (file.exists()) {
            file.delete();
        }
        return "ok";
    }
}
