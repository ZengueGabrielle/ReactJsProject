package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.PaymentMethod;
import com.hephaitos.maintenance.entity.PaymentTransaction;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${app.stripe-secret-key}")
    private String stripeSecretKey;

    @Value("${app.paypal-client-id}")
    private String paypalClientId;

    @Value("${app.paypal-secret}")
    private String paypalSecret;

    @Value("${app.orange-money-api-key}")
    private String orangeMoneyKey;

    @Value("${app.mtn-momo-api-key}")
    private String mtnMomoKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Initie un paiement auprès de la plateforme correspondante.
     * Retourne un dictionnaire contenant des métadonnées (ex: clientSecret pour Stripe, URL de redirection pour PayPal/Orange/MTN).
     */
    public Map<String, String> initiatePayment(PaymentTransaction transaction, String returnUrl) {
        Map<String, String> response = new HashMap<>();
        BigDecimal amount = transaction.getMontant();
        String ref = transaction.getOrder().getReference();

        try {
            if (transaction.getMoyen() == PaymentMethod.CARTE) {
                // Stripe PaymentIntent creation
                // Stripe demande un montant en centimes
                long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("eur") // En dev/sandbox Stripe, EUR ou USD. XAF n'est pas supporté par défaut par toutes les cartes Stripe
                        .addPaymentMethodType("card")
                        .putMetadata("order_ref", ref)
                        .putMetadata("tx_id", String.valueOf(transaction.getId()))
                        .build();

                PaymentIntent intent = PaymentIntent.create(params);
                response.put("paymentIntentId", intent.getId());
                response.put("clientSecret", intent.getClientSecret());
                response.put("status", "REQUIRES_PAYMENT_METHOD");
            } else if (transaction.getMoyen() == PaymentMethod.PAYPAL) {
                // PayPal Simulation ou API Rest Call
                String simulatedApprovalUrl = "https://www.sandbox.paypal.com/checkoutnow?token=EC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                response.put("approvalUrl", simulatedApprovalUrl);
                response.put("paypalOrderId", "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                response.put("status", "PENDING");
            } else if (transaction.getMoyen() == PaymentMethod.ORANGE_MONEY) {
                // Orange Money WebPay Simulation
                String simulatedOrangeUrl = "https://sandbox.orange-money.cm/pay?tx=" + UUID.randomUUID().toString().substring(0, 10);
                response.put("paymentUrl", simulatedOrangeUrl);
                response.put("orangeRef", "OM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                response.put("status", "PENDING");
            } else if (transaction.getMoyen() == PaymentMethod.MTN_MONEY) {
                // MTN Mobile Money Collection Simulation
                String simulatedMtnUrl = "https://sandbox.mtn-momo.cm/collection?id=" + UUID.randomUUID().toString().substring(0, 10);
                response.put("paymentUrl", simulatedMtnUrl);
                response.put("mtnRef", "MTN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                response.put("status", "PENDING");
            } else if (transaction.getMoyen() == PaymentMethod.PRESENTIEL_ACOMPTE) {
                // Paiement d'un acompte en présentiel (50% en ligne, 50% sur place)
                // Le 50% en ligne est traité via carte par défaut dans Stripe
                long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("eur")
                        .addPaymentMethodType("card")
                        .putMetadata("order_ref", ref)
                        .putMetadata("tx_id", String.valueOf(transaction.getId()))
                        .build();

                PaymentIntent intent = PaymentIntent.create(params);
                response.put("paymentIntentId", intent.getId());
                response.put("clientSecret", intent.getClientSecret());
                response.put("status", "REQUIRES_PAYMENT_METHOD");
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'initiation du paiement pour la commande {} : {}", ref, e.getMessage());
            throw new RuntimeException("Erreur d'initiation du paiement : " + e.getMessage(), e);
        }

        return response;
    }

    /**
     * Rembourse partiellement ou totalement une transaction.
     */
    public boolean refundPayment(PaymentTransaction transaction, BigDecimal refundAmount) {
        log.info("Traitement du remboursement de {} XAF pour la commande {}", refundAmount, transaction.getOrder().getReference());
        if (transaction.getReferenceExterne() == null) {
            log.warn("Aucune référence externe de transaction trouvée. Remboursement simulé comme réussi.");
            return true;
        }

        try {
            if (transaction.getMoyen() == PaymentMethod.CARTE || transaction.getMoyen() == PaymentMethod.PRESENTIEL_ACOMPTE) {
                // Stripe Refund
                long refundInCents = refundAmount.multiply(BigDecimal.valueOf(100)).longValue();
                RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(transaction.getReferenceExterne())
                        .setAmount(refundInCents)
                        .build();
                Refund.create(params);
                log.info("Remboursement Stripe effectué avec succès");
                return true;
            } else {
                // Pour PayPal, Orange Money, MTN MoMo -> Simulation de remboursement approuvé par sandbox
                log.info("Remboursement {} simulé avec succès pour la réf : {}", transaction.getMoyen(), transaction.getReferenceExterne());
                return true;
            }
        } catch (Exception e) {
            log.error("Échec du remboursement pour la commande {} : {}", transaction.getOrder().getReference(), e.getMessage());
            return false;
        }
    }
}
