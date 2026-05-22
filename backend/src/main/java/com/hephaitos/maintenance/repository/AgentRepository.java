package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Agent;
import com.hephaitos.maintenance.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByValide(boolean valide);
    List<Agent> findByTeam(Team team);
}
