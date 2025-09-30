package com.example.research2.SpringBoot;


import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class ActivityInterceptor implements HandlerInterceptor {

    private final PlayerRepo playerRepo;

    public ActivityInterceptor(PlayerRepo playerRepo) {
        this.playerRepo = playerRepo;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName(); // у тебя логин по email
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player != null) {
                player.setLastActive(LocalDateTime.now());
                playerRepo.save(player);
            }
        }

        return true; // пропускаем запрос дальше
    }
}
