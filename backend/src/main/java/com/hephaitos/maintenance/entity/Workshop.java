package com.hephaitos.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workshops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false, length = 100)
    private String ville;

    private Double latitude;
    private Double longitude;

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String serviceType;

    @Builder.Default
    private boolean deplacementPossible = false;

    @Column(columnDefinition = "TEXT")
    private String horaires;

    @Column(columnDefinition = "TEXT")
    private String tarifJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Builder.Default
    private boolean actif = true;

    @Builder.Default
    private Double avgRating = 0.0;

    @Builder.Default
    private int totalReviews = 0;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Team> teams;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
