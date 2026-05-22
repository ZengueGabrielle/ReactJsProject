package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.Order;
import com.hephaitos.maintenance.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private static final String SENDER_EMAIL = EncryptionUtils.decrypt("TdUiZfytqcygrGUG0UfFjYGTmAg27sGRDDs2nLT23j0=");

    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(SENDER_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("E-mail envoyé avec succès à : {}", to);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'e-mail à {} : {}", to, e.getMessage());
        }
    }

    public void sendSms(String telephone, String message) {
        // Simulation de l'API Twilio (logger)
        log.info("[SMS Twilio] Envoi du SMS au numéro : {} -> {}", telephone, message);
    }

    public void notifyOrderConfirmation(Order order) {
        String clientEmail = order.getUser().getEmail();
        String clientPhone = order.getUser().getTelephone();

        String subject = "Confirmation de votre commande : " + order.getReference();
        String body = String.format(
                "Bonjour %s %s,\n\n" +
                "Votre commande de maintenance chez l'atelier %s a été confirmée avec succès.\n" +
                "Référence : %s\n" +
                "Date : %s à %s\n" +
                "Mode : %s\n" +
                "Description : %s\n" +
                "Montant Total : %s XAF\n\n" +
                "Merci de votre confiance,\nL'équipe Hephaitos.",
                order.getUser().getPrenom(), order.getUser().getNom(),
                order.getWorkshop().getNom(),
                order.getReference(),
                order.getDate(), order.getHeure(),
                order.getMode(),
                order.getDescription(),
                order.getMontantTotal()
        );

        sendEmail(clientEmail, subject, body);
        sendSms(clientPhone, "Hephaitos : Votre commande " + order.getReference() + " est CONFIRMEE pour le " + order.getDate() + ".");
    }

    public void notifyOrderModification(Order order) {
        String clientEmail = order.getUser().getEmail();
        String clientPhone = order.getUser().getTelephone();

        String subject = "Modification de votre commande : " + order.getReference();
        String body = String.format(
                "Bonjour %s %s,\n\n" +
                "Votre commande %s a été modifiée avec succès.\n" +
                "Nouveaux détails :\n" +
                "Date : %s à %s\n" +
                "Mode : %s\n" +
                "Adresse : %s\n" +
                "Description : %s\n\n" +
                "Cordialement,\nL'équipe Hephaitos.",
                order.getUser().getPrenom(), order.getUser().getNom(),
                order.getReference(),
                order.getDate(), order.getHeure(),
                order.getMode(),
                order.getAdresseDeplacement() != null ? order.getAdresseDeplacement() : "N/A",
                order.getDescription()
        );

        sendEmail(clientEmail, subject, body);
        sendSms(clientPhone, "Hephaitos : Votre commande " + order.getReference() + " a été MODIFIEE. Date: " + order.getDate() + " à " + order.getHeure() + ".");
    }

    public void notifyOrderCancellation(Order order, java.math.BigDecimal fees) {
        String clientEmail = order.getUser().getEmail();
        String clientPhone = order.getUser().getTelephone();

        String subject = "Annulation de votre commande : " + order.getReference();
        String body = String.format(
                "Bonjour %s %s,\n\n" +
                "Votre commande %s a été annulée.\n" +
                "Frais d'annulation appliqués : %s XAF.\n" +
                "Le remboursement du solde (si applicable) a été initié via le moyen de paiement d'origine.\n\n" +
                "Cordialement,\nL'équipe Hephaitos.",
                order.getUser().getPrenom(), order.getUser().getNom(),
                order.getReference(),
                fees
        );

        sendEmail(clientEmail, subject, body);
        sendSms(clientPhone, "Hephaitos : Votre commande " + order.getReference() + " a été ANNULEE. Frais d'annulation : " + fees + " XAF.");
    }

    public void notifyOrderCompletion(Order order) {
        String clientEmail = order.getUser().getEmail();
        String clientPhone = order.getUser().getTelephone();

        String subject = "Fin des travaux pour votre commande : " + order.getReference();
        String body = String.format(
                "Bonjour %s %s,\n\n" +
                "Les travaux concernant votre commande %s chez %s sont terminés.\n" +
                "Vous pouvez dès à présent signer la facture sur votre espace utilisateur.\n\n" +
                "Cordialement,\nL'équipe Hephaitos.",
                order.getUser().getPrenom(), order.getUser().getNom(),
                order.getReference(),
                order.getWorkshop().getNom()
        );

        sendEmail(clientEmail, subject, body);
        sendSms(clientPhone, "Hephaitos : Les travaux de la commande " + order.getReference() + " sont TERMINEES. Veuillez signer la facture.");
    }
}
