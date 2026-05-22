package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Team;
import com.hephaitos.maintenance.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByWorkshop(Workshop workshop);
    List<Team> findByWorkshopAndActif(Workshop workshop, boolean actif);
}
