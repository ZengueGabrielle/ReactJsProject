package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.entity.Order;
import com.hephaitos.maintenance.entity.User;
import com.hephaitos.maintenance.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody OrderService.OrderRequest request
    ) {
        try {
            return ResponseEntity.ok(orderService.initiateOrder(user, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody OrderService.UpdateOrderRequest request
    ) {
        try {
            Order updated = orderService.updateOrder(user, id, request);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        try {
            Order cancelled = orderService.cancelOrder(user, id);
            return ResponseEntity.ok(cancelled);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getUserOrders(user));
    }
}
