package com.taxiservice.config;

import com.taxiservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация Spring Security.
 * Определяет правила доступа к ресурсам на основе ролей пользователей,
 * настройки аутентификации и шифрования паролей.
 *
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Бин для шифрования паролей с использованием BCrypt.
     * Обеспечивает безопасное хранение паролей в базе данных.
     *
     * @return экземпляр BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Бин менеджера аутентификации.
     *
     * @param authConfig конфигурация аутентификации
     * @return экземпляр AuthenticationManager
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Конфигурация цепочки фильтров безопасности.
     * Определяет правила доступа:
     * <ul>
     *   <li>Публичные ресурсы: страницы входа, регистрации, CSS, JS</li>
     *   <li>Клиент: /client/**</li>
     *   <li>Водитель: /driver/**</li>
     *   <li>Диспетчер: /dispatcher/**</li>
     *   <li>Администратор: /admin/**</li>
     * </ul>
     *
     * @param http объект конфигурации HTTP-безопасности
     * @return настроенная цепочка фильтров
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**",
                        "/h2-console/**", "/about", "/error/**").permitAll()
                .requestMatchers("/client/**").hasRole("CLIENT")
                .requestMatchers("/driver/**").hasRole("DRIVER")
                .requestMatchers("/dispatcher/**").hasRole("DISPATCHER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/statistics/**").hasAnyRole("DISPATCHER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            // Разрешить H2 Console (только для разработки)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(
                    frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
