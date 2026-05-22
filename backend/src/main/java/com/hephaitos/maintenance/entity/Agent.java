package com.hephaitos.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(unique = true, length = 100)
    private String carteProId;

    @Builder.Default
    private boolean valide = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(length = 100)
    private String specialite;

    @Column(length = 20)
    private String telephone;
}
