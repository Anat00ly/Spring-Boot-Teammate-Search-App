package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import com.example.research2.SpringBoot.services.FriendshipService;
import com.example.research2.SpringBoot.services.PlayerService;
import com.example.research2.SpringBoot.util.CountryUtils;
import com.example.research2.SpringBoot.util.GamesUtils;
import com.example.research2.SpringBoot.util.LanguageUtils;
import com.example.research2.SpringBoot.util.TimezoneUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class PlayersController {

    private final PlayerService playerService;
    private final PlayerRepo playerRepo;
    private final FriendshipService friendshipService;private static final Logger logger = LoggerFactory.getLogger(PlayersController.class);



    public PlayersController(PlayerService playerService, PlayerRepo playerRepo, FriendshipService friendshipService) {
        this.playerService = playerService;
        this.playerRepo = playerRepo;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/players")
    public String players(Model model, Principal principal) {
        // Проверяем, подтвержден ли аккаунт пользователя
        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());
        if (currentPlayer == null || !currentPlayer.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        List<Player> players = playerService.findAllExceptCurrentPlayer(principal.getName());
        model.addAttribute("players", players);
        return "players";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());

        // Проверяем верификацию
        if (player == null || !player.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        model.addAttribute("player", player);
        model.addAttribute("currentUserId", player.getId());
        model.addAttribute("isCurrentUser", true);
        model.addAttribute("isFriend", false);

        return "profile";
    }

    @GetMapping("/profile/{id}")
    public String viewPlayerPage(@PathVariable Long id, Model model, Principal principal) {
        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());

        // Проверяем верификацию текущего пользователя
        if (currentPlayer == null || !currentPlayer.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        Player viewedPlayer = playerService.findPlayerById(id);
        if (viewedPlayer == null) {
            return "redirect:/players";
        }

        model.addAttribute("player", viewedPlayer);
        model.addAttribute("currentUserId", currentPlayer.getId());

        boolean isCurrentUser = viewedPlayer.getId().equals(currentPlayer.getId());
        model.addAttribute("isCurrentUser", isCurrentUser);

        boolean isFriend = friendshipService.isFriend(currentPlayer, viewedPlayer);
        model.addAttribute("isFriend", isFriend);

        List<Player> friends = friendshipService.getFriends(viewedPlayer);
        model.addAttribute("friends", friends != null ? friends : new ArrayList<>());

        if (isFriend) {
            friendshipService.findFriendshipBetween(currentPlayer.getId(), viewedPlayer.getId())
                    .ifPresent(friendship -> model.addAttribute("friendshipId", friendship.getId()));
        }

        return "profile";
    }

    @GetMapping("/settings")
    public String showSettingsPage(Model model, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());

        // Проверяем верификацию
        if (player == null || !player.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        model.addAttribute("player", player);
        model.addAttribute("timezones", TimezoneUtils.getAllTimezones());
        model.addAttribute("languages", LanguageUtils.getAllLanguages());
        model.addAttribute("countries", CountryUtils.getAllCountries());
        model.addAttribute("games", GamesUtils.getAllGames());
        return "settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute("player") Player player, Principal principal) {
        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());

        // Проверяем верификацию
        if (currentPlayer == null || !currentPlayer.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        playerService.updatePlayer(currentPlayer.getId(), player);
        return "redirect:/settings";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file, Principal principal) {
        if (file == null || file.isEmpty()) {
            return "redirect:/profile?error=empty";
        }

        try {
            // берем путь из playerService (оно подхватывает значение upload.path)
            String uploadDir = playerService.getUploadPath(); // добавим getter в сервис ниже

            // создаём папку, если её нет
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // защищённое имя файла
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            // запретим попытки path traversal
            if (originalFilename.contains("..")) {
                logger.warn("Invalid file name: {}", originalFilename);
                return "redirect:/profile?error=invalid_name";
            }

            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path target = uploadPath.resolve(fileName);

            // сохраняем файл
            file.transferTo(target.toFile());

            // обновляем URL аватара в базе данных — публичный URL должен начинаться с /avatars/
            Player player = playerService.findByEmail(principal.getName());
            player.setAvatarURL("/avatars/" + fileName);
            playerRepo.save(player);

            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error uploading avatar", e);
            return "redirect:/profile?error=upload";
        }
    }
}