package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Invoice;
import com.hephaitos.maintenance.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrder(Order order);
}
