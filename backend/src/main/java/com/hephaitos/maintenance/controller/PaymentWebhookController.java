package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final OrderService orderService;

    @PostMapping("/stripe")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        log.info("[Stripe Webhook] Événement reçu");
        try {
            // Dans un cas réel avec clé de signature configurée, on validerait la signature Stripe
            // Ici, pour le sandbox/dev, nous analysons simplement le JSON pour extraire le PaymentIntent
            // Stripe payload contient type: "payment_intent.succeeded" et data.object.id
            // Faisons un parsing simple du JSON reçu pour en extraire l'id
            
            // Simulation simplifiée
            if (payload.contains("\"payment_intent.succeeded\"") || payload.contains("pi_")) {
                // Essayer d'extraire le pattern pi_...
                String piId = extractPattern(payload, "pi_[a-zA-Z0-9_]+");
                if (piId != null) {
                    log.info("[Stripe Webhook] Succès détecté pour PaymentIntent : {}", piId);
                    orderService.confirmPayment(piId, payload);
                    return ResponseEntity.ok("Success");
                }
            }
            
            return ResponseEntity.ok("Ignored event type");
        } catch (Exception e) {
            log.error("[Stripe Webhook] Erreur : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/paypal")
    public ResponseEntity<String> paypalWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[PayPal Webhook] Événement reçu : {}", payload.get("event_type"));
        try {
            // PayPal webhook pour PAYMENT.CAPTURE.COMPLETED ou CHECKOUT.ORDER.APPROVED
            // L'ID de commande se trouve généralement dans resource.id ou resource.parent_payment
            if ("PAYMENT.CAPTURE.COMPLETED".equals(payload.get("event_type")) || payload.containsKey("resource")) {
                Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
                if (resource != null) {
                    String id = (String) resource.get("id");
                    if (id == null) {
                        id = (String) resource.get("billing_agreement_id");
                    }
                    if (id != null) {
                        orderService.confirmPayment(id, payload.toString());
                        return ResponseEntity.ok("Success");
                    }
                }
            }
            return ResponseEntity.ok("Ignored or missing ID");
        } catch (Exception e) {
            log.error("[PayPal Webhook] Erreur : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/orange")
    public ResponseEntity<String> orangeWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[Orange Money Webhook] Événement reçu");
        try {
            // Orange Money renvoie généralement txid ou reference
            String orangeRef = (String) payload.get("orangeRef");
            if (orangeRef == null) {
                orangeRef = (String) payload.get("transaction_id");
            }
            
            if (orangeRef != null && "SUCCESS".equalsIgnoreCase((String) payload.get("status"))) {
                orderService.confirmPayment(orangeRef, payload.toString());
                return ResponseEntity.ok("Success");
            }
            return ResponseEntity.ok("Ignored or non-success status");
        } catch (Exception e) {
            log.error("[Orange Money Webhook] Erreur : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/mtn")
    public ResponseEntity<String> mtnWebhook(@RequestBody Map<String, Object> payload) {
        log.info("[MTN MoMo Webhook] Événement reçu");
        try {
            // MTN Momo renvoie généralement financialTransactionId ou externalId
            String mtnRef = (String) payload.get("mtnRef");
            if (mtnRef == null) {
                mtnRef = (String) payload.get("financialTransactionId");
            }
            
            if (mtnRef != null && ("SUCCESSFUL".equalsIgnoreCase((String) payload.get("status")) || "SUCCESS".equalsIgnoreCase((String) payload.get("status")))) {
                orderService.confirmPayment(mtnRef, payload.toString());
                return ResponseEntity.ok("Success");
            }
            return ResponseEntity.ok("Ignored or non-success status");
        } catch (Exception e) {
            log.error("[MTN MoMo Webhook] Erreur : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private String extractPattern(String text, String regexPattern) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
