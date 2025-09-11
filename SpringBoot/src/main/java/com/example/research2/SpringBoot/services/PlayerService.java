package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.controllers.PlayersController;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerService {
    private final PlayerRepo playerRepo;

    @Value("${upload.path}")
    private String uploadPath; // e.g. uploads/avatars

    private static final Logger logger = LoggerFactory.getLogger(PlayersController.class);


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

    public void updateAvatar(Long playerId, MultipartFile file) throws IOException {
        Player player = playerRepo.findById(playerId)
                .orElseThrow(()-> new RuntimeException("Player not found"));

        if(file !=null && !file.isEmpty()){
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath,"avatar");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            player.setAvatarURL("/uploads/avatar" + fileName);
            playerRepo.save(player);
        }

    }


    public String getUploadPath() {
        return uploadPath;
    }

    /**
     * Вспомогательный метод: сохраняет файл и обновляет player.avatarURL.
     * Возвращает публичный URL (/avatars/<file>) или null в случае ошибки.
     */
    public String saveAvatarFile(Player player, MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String original = StringUtils.cleanPath(file.getOriginalFilename());
            if (original.contains("..")) {
                throw new IOException("Invalid file name");
            }

            String filename = UUID.randomUUID().toString() + "_" + original;
            Path uploadDir = Paths.get(uploadPath); // uploadPath может быть относительным или абсолютным
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(filename);
            file.transferTo(filePath.toFile());

            String publicUrl = "/avatars/" + filename;
            player.setAvatarURL(publicUrl);
            playerRepo.save(player);
            return publicUrl;
        } catch (Exception e) {
            logger.error("Error saving avatar file", e);
            return null;
        }
    }

}