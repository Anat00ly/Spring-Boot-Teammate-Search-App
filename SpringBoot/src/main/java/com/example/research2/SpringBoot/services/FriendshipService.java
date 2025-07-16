package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.FriendRequestStatus;
import com.example.research2.SpringBoot.models.Friendship;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.FriendshipRepo;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    private final PlayerRepo playerRepo;
    private final FriendshipRepo friendshipRepo;
    private final NotificationService notificationService;

    public FriendshipService(PlayerRepo playerRepo, FriendshipRepo friendshipRepo, NotificationService notificationService) {
        this.playerRepo = playerRepo;
        this.friendshipRepo = friendshipRepo;
        this.notificationService = notificationService;
    }

    public List<Player> getFriends(Player player) {
        List<Player> friends = new ArrayList<>();
        for (Friendship request : player.getSentFriendRequest()) {
            if (request.getStatus() == FriendRequestStatus.ACCEPTED) {
                friends.add(request.getReceiver());
            }
        }
        for (Friendship request : player.getReceivedFriendRequest()) {
            if (request.getStatus() == FriendRequestStatus.ACCEPTED) {
                friends.add(request.getSender());
            }
        }
        return friends;
    }

    public boolean isFriend(Player player1, Player player2) {
        List<Player> friends = getFriends(player1);
        return friends.contains(player2);
    }

    public boolean sendFriendRequest(Long senderId, Long receiverId) {
        Optional<Player> senderOpt = playerRepo.findById(senderId);
        Optional<Player> receiverOpt = playerRepo.findById(receiverId);

        if (senderOpt.isEmpty()) {
            throw new IllegalArgumentException("Sender does not exist");
        }
        if (receiverOpt.isEmpty()) {
            throw new IllegalArgumentException("Receiver does not exist");
        }

        Player sender = senderOpt.get();
        Player receiver = receiverOpt.get();

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You can't send request to yourself");
        }

        if (friendshipRepo.existsBySenderIdAndReceiverId(senderId, receiverId)) {
            throw new IllegalArgumentException("You are already friends or have a pending request");
        }

        Friendship request = new Friendship(receiver, sender, FriendRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        friendshipRepo.save(request);

        notificationService.createFriendRequestNotification(receiver, sender);

        return true;
    }

    public boolean acceptFriendRequest(Long friendshipId, Long currentPlayerId) {
        Optional<Friendship> friendshipOpt = friendshipRepo.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Request does not exist");
        }

        Friendship friendship = friendshipOpt.get();
        if (!friendship.getReceiver().getId().equals(currentPlayerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        if (friendship.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request cannot be accepted");
        }

        friendship.setStatus(FriendRequestStatus.ACCEPTED);
        friendship.setUpdatedAt(LocalDateTime.now());
        friendshipRepo.save(friendship);

        notificationService.createFriendAcceptedNotification(friendship.getSender(), friendship.getReceiver());

        return true;
    }

    public boolean declineFriendRequest(Long friendshipId, Long currentPlayerId) {
        Optional<Friendship> friendshipOpt = friendshipRepo.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("Request does not exist");
        }

        Friendship friendship = friendshipOpt.get();
        if (!friendship.getReceiver().getId().equals(currentPlayerId)) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        if (friendship.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request cannot be declined");
        }

        friendship.setStatus(FriendRequestStatus.DECLINED);
        friendship.setUpdatedAt(LocalDateTime.now());
        friendshipRepo.save(friendship);

        return true;
    }

    public Optional<Friendship> findFriendshipBetween(Long player1Id, Long player2Id) {
        return friendshipRepo.findBySenderIdAndReceiverId(player1Id, player2Id)
                .or(() -> friendshipRepo.findBySenderIdAndReceiverId(player2Id, player1Id));
    }

    public boolean removeFriend(Long friendshipId, Long currentPlayerId) {
        Optional<Friendship> friendshipOpt = friendshipRepo.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            System.out.println("Friendship not found for ID: " + friendshipId);
            return false;
        }

        Friendship friendship = friendshipOpt.get();
        System.out.println("Removing friendship with ID: " + friendship.getId() + " between sender ID: " + friendship.getSender().getId() + " and receiver ID: " + friendship.getReceiver().getId());

        Player recipient = friendship.getSender().getId().equals(currentPlayerId) ? friendship.getReceiver() : friendship.getSender();
        notificationService.createFriendRemovedNotification(recipient, friendship.getSender().getId().equals(currentPlayerId) ? friendship.getSender() : friendship.getReceiver());

        friendshipRepo.delete(friendship);
        System.out.println("Friendship deleted successfully");

        return true;
    }
}