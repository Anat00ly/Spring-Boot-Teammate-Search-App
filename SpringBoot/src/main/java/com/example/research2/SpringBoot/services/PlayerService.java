package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepo playerRepo;

    public PlayerService(PlayerRepo playerRepo) {
        this.playerRepo = playerRepo;
    }

    public Player registerPlayer(Player player) {
        return playerRepo.save(player);
    }

    public List<Player> findAllExceptCurrentPlayer(String email){
        List<Player> players = playerRepo.findAllByEmailNot(email);
        // Возвращаем только подтвержденных пользователей
        return players.stream()
                .filter(Player::isVerified)
                .toList();
    }

    public Player findPlayerByEmail(String email) {
        return playerRepo.findByEmail(email).orElse(null);
    }

    public Player findPlayerById(Long id) {
        return playerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Can't find the player"));
    }

    public void updatePlayer(Long id, Player player) {
        Player currentPlayer = playerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Can't find the player"));

        currentPlayer.setName(player.getName());
        currentPlayer.setBirthday(player.getBirthday());
        currentPlayer.setGender(player.getGender());
        currentPlayer.setCountry(player.getCountry());
        currentPlayer.setTimezone(player.getTimezone());
        currentPlayer.setLanguages(player.getLanguages());
        currentPlayer.setGames(player.getGames());
        currentPlayer.setTgLink(player.getTgLink());
        playerRepo.save(currentPlayer);
    }

    public Player findByEmail(String email) {
        return findPlayerByEmail(email);
    }

    public Player findById(Long id) {
        return findPlayerById(id);
    }

    // Дополнительные методы для работы с верификацией

    public boolean existsByEmail(String email) {
        return playerRepo.existsByEmail(email);
    }

    public void updateVerificationToken(String email, String token) {
        Player player = playerRepo.findByEmail(email).orElse(null);
        if (player != null) {
            player.setVerificationToken(token);
            playerRepo.save(player);
        }
    }

    public boolean verifyPlayer(String verificationToken) {
        // Найдем пользователя по токену (нужно добавить метод в репозиторий)
        List<Player> allPlayers = playerRepo.findAll();
        for (Player player : allPlayers) {
            if (verificationToken.equals(player.getVerificationToken())) {
                player.setVerified(true);
                player.setVerificationToken(null);
                playerRepo.save(player);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void updatePassword(Player player, String newPassword, PasswordEncoder passwordEncoder) {
        player.setPassword(passwordEncoder.encode(newPassword));
        playerRepo.save(player);
    }

}