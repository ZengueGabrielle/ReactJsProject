package com.hephaitos.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 500)
    private String pdfUrl;

    @Column(nullable = false, length = 256)
    private String signatureHash;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateEmission = LocalDateTime.now();
}
