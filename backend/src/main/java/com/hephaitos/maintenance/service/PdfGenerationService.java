package com.hephaitos.maintenance.service;

import com.hephaitos.maintenance.entity.Order;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Slf4j
public class PdfGenerationService {

    @Value("${app.invoice-storage-path:./invoices}")
    private String storagePath;

    public String hashSignature(String base64Signature) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base64Signature.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Erreur de hachage de la signature : {}", e.getMessage());
            throw new RuntimeException("Erreur de hachage de la signature", e);
        }
    }

    public File generateInvoicePdf(Order order, String base64Signature) {
        try {
            File dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = "facture_" + order.getReference() + ".pdf";
            File pdfFile = new File(dir, filename);

            PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Style / Palette (Charte graphique)
            DeviceRgb clayBlue = new DeviceRgb(108, 139, 159);  // #6C8B9F
            DeviceRgb violetGrey = new DeviceRgb(74, 78, 105);   // #4A4E69
            DeviceRgb darkAccent = new DeviceRgb(26, 26, 26);    // #1A1A1A

            // Title
            Paragraph title = new Paragraph("FACTURE HEPHAITOS")
                    .setFontSize(24)
                    .setFontColor(clayBlue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            document.add(title);

            // Subtitle / Date
            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            document.add(new Paragraph("Générée le : " + formattedDate)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(violetGrey));

            document.add(new Paragraph("\n"));

            // Client and Workshop Details
            Table detailsTable = new Table(2);
            detailsTable.useAllAvailableWidth();
            detailsTable.addCell(new Paragraph("CLIENT :\n" +
                    order.getUser().getPrenom() + " " + order.getUser().getNom() + "\n" +
                    "Tél : " + order.getUser().getTelephone() + "\n" +
                    "Email : " + order.getUser().getEmail())
                    .setFontColor(darkAccent));

            detailsTable.addCell(new Paragraph("ATELIER :\n" +
                    order.getWorkshop().getNom() + "\n" +
                    "Adresse : " + order.getWorkshop().getAdresse() + "\n" +
                    "Tél : " + order.getWorkshop().getTelephone())
                    .setFontColor(darkAccent));
            document.add(detailsTable);

            document.add(new Paragraph("\n---"));

            // Order details
            document.add(new Paragraph("DÉTAILS DE L'INTERVENTION")
                    .setBold()
                    .setFontColor(violetGrey));
            document.add(new Paragraph("Référence Commande : " + order.getReference()));
            document.add(new Paragraph("Type d'intervention : " + order.getWorkshop().getServiceType()));
            document.add(new Paragraph("Mode : " + order.getMode()));
            if (order.getAdresseDeplacement() != null && !order.getAdresseDeplacement().isEmpty()) {
                document.add(new Paragraph("Adresse de déplacement : " + order.getAdresseDeplacement()));
            }
            document.add(new Paragraph("Date & Heure : " + order.getDate() + " à " + order.getHeure()));
            document.add(new Paragraph("Description : " + order.getDescription()));
            document.add(new Paragraph("Montant Total : " + order.getMontantTotal() + " XAF").setBold().setFontSize(14).setFontColor(clayBlue));

            document.add(new Paragraph("\n"));

            // Add signature if provided
            if (base64Signature != null && !base64Signature.isEmpty()) {
                String cleanBase64 = base64Signature;
                if (base64Signature.contains(",")) {
                    cleanBase64 = base64Signature.split(",")[1];
                }
                byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
                ImageData imageData = ImageDataFactory.create(decodedBytes);
                Image signatureImg = new Image(imageData);
                signatureImg.setMaxWidth(150);
                signatureImg.setMaxHeight(80);

                document.add(new Paragraph("SIGNATURE CLIENT :").setBold().setFontColor(violetGrey));
                document.add(signatureImg);

                // Hash reference print
                String signatureHash = hashSignature(base64Signature);
                document.add(new Paragraph("Empreinte de sécurité : " + signatureHash)
                        .setFontSize(8)
                        .setFontColor(violetGrey));
            }

            document.close();
            log.info("Facture PDF générée avec succès pour la commande {} sous : {}", order.getReference(), pdfFile.getAbsolutePath());
            return pdfFile;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF de la facture : {}", e.getMessage());
            throw new RuntimeException("Erreur de génération PDF", e);
        }
    }
}
