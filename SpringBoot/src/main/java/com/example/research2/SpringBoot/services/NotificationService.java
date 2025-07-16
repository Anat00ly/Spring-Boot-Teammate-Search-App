package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.Friendship;
import com.example.research2.SpringBoot.models.Notification;
import com.example.research2.SpringBoot.models.NotificationType;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.FriendshipRepo;
import com.example.research2.SpringBoot.repositories.NotificationRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final FriendshipRepo friendshipRepo;

    public NotificationService(NotificationRepo notificationRepo, FriendshipRepo friendshipRepo) {
        this.notificationRepo = notificationRepo;
        this.friendshipRepo = friendshipRepo;
    }

    public Notification createFriendRequestNotification(Player receiver, Player sender) {
        String message = String.format("%s want to be your friend", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(receiver, sender, NotificationType.FRIEND_REQUEST, message);
        Friendship friendship = friendshipRepo.findBySenderIdAndReceiverId(sender.getId(), receiver.getId())
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
        notification.setRelatedId(friendship.getId());
        return notificationRepo.save(notification);
    }

    public Notification createFriendAcceptedNotification(Player recipient, Player sender) {
        String message = String.format("%s accepted your friend request", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(recipient, sender, NotificationType.FRIEND_ACCEPTED, message);
        return notificationRepo.save(notification);
    }

    public Notification createFriendRemovedNotification(Player recipient, Player sender) {
        String message = String.format("%s delete you from friends", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(recipient, sender, NotificationType.FRIEND_REMOVED, message);
        return notificationRepo.save(notification);
    }

    public List<Notification> getNotificationsForPlayer(Player player) {
        return notificationRepo.findByReceiverIdOrderByCreatedAtDesc(player.getId());
    }

    public void markAllAsRead(Player player) {
        List<Notification> notifications = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(player.getId());
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepo.save(notification);
            }
        }
    }

    public void deleteNotification(Long notificationId, Long playerId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getReceiver().getId().equals(playerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        notificationRepo.delete(notification);
    }

    public void markNotificationAsRead(Long notificationId, Long playerId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getReceiver().getId().equals(playerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        notification.setRead(true);
        notificationRepo.save(notification);
    }
}