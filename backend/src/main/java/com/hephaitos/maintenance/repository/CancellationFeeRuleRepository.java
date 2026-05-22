package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.CancellationFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationFeeRuleRepository extends JpaRepository<CancellationFeeRule, Long> {
    Optional<CancellationFeeRule> findBySeuilHeuresAvant(int seuilHeuresAvant);
    List<CancellationFeeRule> findAllByOrderBySeuilHeuresAvantDesc();
}
