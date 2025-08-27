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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PlayersController {

    private final PlayerService playerService;
    private final PlayerRepo playerRepo;
    private final FriendshipService friendshipService;

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
}