package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.services.NotificationService;
import com.example.research2.SpringBoot.services.PlayerService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;
    private final PlayerService playerService;

    public GlobalControllerAdvice(NotificationService notificationService, PlayerService playerService) {
        this.notificationService = notificationService;
        this.playerService = playerService;
    }

    @ModelAttribute("name")
    public String globalUsername(Principal principal) {
        return principal != null ? principal.getName() : null;
    }

    @ModelAttribute("unreadNotificationsCount")
    public Long globalUnreadNotificationsCount(Principal principal) {
        if (principal != null) {
            try {
                Player player = playerService.findPlayerByEmail(principal.getName());
                if (player != null) {
                    return notificationService.getUnreadNotificationsCount(player);
                }
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем работу приложения
                System.err.println("Error getting unread notifications count: " + e.getMessage());
            }
        }
        return 0L;
    }
}