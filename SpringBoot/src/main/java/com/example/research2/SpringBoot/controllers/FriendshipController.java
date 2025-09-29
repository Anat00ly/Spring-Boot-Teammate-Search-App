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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String sendFriendRequest(@PathVariable Long receiverId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Player sender = playerService.findPlayerByEmail(principal.getName());
            friendshipService.sendFriendRequest(sender.getId(), receiverId);
            redirectAttributes.addFlashAttribute("success", "Friend request sent successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/" + receiverId;
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

        // вызываем сервис для принятия заявки и удаления уведомления
        friendshipService.acceptFriendRequest(notification.getRelatedId(), player.getId());

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

        // удаляем заявку и уведомление
        friendshipService.declineFriendRequest(notification.getRelatedId(), player.getId());

        // ✅ НЕ вызываем markNotificationAsRead()
        return "redirect:/notifications";
    }

    @PostMapping("/remove/{friendId}")
    public String removeFriend(@PathVariable Long friendId, Principal principal, RedirectAttributes redirectAttributes) {
        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());
        Optional<Friendship> friendshipOpt = friendshipService.findFriendshipBetween(currentPlayer.getId(), friendId);
        if (friendshipOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Friendship not found");
            return "redirect:/mainPage";
        }

        Friendship friendship = friendshipOpt.get();
        Long redirectId = friendship.getSender().getId().equals(currentPlayer.getId())
                ? friendship.getReceiver().getId()
                : friendship.getSender().getId();

        try {
            friendshipService.removeFriend(friendship.getId(), currentPlayer.getId());
            redirectAttributes.addFlashAttribute("success", "Friend removed");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/" + redirectId;
    }

}