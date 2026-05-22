package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByValide(boolean valide);
}
