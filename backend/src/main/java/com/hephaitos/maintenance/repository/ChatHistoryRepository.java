package com.hephaitos.maintenance.repository;

import com.hephaitos.maintenance.entity.ChatHistory;
import com.hephaitos.maintenance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserOrderByHorodatageAsc(User user);
}
