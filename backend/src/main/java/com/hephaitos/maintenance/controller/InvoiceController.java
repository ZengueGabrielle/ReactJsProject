package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.entity.Invoice;
import com.hephaitos.maintenance.entity.Order;
import com.hephaitos.maintenance.entity.User;
import com.hephaitos.maintenance.repository.InvoiceRepository;
import com.hephaitos.maintenance.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final OrderService orderService;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.invoice-storage-path:./invoices}")
    private String storagePath;

    @PostMapping("/{orderId}/sign")
    public ResponseEntity<?> signInvoice(
            @PathVariable Long orderId,
            @RequestBody SignatureRequest request
    ) {
        if (request.getSignature() == null || request.getSignature().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "La signature est requise."));
        }

        try {
            Invoice invoice = orderService.signInvoice(orderId, request.getSignature());
            return ResponseEntity.ok(invoice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{orderId}")
    public ResponseEntity<Resource> downloadInvoice(
            @PathVariable Long orderId
    ) {
        Order order = orderService.getOrderById(orderId);
        String filename = "facture_" + order.getReference() + ".pdf";
        File file = new File(storagePath, filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Data
    public static class SignatureRequest {
        private String signature; // Base64 image
    }
}
