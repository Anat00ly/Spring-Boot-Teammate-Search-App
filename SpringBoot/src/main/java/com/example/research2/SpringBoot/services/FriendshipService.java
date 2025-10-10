package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.FriendRequestStatus;
import com.example.research2.SpringBoot.models.Friendship;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.FriendshipRepo;
import com.example.research2.SpringBoot.repositories.NotificationRepo;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    private final PlayerRepo playerRepo;
    private final FriendshipRepo friendshipRepo;
    private final NotificationService notificationService;
    private final NotificationRepo notificationRepo;


    public FriendshipService(PlayerRepo playerRepo, FriendshipRepo friendshipRepo, NotificationService notificationService, NotificationRepo notificationRepo) {
        this.playerRepo = playerRepo;
        this.friendshipRepo = friendshipRepo;
        this.notificationService = notificationService;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    public List<Player> getFriends(Player player) {
        List<Player> friends = new ArrayList<>();
        for (Friendship request : player.getSentFriendRequest()) {
            if (request.getFriendRequestStatus() == FriendRequestStatus.ACCEPTED) {
                friends.add(request.getReceiver());
            }
        }
        for (Friendship request : player.getReceivedFriendRequest()) {
            if (request.getFriendRequestStatus() == FriendRequestStatus.ACCEPTED) {
                friends.add(request.getSender());
            }
        }
        return friends;
    }

    @Transactional
    public boolean isFriend(Player player1, Player player2) {
        List<Player> friends = getFriends(player1);
        return friends.contains(player2);
    }

    @Transactional
    public boolean sendFriendRequest(Long senderId, Long receiverId) {
        Player sender = playerRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender does not exist"));
        Player receiver = playerRepo.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver does not exist"));

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You can't send request to yourself");
        }

        Optional<Friendship> existing = findFriendshipBetween(senderId, receiverId);
        if (existing.isPresent()) {
            FriendRequestStatus status = existing.get().getFriendRequestStatus();
            if (status == FriendRequestStatus.PENDING) {
                throw new IllegalArgumentException("Friend request already pending");
            } else if (status == FriendRequestStatus.ACCEPTED) {
                throw new IllegalArgumentException("You are already friends");
            }
        }



        Friendship request = new Friendship(receiver, sender, FriendRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        Friendship saved = friendshipRepo.save(request);

        notificationService.createFriendRequestNotification(receiver, sender, saved.getId());
        return true;
    }

    @Transactional
    public boolean removeFriend(Long friendshipId, Long currentPlayerId) {
        Friendship friendship = friendshipRepo.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        boolean isSender = friendship.getSender().getId().equals(currentPlayerId);
        boolean isReceiver = friendship.getReceiver().getId().equals(currentPlayerId);
        if (!isSender && !isReceiver) {
            throw new IllegalArgumentException("Unauthorized action");
        }

        if (friendship.getFriendRequestStatus() != FriendRequestStatus.ACCEPTED) {
            throw new IllegalArgumentException("You can only remove accepted friends");
        }

        Player otherPlayer = isSender ? friendship.getReceiver() : friendship.getSender();
        Player currentPlayer = isSender ? friendship.getSender() : friendship.getReceiver();

        notificationService.createFriendRemovedNotification(otherPlayer, currentPlayer);
        friendshipRepo.delete(friendship);
        return true;
    }


    @Transactional
    public boolean acceptFriendRequest(Long friendshipId, Long currentPlayerId) {
        Friendship friendship = friendshipRepo.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Request does not exist"));

        if (!friendship.getReceiver().getId().equals(currentPlayerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        if (friendship.getFriendRequestStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request cannot be accepted");
        }

        friendship.setFriendRequestStatus(FriendRequestStatus.ACCEPTED);
        friendship.setUpdatedAt(LocalDateTime.now());
        friendshipRepo.save(friendship);

        // создаём уведомление о принятии заявки
        notificationService.createFriendAcceptedNotification(friendship.getSender(), friendship.getReceiver());

        // ✅ удаляем уведомление о запросе дружбы
        notificationService.deleteNotificationByRelatedId(friendshipId);


        return true;
    }

    @Transactional
    public void declineFriendRequest(Long friendshipId, Long currentPlayerId) {
        Friendship friendship = friendshipRepo.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!friendship.getReceiver().getId().equals(currentPlayerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }

        // удаляем уведомление
        notificationService.deleteNotificationByRelatedId(friendshipId);

        // удаляем заявку в друзья
        friendshipRepo.delete(friendship);
    }


    @Transactional
    public Optional<Friendship> findFriendshipBetween(Long player1Id, Long player2Id) {
        return friendshipRepo.findBySenderIdAndReceiverId(player1Id, player2Id)
                .or(() -> friendshipRepo.findBySenderIdAndReceiverId(player2Id, player1Id));
    }


    @Transactional
    public boolean hasPendingRequest(Long senderId, Long receiverId) {
        return friendshipRepo.existsBySenderIdAndReceiverIdAndFriendRequestStatus(senderId, receiverId, FriendRequestStatus.PENDING) ||
                friendshipRepo.existsBySenderIdAndReceiverIdAndFriendRequestStatus(receiverId, senderId, FriendRequestStatus.PENDING);
    }

    @Transactional
    public boolean areFriends(Long player1Id, Long player2Id) {
        return friendshipRepo.existsBySenderIdAndReceiverIdAndFriendRequestStatus(player1Id, player2Id, FriendRequestStatus.ACCEPTED) ||
                friendshipRepo.existsBySenderIdAndReceiverIdAndFriendRequestStatus(player2Id, player1Id, FriendRequestStatus.ACCEPTED);
    }

    public boolean hasPendingRequest(Player currentPlayer, Player viewedPlayer) {
        return friendshipRepo.existsBySenderAndReceiverAndFriendRequestStatus(
                viewedPlayer, currentPlayer, FriendRequestStatus.PENDING
        );
    }


}