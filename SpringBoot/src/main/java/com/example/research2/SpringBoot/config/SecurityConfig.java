package com.example.research2.SpringBoot.config;

import com.example.research2.SpringBoot.repositories.PlayerRepo;
import com.example.research2.SpringBoot.security.PlayerDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration // Помечает этот класс как конфигурационный (для Spring)
@EnableWebSecurity // Включает поддержку Spring Security в приложении
public class SecurityConfig {

    // Подключаем наш репозиторий пользователей (чтобы искать пользователей в базе по email)
    private final PlayerRepo playerRepo;

    // Внедряем репозиторий через конструктор
    public SecurityConfig(PlayerRepo playerRepo) {
        this.playerRepo = playerRepo;
    }

    /**
     * Главный метод настройки безопасности.
     * Здесь мы указываем:
     * - какие страницы доступны всем;
     * - как обрабатывается логин и логаут;
     * - куда перенаправлять при успехе или ошибке;
     * - отключаем CSRF для упрощения (на начальном этапе).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Настройка авторизации запросов
                .authorizeHttpRequests((authorize) -> authorize
                        // Разрешаем доступ всем пользователям к этим endpoint'ам
                        .requestMatchers("/login", "/register").permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                // Настройка формы входа
                .formLogin(form -> form
                        // Указываем кастомную страницу входа
                        .loginPage("/login")
                        // URL для перенаправления после успешного входа
                        .defaultSuccessUrl("/", true)
                        // URL для перенаправления при ошибке входа
                        .failureUrl("/login?error=true")
                        // Разрешаем доступ к форме входа всем
                        .permitAll()
                )
                // Настройка выхода из системы
                .logout(logout -> logout
                        // URL для перенаправления после успешного выхода
                        .logoutSuccessUrl("/login?logout=true")
                        // Разрешаем доступ к logout всем
                        .permitAll()
                )
                // Отключаем CSRF защиту (обычно для API или если используется другая защита)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Настройка кодировщика паролей.
     * BCrypt — безопасный способ хранения паролей, он хеширует пароль перед сохранением.
     * Обязательно используется при регистрации и проверке пароля на входе.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Этот метод говорит Spring Security, как именно искать пользователя по логину/email.
     * Когда пользователь вводит логин, Spring вызывает этот метод.
     * - Мы ищем пользователя по name в базе (playerRepo.findByName).
     * - Если находим — оборачиваем в объект PlayerDetails (реализует UserDetails).
     * - Если не находим — выбрасываем исключение, чтобы Spring показал ошибку логина.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public PlayerDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                var player = playerRepo.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                return new PlayerDetails(player);
            }
        };
    }

}


