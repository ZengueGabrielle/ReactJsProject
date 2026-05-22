package com.hephaitos.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod moyen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus statut = PaymentStatus.EN_ATTENTE;

    @Column(length = 300)
    private String referenceExterne;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentType type;

    @Builder.Default
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String metaData;
}
