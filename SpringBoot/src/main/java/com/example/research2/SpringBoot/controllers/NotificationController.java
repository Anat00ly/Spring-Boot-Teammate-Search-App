package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Notification;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.services.NotificationService;
import com.example.research2.SpringBoot.services.PlayerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final PlayerService playerService;

    public NotificationController(NotificationService notificationService, PlayerService playerService) {
        this.notificationService = notificationService;
        this.playerService = playerService;
    }

    @GetMapping("/notifications")
    public String showNotificationPage(Model model, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        List<Notification> notifications = notificationService.getNotificationsForPlayer(player);
        model.addAttribute("notifications", notifications);
        return "notifications";
    }

    @PostMapping("/notifications/markAllAsRead")
    public String markAllAsRead(Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        notificationService.markAllAsRead(player);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/delete/{notificationId}")
    public String deleteNotification(@PathVariable Long notificationId, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());
        notificationService.deleteNotification(notificationId, player.getId());
        return "redirect:/notifications";
    }
}