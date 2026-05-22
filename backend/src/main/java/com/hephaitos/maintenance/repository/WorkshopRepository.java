package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Workshop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    @Query("SELECT w FROM Workshop w WHERE w.actif = true " +
            "AND (:serviceType IS NULL OR w.serviceType = :serviceType) " +
            "AND (:ville IS NULL OR w.ville LIKE %:ville%) " +
            "AND (:deplacementPossible IS NULL OR w.deplacementPossible = :deplacementPossible)")
    Page<Workshop> findFilteredWorkshops(
            @Param("serviceType") String serviceType,
            @Param("ville") String ville,
            @Param("deplacementPossible") Boolean deplacementPossible,
            Pageable pageable
    );

    @Query("SELECT w FROM Workshop w WHERE w.actif = true AND w.company.valide = true")
    List<Workshop> findByActiveAndCompanyValidated();
}
