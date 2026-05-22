package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.entity.User;
import com.hephaitos.maintenance.service.ChatbotService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal User user,
            @RequestBody MessageRequest request
    ) {
        if ((request.getMessage() == null || request.getMessage().trim().isEmpty()) && request.getImageBase64() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Le message ne peut pas être vide."));
        }

        String reply = chatbotService.generateResponse(user, request.getMessage(), request.getImageBase64(), request.getImageMediaType());
        return ResponseEntity.ok(java.util.Map.of("response", reply));
    }

    @Data
    public static class MessageRequest {
        private String message;
        private String imageBase64;
        private String imageMediaType;
    }
}
