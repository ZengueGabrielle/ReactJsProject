package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.ChatHistory;
import com.hephaitos.maintenance.entity.User;
import com.hephaitos.maintenance.repository.ChatHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Value("${app.gemini-api-key}")
    private String geminiApiKey;

    @Value("${app.gemini-api-url}")
    private String geminiApiUrl;

    public String generateResponse(User user, String userMessage, String imageBase64, String imageMediaType) {
        log.info("Appel de l'assistant Gemini (avec vision: {}) pour l'utilisateur {} : {}", imageBase64 != null, user.getEmail(), userMessage);

        try {
            WebClient webClient = WebClient.builder()
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            GeminiRequest requestBody = new GeminiRequest();
            
            // System instruction
            GeminiContent systemContent = new GeminiContent();
            GeminiPart systemPart = new GeminiPart();
            systemPart.setText("Tu es un chatbot spécialisé dans la maintenance en tout genre (plomberie, bricolage, maintenance automobile, menuiserie, électronique et IT). Tu dois être un expert en tout ça et répondre à toutes les questions, donner des conseils sur demande. Tu scannes les images qu'on t'envoie, qui ne doivent concerner que la maintenance (par exemple, un évier qui fuit ou un pied de table endommagé). Si l'image ne concerne pas la maintenance (par exemple, un gâteau), refuse poliment d'y répondre.");
            systemContent.setParts(List.of(systemPart));
            requestBody.setSystemInstruction(systemContent);

            // User message content
            List<GeminiPart> parts = new ArrayList<>();
            
            if (userMessage != null && !userMessage.isEmpty()) {
                GeminiPart textPart = new GeminiPart();
                textPart.setText(userMessage);
                parts.add(textPart);
            }
            
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                GeminiPart imagePart = new GeminiPart();
                GeminiInlineData inlineData = new GeminiInlineData();
                inlineData.setMimeType(imageMediaType != null ? imageMediaType : "image/jpeg");
                inlineData.setData(imageBase64);
                imagePart.setInlineData(inlineData);
                parts.add(imagePart);
            }

            if (parts.isEmpty()) {
                GeminiPart defaultPart = new GeminiPart();
                defaultPart.setText("Bonjour");
                parts.add(defaultPart);
            }

            GeminiContent userContent = new GeminiContent();
            userContent.setRole("user");
            userContent.setParts(parts);
            
            requestBody.setContents(List.of(userContent));

            GeminiResponse response = webClient.post()
                    .uri(geminiApiUrl + "?key=" + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block(); // Appel synchrone

            String botReply = "Désolé, je ne parviens pas à traiter votre demande pour le moment.";
            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                botReply = response.getCandidates().get(0).getContent().getParts().get(0).getText();
            }

            // Sauvegarde de l'historique
            ChatHistory history = ChatHistory.builder()
                    .user(user)
                    .message(userMessage)
                    .reponse(botReply)
                    .horodatage(LocalDateTime.now())
                    .build();
            chatHistoryRepository.save(history);

            return botReply;
        } catch (Exception e) {
            log.error("Erreur de communication avec l'API Gemini : {}", e.getMessage());
            throw new RuntimeException("Erreur de communication avec l'API Gemini: " + e.getMessage());
        }
    }

    // Structures DTO internes pour Gemini API
    @Data
    public static class GeminiRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("system_instruction")
        private GeminiContent systemInstruction;
        private List<GeminiContent> contents;
    }

    @Data
    public static class GeminiContent {
        private String role;
        private List<GeminiPart> parts;
    }

    @Data
    public static class GeminiPart {
        private String text;
        @com.fasterxml.jackson.annotation.JsonProperty("inlineData")
        private GeminiInlineData inlineData;
    }

    @Data
    public static class GeminiInlineData {
        @com.fasterxml.jackson.annotation.JsonProperty("mimeType")
        private String mimeType;
        private String data;
    }

    @Data
    public static class GeminiResponse {
        private List<GeminiCandidate> candidates;
    }

    @Data
    public static class GeminiCandidate {
        private GeminiContent content;
    }
}
