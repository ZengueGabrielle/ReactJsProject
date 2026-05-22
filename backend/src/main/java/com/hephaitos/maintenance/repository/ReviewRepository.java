package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.Review;
import com.hephaitos.maintenance.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByWorkshop(Workshop workshop);
}
