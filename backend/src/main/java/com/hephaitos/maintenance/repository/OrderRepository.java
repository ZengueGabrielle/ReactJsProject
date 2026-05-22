package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Order;
import com.hephaitos.maintenance.entity.OrderStatus;
import com.hephaitos.maintenance.entity.Team;
import com.hephaitos.maintenance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByDateCreationDesc(User user);

    @Query("SELECT o FROM Order o WHERE o.team = :team AND o.date = :date AND o.statut IN :statuses")
    List<Order> findTeamOrdersForDate(
            @Param("team") Team team,
            @Param("date") LocalDate date,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT o FROM Order o WHERE o.workshop.company.id = :companyId ORDER BY o.dateCreation DESC")
    List<Order> findByCompanyId(@Param("companyId") Long companyId);
}
