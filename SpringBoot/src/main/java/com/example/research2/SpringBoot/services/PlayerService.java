package com.example.research2.SpringBoot.services;

import com.example.research2.SpringBoot.controllers.PlayersController;
import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Service
public class PlayerService {

    private final PlayerRepo playerRepo;

    @Value("${upload.path}")
    private String uploadPath;

    private static final Logger logger = LoggerFactory.getLogger(PlayersController.class);

    public PlayerService(PlayerRepo playerRepo) {
        this.playerRepo = playerRepo;
    }

    @CacheEvict(value = {"players", "playerByEmail", "playerById"}, allEntries = true)
    public Player registerPlayer(Player player) {

        if (player.getAvatarURL() == null || player.getAvatarURL().isEmpty()) {
            player.setAvatarURL("/images/default-avatar.png"); // или путь к вашей дефолтной аватарке
        }
        return playerRepo.save(player);
    }

    @Cacheable(value = "players", key = "#email")
    public List<Player> findAllExceptCurrentPlayer(String email) {
        List<Player> players = playerRepo.findAllByEmailNot(email);
        // Возвращаем только подтверждённых пользователей
        return players.stream()
                .filter(Player::isVerified)
                .toList();
    }

    @Cacheable(value = "searchPlayers",
            key = "#currentUserEmail + '_' + #name + '_' + #gender + '_' + #country + '_' + #timezone + '_' + #language + '_' + #game + '_' + #ageFrom + '_' + #ageTo")
    public List<Player> searchPlayers(String currentUserEmail, String name, String gender, String country,
                                      String timezone, String language, String game,
                                      Integer ageFrom, Integer ageTo) {
        return playerRepo.findAll().stream()
                .filter(p -> !p.getEmail().equals(currentUserEmail)) // Исключаем текущего пользователя
                .filter(p -> name == null || name.isEmpty() ||
                        p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> gender == null || gender.isEmpty() ||
                        p.getGender().equals(gender))
                .filter(p -> country == null || country.isEmpty() ||
                        (p.getCountry() != null && p.getCountry().toLowerCase().contains(country.toLowerCase())))
                .filter(p -> timezone == null || timezone.isEmpty() ||
                        (p.getTimezone() != null && p.getTimezone().contains(timezone)))
                .filter(p -> language == null || language.isEmpty() ||
                        (p.getLanguages() != null && p.getLanguages().toLowerCase().contains(language.toLowerCase())))
                .filter(p -> game == null || game.isEmpty() ||
                        (p.getGames() != null && p.getGames().toLowerCase().contains(game.toLowerCase())))
                .filter(p -> ageFrom == null ||
                        (p.getBirthday() != null && getAge(p.getBirthday()) >= ageFrom))
                .filter(p -> ageTo == null ||
                        (p.getBirthday() != null && getAge(p.getBirthday()) <= ageTo))
                .collect(Collectors.toList());
    }

    private int getAge(LocalDate birthday) {
        if (birthday == null) {
            return 0;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }


    @Cacheable(value = "playerByEmail", key = "#email", unless = "#result == null")
    public Player findPlayerByEmail(String email) {
        return playerRepo.findByEmail(email).orElse(null);
    }

    public Player findPlayerById(Long id) {
        return playerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Can't find the player"));
    }

    @Caching(evict = {
            @CacheEvict(value = "playerById", key = "#id"),
            @CacheEvict(value = "playerByEmail", allEntries = true),
            @CacheEvict(value = "players", allEntries = true),
            @CacheEvict(value = "searchPlayers", allEntries = true)
    })
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
        currentPlayer.setPrivate(player.isPrivate()); // Добавляем обновление приватности
        playerRepo.save(currentPlayer);
    }

    /**
     * Проверяет, может ли пользователь viewer видеть полный профиль пользователя profileOwner
     * Примечание: этот метод перенесен в PlayersController для избежания циклической зависимости
     */

    public Player findByEmail(String email) {
        return findPlayerByEmail(email);
    }

    @Cacheable(value = "playerById", key = "#id", unless = "#result == null")
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

    @CacheEvict(value = {"players", "playerByEmail", "playerById"}, allEntries = true)
    public boolean verifyPlayer(String verificationToken) {
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

    @Caching(evict = {
            @CacheEvict(value = "playerById", key = "#playerId"),
            @CacheEvict(value = "playerByEmail", allEntries = true),
            @CacheEvict(value = "players", allEntries = true)
    })
    public void updateAvatar(Long playerId, MultipartFile file) throws IOException {
        Player player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath, "avatar");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());
            player.setAvatarURL("/uploads/avatar/" + fileName);
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
            Path uploadDir = Paths.get(uploadPath);
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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "playerById", key = "#player.id"),
            @CacheEvict(value = "playerByEmail", key = "#player.email")
    })
    public void updatePassword(Player player, String newPassword, PasswordEncoder passwordEncoder) {
        player.setPassword(passwordEncoder.encode(newPassword));
        playerRepo.save(player);
    }

    @CacheEvict(value = {"playerById", "onlineStatus"}, key = "#id")
    public void updateLastActive(Long id){
        Player player = playerRepo.findById(id).orElse(null);
        if(player != null){
            player.setLastActive(LocalDateTime.now());
            playerRepo.save(player);
        }
    }


    @Cacheable(value = "onlineStatus", key = "#player.id", unless = "#result == false")
    public boolean isOnline(Player player){
        if(player.getLastActive() != null && player.getLastActive().isAfter(LocalDateTime.now()
                .minusMinutes(2))){
            return true;
        }
        return false;
    }
}