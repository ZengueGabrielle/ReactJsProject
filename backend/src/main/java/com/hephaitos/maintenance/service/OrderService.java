package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.*;
import com.hephaitos.maintenance.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WorkshopRepository workshopRepository;
    private final TeamRepository teamRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CancellationFeeRuleRepository feeRuleRepository;
    private final InvoiceRepository invoiceRepository;
    
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final PdfGenerationService pdfGenerationService;

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByDateCreationDesc(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
    }

    /**
     * Vérifie la disponibilité d'une équipe pour une date et heure données (+/- 2 heures).
     */
    public boolean isTeamAvailable(Team team, LocalDate date, LocalTime time) {
        if (!team.isActif()) {
            return false;
        }
        
        // Vérifier si tous les agents de l'équipe sont validés
        if (team.getAgents() == null || team.getAgents().isEmpty()) {
            return false;
        }
        for (Agent agent : team.getAgents()) {
            if (!agent.isValide()) {
                return false; // Règle métier : Un agent n'est utilisable que si validé = true
            }
        }

        // Récupérer les commandes assignées à cette équipe pour ce jour
        List<Order> dayOrders = orderRepository.findTeamOrdersForDate(
                team, date, List.of(OrderStatus.CONFIRMEE, OrderStatus.AGENT_EN_ROUTE, OrderStatus.TRAVAUX_EN_COURS)
        );

        LocalDateTime requestDateTime = LocalDateTime.of(date, time);

        for (Order existingOrder : dayOrders) {
            LocalDateTime existingDateTime = LocalDateTime.of(existingOrder.getDate(), existingOrder.getHeure());
            Duration duration = Duration.between(requestDateTime, existingDateTime).abs();
            // Si l'intervalle est inférieur à 2 heures, l'équipe est considérée occupée
            if (duration.toHours() < 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * Initie une commande de maintenance.
     */
    @Transactional
    public OrderInitResponse initiateOrder(User user, OrderRequest request) {
        Workshop workshop = workshopRepository.findById(request.getWorkshopId())
                .orElseThrow(() -> new IllegalArgumentException("Atelier non trouvé"));

        if (!workshop.isActif() || (workshop.getCompany() != null && !workshop.getCompany().isValide())) {
            throw new IllegalArgumentException("L'atelier ou l'entreprise associée n'est pas active/validée.");
        }

        Team team = null;
        if (request.getMode() == OrderMode.DEPLACEMENT) {
            if (request.getTeamId() == null) {
                throw new IllegalArgumentException("Une équipe doit être spécifiée pour un déplacement.");
            }
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Équipe non trouvée"));

            if (!isTeamAvailable(team, request.getDate(), request.getHeure())) {
                throw new IllegalArgumentException("L'équipe sélectionnée n'est pas disponible pour ce créneau.");
            }
        }

        // Créer la commande
        Order order = Order.builder()
                .user(user)
                .workshop(workshop)
                .team(team)
                .mode(request.getMode())
                .adresseDeplacement(request.getAdresseDeplacement())
                .date(request.getDate())
                .heure(request.getHeure())
                .urgence(request.isUrgence())
                .description(request.getDescription())
                .montantTotal(request.getMontantTotal())
                .statut(OrderStatus.EN_ATTENTE_PAIEMENT)
                .build();

        order = orderRepository.save(order);

        // Déterminer le montant du paiement en ligne
        BigDecimal onlineAmount = order.getMontantTotal();
        PaymentType paymentType = PaymentType.TOTAL;

        if (request.getMoyenPaiement() == PaymentMethod.PRESENTIEL_ACOMPTE) {
            // Acompte de 50%
            onlineAmount = order.getMontantTotal().multiply(BigDecimal.valueOf(0.5));
            paymentType = PaymentType.ACOMPTE;
        }

        // Créer la transaction de paiement
        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .montant(onlineAmount)
                .moyen(request.getMoyenPaiement())
                .statut(PaymentStatus.EN_ATTENTE)
                .type(paymentType)
                .build();

        tx = paymentTransactionRepository.save(tx);

        // Lancer la passerelle de paiement
        Map<String, String> paymentMetadata = paymentService.initiatePayment(tx, "http://localhost:5173/payment-success");

        // Si c'est PayPal, Orange Money ou MTN, on sauvegarde l'ID externe temporaire
        if (paymentMetadata.containsKey("paypalOrderId")) {
            tx.setReferenceExterne(paymentMetadata.get("paypalOrderId"));
        } else if (paymentMetadata.containsKey("orangeRef")) {
            tx.setReferenceExterne(paymentMetadata.get("orangeRef"));
        } else if (paymentMetadata.containsKey("mtnRef")) {
            tx.setReferenceExterne(paymentMetadata.get("mtnRef"));
        } else if (paymentMetadata.containsKey("paymentIntentId")) {
            tx.setReferenceExterne(paymentMetadata.get("paymentIntentId"));
        }
        paymentTransactionRepository.save(tx);

        // Response DTO
        OrderInitResponse response = new OrderInitResponse();
        response.setOrderId(order.getId());
        response.setReference(order.getReference());
        response.setMontantEnLigne(onlineAmount);
        response.setMetadata(paymentMetadata);

        return response;
    }

    /**
     * Confirme le paiement et active la commande.
     */
    @Transactional
    public void confirmPayment(String externalRef, String metadataJson) {
        PaymentTransaction tx = paymentTransactionRepository.findByReferenceExterne(externalRef)
                .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable pour la référence externe : " + externalRef));

        if (tx.getStatut() == PaymentStatus.SUCCES) {
            log.info("La transaction {} est déjà marquée comme SUCCES", externalRef);
            return;
        }

        tx.setStatut(PaymentStatus.SUCCES);
        tx.setMetaData(metadataJson);
        paymentTransactionRepository.save(tx);

        Order order = tx.getOrder();
        order.setStatut(OrderStatus.CONFIRMEE);
        orderRepository.save(order);

        // Envoyer email et SMS
        notificationService.notifyOrderConfirmation(order);
    }

    /**
     * Modifie une commande (Une seule fois maximum).
     */
    @Transactional
    public Order updateOrder(User user, Long orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        // Vérifier l'autorisation
        if (!order.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette commande.");
        }

        // Règle métier : modificationCount < 1
        if (order.getModificationCount() >= 1) {
            throw new IllegalArgumentException("Cette commande a déjà été modifiée. Une seule modification est autorisée.");
        }

        // Vérification de disponibilité de la nouvelle équipe si date/heure/équipe changent
        boolean dateTimeChanged = !order.getDate().equals(request.getDate()) || !order.getHeure().equals(request.getHeure());
        boolean teamChanged = request.getTeamId() != null && (order.getTeam() == null || !order.getTeam().getId().equals(request.getTeamId()));

        if (dateTimeChanged || teamChanged) {
            Team team = order.getTeam();
            if (request.getTeamId() != null) {
                team = teamRepository.findById(request.getTeamId())
                        .orElseThrow(() -> new IllegalArgumentException("Nouvelle équipe non trouvée"));
            }

            if (team != null) {
                if (!isTeamAvailable(team, request.getDate(), request.getHeure())) {
                    throw new IllegalArgumentException("L'équipe n'est pas disponible pour cette nouvelle date/heure.");
                }
                order.setTeam(team);
            }
        }

        order.setDate(request.getDate());
        order.setHeure(request.getHeure());
        order.setDescription(request.getDescription());
        order.setMode(request.getMode());
        order.setAdresseDeplacement(request.getAdresseDeplacement());
        order.setModificationCount(order.getModificationCount() + 1);

        order = orderRepository.save(order);

        // Notifier le client
        notificationService.notifyOrderModification(order);

        return order;
    }

    /**
     * Annule une commande et calcule les frais d'annulation selon les règles paramétrées.
     */
    @Transactional
    public Order cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        // Vérifier l'autorisation
        if (!order.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new SecurityException("Vous n'êtes pas autorisé à annuler cette commande.");
        }

        if (order.getStatut() == OrderStatus.ANNULEE) {
            throw new IllegalArgumentException("La commande est déjà annulée.");
        }

        // Calcul des frais
        LocalDateTime orderDateTime = LocalDateTime.of(order.getDate(), order.getHeure());
        LocalDateTime now = LocalDateTime.now();

        long hoursBefore = Duration.between(now, orderDateTime).toHours();
        BigDecimal feePercentage = BigDecimal.ZERO;

        if (hoursBefore > 0) {
            // Chercher la règle correspondante
            List<CancellationFeeRule> rules = feeRuleRepository.findAllByOrderBySeuilHeuresAvantDesc();
            // Exemple de calcul :
            // Si hoursBefore = 36.
            // Règles existantes : >48h (10%), >24h (30%), <24h (50%)
            // On cherche le seuil le plus proche inférieur ou égal à hoursBefore.
            for (CancellationFeeRule rule : rules) {
                if (hoursBefore <= rule.getSeuilHeuresAvant()) {
                    feePercentage = rule.getPourcentage();
                }
            }
            // Fallback par défaut si aucune règle n'est configurée :
            if (feePercentage.compareTo(BigDecimal.ZERO) == 0) {
                if (hoursBefore >= 48) {
                    feePercentage = BigDecimal.valueOf(10.00); // 10%
                } else if (hoursBefore >= 24) {
                    feePercentage = BigDecimal.valueOf(30.00); // 30%
                } else {
                    feePercentage = BigDecimal.valueOf(50.00); // 50%
                }
            }
        } else {
            // Commande déjà passée ou en cours
            feePercentage = BigDecimal.valueOf(100.00); // 100% frais
        }

        PaymentTransaction tx = order.getPaymentTransaction();
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal feeAmount = BigDecimal.ZERO;

        if (tx != null && tx.getStatut() == PaymentStatus.SUCCES) {
            BigDecimal totalPaid = tx.getMontant();
            feeAmount = totalPaid.multiply(feePercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            refundAmount = totalPaid.subtract(feeAmount);

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                boolean refundSuccess = paymentService.refundPayment(tx, refundAmount);
                if (refundSuccess) {
                    tx.setStatut(PaymentStatus.PARTIELLEMENT_REMBOURSE);
                } else {
                    log.error("Échec du remboursement bancaire réel pour la commande {}", order.getReference());
                }
            } else {
                tx.setStatut(PaymentStatus.REMBOURSE); // ou reste conservé en frais total
            }
            paymentTransactionRepository.save(tx);
        }

        order.setStatut(OrderStatus.ANNULEE);
        order = orderRepository.save(order);

        // Notification
        notificationService.notifyOrderCancellation(order, feeAmount);

        return order;
    }

    /**
     * Signe la facture et génère le fichier PDF.
     */
    @Transactional
    public Invoice signInvoice(Long orderId, String signatureBase64) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        // Règle métier : la commande doit être terminée (ou facturée)
        if (order.getStatut() != OrderStatus.TERMINEE && order.getStatut() != OrderStatus.FACTUREE) {
            throw new IllegalArgumentException("Les travaux doivent être terminés avant de signer la facture.");
        }

        String signatureHash = pdfGenerationService.hashSignature(signatureBase64);
        File pdfFile = pdfGenerationService.generateInvoicePdf(order, signatureBase64);

        // URL publique virtuelle pour accéder à la facture
        String pdfUrl = "/api/invoices/download/" + order.getId();

        Invoice invoice = invoiceRepository.findByOrder(order).orElse(null);
        if (invoice == null) {
            invoice = Invoice.builder()
                    .order(order)
                    .pdfUrl(pdfUrl)
                    .signatureHash(signatureHash)
                    .dateEmission(LocalDateTime.now())
                    .build();
        } else {
            invoice.setPdfUrl(pdfUrl);
            invoice.setSignatureHash(signatureHash);
            invoice.setDateEmission(LocalDateTime.now());
        }

        invoice = invoiceRepository.save(invoice);

        order.setStatut(OrderStatus.FACTUREE);
        orderRepository.save(order);

        // Envoyer la facture par e-mail
        notificationService.sendEmail(
                order.getUser().getEmail(),
                "Votre facture signée - Commande " + order.getReference(),
                "Bonjour " + order.getUser().getPrenom() + ",\n\n" +
                "Vous trouverez votre facture de maintenance signée électroniquement en pièce jointe ou via ce lien de téléchargement :\n" +
                "http://localhost:8080" + pdfUrl + "\n\n" +
                "Empreinte cryptographique de la signature : " + signatureHash + "\n\n" +
                "Merci d'avoir utilisé Hephaitos !"
        );

        return invoice;
    }

    @Data
    public static class OrderRequest {
        private Long workshopId;
        private Long teamId; // optionnel (uniquement si DEPLACEMENT)
        private OrderMode mode;
        private String adresseDeplacement;
        private LocalDate date;
        private LocalTime heure;
        private boolean urgence;
        private String description;
        private BigDecimal montantTotal;
        private PaymentMethod moyenPaiement;
    }

    @Data
    public static class UpdateOrderRequest {
        private LocalDate date;
        private LocalTime heure;
        private String description;
        private OrderMode mode;
        private String adresseDeplacement;
        private Long teamId;
    }

    @Data
    public static class OrderInitResponse {
        private Long orderId;
        private String reference;
        private BigDecimal montantEnLigne;
        private Map<String, String> metadata;
    }
}
