package com.hephaitos.maintenance.controller;

import com.hephaitos.maintenance.entity.Order;
import com.hephaitos.maintenance.entity.OrderStatus;
import com.hephaitos.maintenance.entity.User;
import com.hephaitos.maintenance.entity.Workshop;
import com.hephaitos.maintenance.repository.OrderRepository;
import com.hephaitos.maintenance.repository.WorkshopRepository;
import com.hephaitos.maintenance.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final OrderRepository orderRepository;
    private final WorkshopRepository workshopRepository;
    private final NotificationService notificationService;

    @GetMapping("/workshops")
    public ResponseEntity<List<Workshop>> getManagerWorkshops(@AuthenticationPrincipal User user) {
        if (user.getCompany() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        // Charger les ateliers de l'entreprise
        return ResponseEntity.ok(user.getCompany().getWorkshops());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getManagerOrders(@AuthenticationPrincipal User user) {
        if (user.getCompany() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(orderRepository.findByCompanyId(user.getCompany().getId()));
    }

    @PutMapping("/orders/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        // Vérifier que le manager gère bien l'atelier de cette commande
        if (user.getCompany() == null || !order.getWorkshop().getCompany().getId().equals(user.getCompany().getId())) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Vous n'êtes pas autorisé à gérer cette commande."));
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatut(newStatus);
            orderRepository.save(order);

            // Si TERMINEE, déclenche la notification de fin de travaux pour facturation
            if (newStatus == OrderStatus.TERMINEE) {
                notificationService.notifyOrderCompletion(order);
            }

            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Statut invalide : " + request.getStatus()));
        }
    }

    @Data
    public static class StatusUpdateRequest {
        private String status;
    }
}
