package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Friendship;
import com.example.research2.SpringBoot.models.Notification;
import com.example.research2.SpringBoot.models.NotificationType;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.services.FriendshipService;
import com.example.research2.SpringBoot.services.NotificationService;
import com.example.research2.SpringBoot.services.PlayerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class FriendshipController {

    private final PlayerService playerService;
    private final FriendshipService friendshipService;
    private final NotificationService notificationService;

    public FriendshipController(PlayerService playerService, FriendshipService friendshipService, NotificationService notificationService) {
        this.playerService = playerService;
        this.friendshipService = friendshipService;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile/friendList")
    public String showFriendList(Model model, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        model.addAttribute("friends", friendshipService.getFriends(player));
        model.addAttribute("player", player);
        model.addAttribute("name", player.getName() != null ? player.getName() : "User");
        return "friendList";
    }

    @PostMapping("/add/{receiverId}")
    public String sendFriendRequest(@PathVariable Long receiverId, Principal principal) {
        Player sender = playerService.findPlayerByEmail(principal.getName());
        friendshipService.sendFriendRequest(sender.getId(), receiverId);
        return "redirect:/mainPage";
    }

    @PostMapping("/friendship/accept/{notificationId}")
    public String acceptFriendRequest(@PathVariable Long notificationId, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        Notification notification = notificationService.getNotificationsForPlayer(player).stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getReceiver().getId().equals(player.getId())) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        if (notification.getType() != NotificationType.FRIEND_REQUEST || notification.isRead()) {
            throw new IllegalArgumentException("Invalid notification");
        }
        if (notification.getRelatedId() == null) {
            throw new IllegalArgumentException("No associated friendship found");
        }

        friendshipService.acceptFriendRequest(notification.getRelatedId(), player.getId());
        notificationService.markNotificationAsRead(notificationId, player.getId());
        return "redirect:/notifications";
    }

    @PostMapping("/friendship/reject/{notificationId}")
    public String rejectFriendRequest(@PathVariable Long notificationId, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        Notification notification = notificationService.getNotificationsForPlayer(player).stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getReceiver().getId().equals(player.getId())) {
            throw new IllegalArgumentException("Unauthorized action");
        }
        if (notification.getType() != NotificationType.FRIEND_REQUEST || notification.isRead()) {
            throw new IllegalArgumentException("Invalid notification");
        }
        if (notification.getRelatedId() == null) {
            throw new IllegalArgumentException("No associated friendship found");
        }

        friendshipService.declineFriendRequest(notification.getRelatedId(), player.getId());
        notificationService.markNotificationAsRead(notificationId, player.getId());
        return "redirect:/notifications";
    }

    @PostMapping("/remove/{friendId}")
    public String removeFriend(@PathVariable Long friendId, Principal principal) {
        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());
        Optional<Friendship> friendshipOpt = friendshipService.findFriendshipBetween(currentPlayer.getId(), friendId);

        if (friendshipOpt.isPresent()) {
            Friendship friendship = friendshipOpt.get();
            Long actualFriendId = friendship.getSender().getId().equals(currentPlayer.getId())
                    ? friendship.getReceiver().getId()
                    : friendship.getSender().getId();

            boolean removed = friendshipService.removeFriend(friendship.getId(), currentPlayer.getId());
            return "redirect:/profile/" + actualFriendId;
        }
        return "redirect:/mainPage";
    }
}