package com.example.research2.SpringBoot.repositories;

import com.example.research2.SpringBoot.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long id);

    void deleteAllByReceiverId(Long id);

    List<Notification> findByRelatedId(Long relatedId);
}
