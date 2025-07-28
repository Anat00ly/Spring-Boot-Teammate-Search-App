package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        return players;
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


}
