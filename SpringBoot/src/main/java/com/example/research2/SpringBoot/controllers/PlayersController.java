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
    private final FriendshipService friendshipService;
    private static final Logger logger = LoggerFactory.getLogger(PlayersController.class);

    public PlayersController(PlayerService playerService, PlayerRepo playerRepo, FriendshipService friendshipService) {
        this.playerService = playerService;
        this.playerRepo = playerRepo;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/players")
    public String players(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String timezone,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) Integer ageFrom,
            @RequestParam(required = false) Integer ageTo,
            Model model,
            Principal principal) {

        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());
        if (currentPlayer == null || !currentPlayer.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        // Добавляем списки для выпадающих меню
        model.addAttribute("timezones", TimezoneUtils.getAllTimezones());
        model.addAttribute("languages", LanguageUtils.getAllLanguages());
        model.addAttribute("countries", CountryUtils.getAllCountries()); // ДОБАВЛЕНО!
        model.addAttribute("games", GamesUtils.getAllGames());

        // Если нет параметров поиска - показываем всех игроков
        if (name == null && gender == null && country == null && timezone == null &&
                language == null && game == null && ageFrom == null && ageTo == null) {
            List<Player> players = playerService.findAllExceptCurrentPlayer(principal.getName());
            model.addAttribute("players", players);
        } else {
            // Если есть параметры - применяем фильтры
            List<Player> players = playerService.searchPlayers(
                    principal.getName(), name, gender, country, timezone, language, game, ageFrom, ageTo
            );
            model.addAttribute("players", players);
        }

        return "players";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal) {
        Player player = playerService.findPlayerByEmail(principal.getName());

        if (player == null || !player.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        model.addAttribute("player", player);
        model.addAttribute("currentUserId", player.getId());
        model.addAttribute("isCurrentUser", true);
        model.addAttribute("isFriend", false);
        model.addAttribute("canViewFullProfile", true);

        return "profile";
    }

    @GetMapping("/profile/{id}")
    public String viewPlayerPage(@PathVariable Long id, Model model, Principal principal) {
        Player currentPlayer = playerService.findPlayerByEmail(principal.getName());

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

        // ДОБАВЛЕНО: проверка на наличие ожидающего запроса в друзья
        boolean hasPendingRequest = friendshipService.hasPendingRequest(currentPlayer, viewedPlayer);
        model.addAttribute("hasPendingRequest", hasPendingRequest);

        boolean canViewFullProfile = canViewFullProfile(currentPlayer, viewedPlayer, isFriend);
        model.addAttribute("canViewFullProfile", canViewFullProfile);

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

        if (currentPlayer == null || !currentPlayer.isVerified()) {
            return "redirect:/login?error=email_not_verified";
        }

        playerService.updatePlayer(currentPlayer.getId(), player);
        return "redirect:/profile?success=true";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file, Principal principal) {
        if (file == null || file.isEmpty()) {
            return "redirect:/profile?error=empty";
        }

        try {
            String uploadDir = playerService.getUploadPath();
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                logger.warn("Invalid file name: {}", originalFilename);
                return "redirect:/profile?error=invalid_name";
            }

            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path target = uploadPath.resolve(fileName);

            file.transferTo(target.toFile());

            Player player = playerService.findByEmail(principal.getName());
            player.setAvatarURL("/avatars/" + fileName);
            playerRepo.save(player);

            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error uploading avatar", e);
            return "redirect:/profile?error=upload";
        }
    }

    private boolean canViewFullProfile(Player viewer, Player profileOwner, boolean isFriend) {
        if (viewer.getId().equals(profileOwner.getId())) {
            return true;
        }

        if (!profileOwner.isPrivate()) {
            return true;
        }

        return isFriend;
    }
}