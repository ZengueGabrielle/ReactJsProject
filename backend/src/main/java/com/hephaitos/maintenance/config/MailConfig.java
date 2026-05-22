package com.hephaitos.maintenance.config;

import com.hephaitos.maintenance.util.EncryptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private static final String ENCRYPTED_EMAIL = "TdUiZfytqcygrGUG0UfFjYGTmAg27sGRDDs2nLT23j0=";
    private static final String ENCRYPTED_PASSWORD = "sslJv7NhOv4M3OSefoBLJm4s8AkBFMet4odsAuKzbYg=";

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // Décryptage à la volée des informations
        String decryptedEmail = EncryptionUtils.decrypt(ENCRYPTED_EMAIL);
        String decryptedPassword = EncryptionUtils.decrypt(ENCRYPTED_PASSWORD);

        mailSender.setUsername(decryptedEmail);
        mailSender.setPassword(decryptedPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }
}
