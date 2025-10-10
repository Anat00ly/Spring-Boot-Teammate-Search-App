package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.*;
import com.example.research2.SpringBoot.repositories.FriendshipRepo;
import com.example.research2.SpringBoot.repositories.NotificationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final FriendshipRepo friendshipRepo;

    public NotificationService(NotificationRepo notificationRepo, FriendshipRepo friendshipRepo) {
        this.notificationRepo = notificationRepo;
        this.friendshipRepo = friendshipRepo;
    }

    @Transactional
    public Notification createFriendRequestNotification(Player receiver, Player sender, Long friendshipId) {
        String message = String.format("%s wants to be your friend", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(receiver, sender, NotificationType.FRIEND_REQUEST, message);
        notification.setRelatedId(friendshipId);
        return notificationRepo.save(notification);
    }

    @Transactional
    public void deleteNotificationByRelatedId(Long relatedId) {
        List<Notification> notifications = notificationRepo.findByRelatedId(relatedId);
        if (notifications != null && !notifications.isEmpty()) {
            notificationRepo.deleteAll(notifications);
        }
    }


    @Transactional
    public Notification createFriendAcceptedNotification(Player recipient, Player sender) {
        String message = String.format("%s accepted your friend request", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(recipient, sender, NotificationType.FRIEND_ACCEPTED, message);
        return notificationRepo.save(notification);
    }

    @Transactional
    public Notification createFriendRemovedNotification(Player recipient, Player sender) {
        String message = String.format("%s delete you from friends", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(recipient, sender, NotificationType.FRIEND_REMOVED, message);
        return notificationRepo.save(notification);
    }

    @Transactional
    public List<Notification> getNotificationsForPlayer(Player player) {
        return notificationRepo.findByReceiverIdOrderByCreatedAtDesc(player.getId());
    }

    @Transactional
    public void markAllAsRead(Player player) {
        List<Notification> notifications = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(player.getId());
        boolean changed = false;
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            notificationRepo.saveAll(notifications);
        }
    }


    @Transactional
    public void deleteNotification(Long notificationId, Long playerId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getReceiver().getId().equals(playerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        notificationRepo.delete(notification);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, Long playerId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getReceiver().getId().equals(playerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        notification.setRead(true);
        notificationRepo.save(notification);
    }


    @Transactional
    public void deleteAllNotificationsForPlayer(Player player) {
        notificationRepo.deleteAllByReceiverId(player.getId());
    }

    public void sendRespondPostNotification(Player receiver, Player sender, Long postId){
        String message = String.format("%s is interested in your post", sender.getName() != null ? sender.getName() : "Someone");
        Notification notification = new Notification(receiver, sender, NotificationType.POST_RESPOND, message);
        notification.setRelatedId(postId);
        notification.setRead(false);
        notificationRepo.save(notification);
    }


    @Transactional
    public long getUnreadNotificationsCount(Player player) {
        return notificationRepo.countUnreadByReceiver(player);
    }


}