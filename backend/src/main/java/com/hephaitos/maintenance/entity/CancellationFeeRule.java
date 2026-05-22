package com.hephaitos.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cancellation_fee_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int seuilHeuresAvant; // ex: 24, 48

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal pourcentage; // ex: 10.00, 30.00, 50.00
}
