package com.example.research2.SpringBoot.repositories;

import com.example.research2.SpringBoot.models.Notification;
import com.example.research2.SpringBoot.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long id);

    void deleteAllByReceiverId(Long id);

    List<Notification> findByRelatedId(Long relatedId);


    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver = :receiver AND n.isRead = false")
    long countUnreadByReceiver(@Param("receiver") Player receiver);
}
