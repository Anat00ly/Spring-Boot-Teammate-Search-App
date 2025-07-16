package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import com.example.research2.SpringBoot.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class AuthController {

    private final PlayerRepo playerRepo;

    @Autowired
    private final PlayerService playerService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(PlayerService playerService, PlayerRepo playerRepo) {
        this.playerService = playerService;
        this.playerRepo = playerRepo;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("player", new Player());
        return "register";
    }

    @PostMapping("/register")
    public String performRegister(@ModelAttribute("player") Player player) {

        if(playerRepo.existsByEmail(player.getEmail())) {
            return "redirect:/register";
        }
        player.setPassword(passwordEncoder.encode(player.getPassword()));

        playerRepo.save(player);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

}
