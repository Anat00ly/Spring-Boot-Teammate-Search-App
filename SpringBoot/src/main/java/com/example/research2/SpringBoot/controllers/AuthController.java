package com.example.research2.SpringBoot.controllers;

import com.example.research2.SpringBoot.models.Player;
import com.example.research2.SpringBoot.repositories.PlayerRepo;
import com.example.research2.SpringBoot.services.PlayerService;
import com.example.research2.SpringBoot.services.EmailService;
import com.example.research2.SpringBoot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final PlayerRepo playerRepo;
    private final PlayerService playerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

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
    public String performRegister(@ModelAttribute("player") Player player, Model model) {
        try {
            Player existingPlayer = playerRepo.findByEmail(player.getEmail()).orElse(null);

            if (existingPlayer != null) {
                if (existingPlayer.isVerified()) {
                    model.addAttribute("error", "Пользователь с таким email уже существует и подтвержден.");
                    return "register";
                } else {
                    String verificationToken = JwtTokenUtils.generateToken(existingPlayer.getEmail());
                    existingPlayer.setVerificationToken(verificationToken);
                    playerRepo.save(existingPlayer);

                    emailService.sendVerificationEmail(existingPlayer.getEmail(), verificationToken);
                    model.addAttribute("message", "Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
                    return "login";
                }
            }

            player.setPassword(passwordEncoder.encode(player.getPassword()));
            String verificationToken = JwtTokenUtils.generateToken(player.getEmail());
            player.setVerificationToken(verificationToken);
            player.setVerified(false);

            playerRepo.save(player);

            emailService.sendVerificationEmail(player.getEmail(), verificationToken);

            model.addAttribute("message", "Регистрация успешна! Проверьте вашу почту для подтверждения аккаунта.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLogin(Model model,
                            @RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "error", required = false) String error) {
        if (message != null) {
            model.addAttribute("message", message);
        }
        if (error != null) {
            if ("email_not_verified".equals(error)) {
                model.addAttribute("error", "Для доступа необходимо подтвердить email. Проверьте вашу почту.");
            } else {
                model.addAttribute("error", error);
            }
        }
        return "login";
    }

    @GetMapping("/req/signup/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        try {
            String email = jwtTokenUtils.extractEmail(token);
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null || player.getVerificationToken() == null) {
                model.addAttribute("error", "Неверная ссылка или токен истек!");
                return "login";
            }

            if (!jwtTokenUtils.validateToken(token) || !player.getVerificationToken().equals(token)) {
                model.addAttribute("error", "Неверная ссылка или токен истек!");
                return "login";
            }

            player.setVerificationToken(null);
            player.setVerified(true);
            playerRepo.save(player);

            model.addAttribute("message", "Email успешно подтвержден! Теперь вы можете войти в систему.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при подтверждении: " + e.getMessage());
            return "login";
        }
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email, Model model) {
        try {
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null) {
                model.addAttribute("error", "Пользователь с таким email не найден.");
                return "register";
            }

            if (player.isVerified()) {
                model.addAttribute("message", "Ваш аккаунт уже подтвержден. Можете войти в систему.");
                return "login";
            }

            String verificationToken = JwtTokenUtils.generateToken(player.getEmail());
            player.setVerificationToken(verificationToken);
            playerRepo.save(player);

            emailService.sendVerificationEmail(player.getEmail(), verificationToken);
            model.addAttribute("message", "Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при отправке письма: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null) {
                model.addAttribute("error", "Пользователь с таким email не найден.");
                return "forgot-password";
            }

            if (!player.isVerified()) {
                model.addAttribute("error", "Сначала подтвердите ваш email адрес.");
                return "forgot-password";
            }

            String resetToken = JwtTokenUtils.generateToken(player.getEmail());
            player.setResetToken(resetToken);
            playerRepo.save(player);

            emailService.sendForgotPasswordEmail(player.getEmail(), resetToken);

            model.addAttribute("message", "Инструкции по восстановлению пароля отправлены на вашу почту.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при отправке письма: " + e.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/req/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        try {
            String email = jwtTokenUtils.extractEmail(token);
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null || player.getResetToken() == null) {
                model.addAttribute("error", "Неверная ссылка или токен истек!");
                return "login";
            }

            if (!jwtTokenUtils.validateToken(token) || !player.getResetToken().equals(token)) {
                model.addAttribute("error", "Неверная ссылка или токен истек!");
                return "login";
            }

            model.addAttribute("token", token);
            return "reset-password";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при проверке токена: " + e.getMessage());
            return "login";
        }
    }

    @PostMapping("/req/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Пароли не совпадают!");
                model.addAttribute("token", token);
                return "reset-password";
            }

            if (!jwtTokenUtils.validateToken(token)) {
                model.addAttribute("error", "Ссылка для сброса пароля недействительна или истекла!");
                return "login";
            }

            String email = jwtTokenUtils.extractEmail(token);
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null) {
                model.addAttribute("error", "Пользователь не найден!");
                return "login";
            }

            player.setPassword(passwordEncoder.encode(password));
            player.setResetToken(null);
            playerRepo.save(player);

            model.addAttribute("message", "Пароль успешно обновлен! Теперь вы можете войти с новым паролем.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при сбросе пароля: " + e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}