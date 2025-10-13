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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final PlayerRepo playerRepo;
    private final PlayerService playerService;
    private final EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    // Используем конструктор вместо @Autowired для EmailService
    public AuthController(PlayerService playerService, PlayerRepo playerRepo, EmailService emailService) {
        this.playerService = playerService;
        this.playerRepo = playerRepo;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("player", new Player());
        return "register";
    }

    @PostMapping("/register")
    public String performRegister(@ModelAttribute("player") Player player,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
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

                    // АСИНХРОННАЯ отправка - пользователь не ждет
                    emailService.sendVerificationEmail(existingPlayer.getEmail(), verificationToken);

                    redirectAttributes.addFlashAttribute("message",
                            "Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
                    return "redirect:/login";
                }
            }

            player.setPassword(passwordEncoder.encode(player.getPassword()));
            String verificationToken = JwtTokenUtils.generateToken(player.getEmail());
            player.setVerificationToken(verificationToken);
            player.setVerified(false);

            playerRepo.save(player);

            // АСИНХРОННАЯ отправка - регистрация завершается мгновенно
            emailService.sendVerificationEmail(player.getEmail(), verificationToken);

            redirectAttributes.addFlashAttribute("message",
                    "Регистрация успешна! Проверьте вашу почту для подтверждения аккаунта.");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Registration error for email: {}", player.getEmail(), e);
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
    public String verifyEmail(@RequestParam("token") String token,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = jwtTokenUtils.extractEmail(token);
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null || player.getVerificationToken() == null) {
                redirectAttributes.addFlashAttribute("error", "Неверная ссылка или токен истек!");
                return "redirect:/login";
            }

            if (!jwtTokenUtils.validateToken(token) || !player.getVerificationToken().equals(token)) {
                redirectAttributes.addFlashAttribute("error", "Неверная ссылка или токен истек!");
                return "redirect:/login";
            }

            player.setVerificationToken(null);
            player.setVerified(true);
            playerRepo.save(player);

            logger.info("Email verified successfully for: {}", email);
            redirectAttributes.addFlashAttribute("message",
                    "Email успешно подтвержден! Теперь вы можете войти в систему.");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Email verification error for token: {}", token, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при подтверждении: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email,
                                     RedirectAttributes redirectAttributes) {
        try {
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь с таким email не найден.");
                return "redirect:/register";
            }

            if (player.isVerified()) {
                redirectAttributes.addFlashAttribute("message",
                        "Ваш аккаунт уже подтвержден. Можете войти в систему.");
                return "redirect:/login";
            }

            String verificationToken = JwtTokenUtils.generateToken(player.getEmail());
            player.setVerificationToken(verificationToken);
            playerRepo.save(player);

            // АСИНХРОННАЯ отправка
            emailService.sendVerificationEmail(player.getEmail(), verificationToken);

            redirectAttributes.addFlashAttribute("message",
                    "Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Resend verification error for email: {}", email, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке письма: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь с таким email не найден.");
                return "redirect:/forgot-password";
            }

            if (!player.isVerified()) {
                redirectAttributes.addFlashAttribute("error", "Сначала подтвердите ваш email адрес.");
                return "redirect:/forgot-password";
            }

            String resetToken = JwtTokenUtils.generateToken(player.getEmail());
            player.setResetToken(resetToken);
            playerRepo.save(player);

            // АСИНХРОННАЯ отправка email восстановления пароля
            emailService.sendForgotPasswordEmail(player.getEmail(), resetToken);

            redirectAttributes.addFlashAttribute("message",
                    "Инструкции по восстановлению пароля отправлены на вашу почту.");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Forgot password error for email: {}", email, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке письма: " + e.getMessage());
            return "redirect:/forgot-password";
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
            logger.error("Reset password token validation error: {}", token, e);
            model.addAttribute("error", "Ошибка при проверке токена: " + e.getMessage());
            return "login";
        }
    }

    @PostMapping("/req/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Пароли не совпадают!");
                model.addAttribute("token", token);
                return "reset-password";
            }

            if (!jwtTokenUtils.validateToken(token)) {
                redirectAttributes.addFlashAttribute("error",
                        "Ссылка для сброса пароля недействительна или истекла!");
                return "redirect:/login";
            }

            String email = jwtTokenUtils.extractEmail(token);
            Player player = playerRepo.findByEmail(email).orElse(null);

            if (player == null) {
                redirectAttributes.addFlashAttribute("error", "Пользователь не найден!");
                return "redirect:/login";
            }

            player.setPassword(passwordEncoder.encode(password));
            player.setResetToken(null);
            playerRepo.save(player);

            logger.info("Password reset successfully for: {}", email);
            redirectAttributes.addFlashAttribute("message",
                    "Пароль успешно обновлен! Теперь вы можете войти с новым паролем.");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Password reset error for token: {}", token, e);
            model.addAttribute("error", "Ошибка при сбросе пароля: " + e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}