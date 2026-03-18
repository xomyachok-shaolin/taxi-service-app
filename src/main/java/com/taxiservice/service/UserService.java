package com.taxiservice.service;

import com.taxiservice.model.Role;
import com.taxiservice.model.User;
import com.taxiservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления пользователями.
 * Реализует {@link UserDetailsService} для интеграции со Spring Security.
 * Обеспечивает регистрацию, поиск и управление учётными записями.
 *
 * @version 1.0
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Загрузка пользователя по логину для Spring Security.
     *
     * @param username логин пользователя
     * @return объект UserDetails для аутентификации
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    /**
     * Регистрация нового пользователя.
     * Пароль хешируется перед сохранением в БД.
     *
     * @param user новый пользователь
     * @return сохранённый пользователь
     * @throws IllegalArgumentException если логин уже занят
     */
    @Transactional
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Пользователь с логином '"
                    + user.getUsername() + "' уже существует");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Поиск пользователя по логину.
     *
     * @param username логин
     * @return Optional с пользователем
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Поиск пользователя по ID.
     *
     * @param id идентификатор
     * @return Optional с пользователем
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Получение всех пользователей.
     *
     * @return список пользователей
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Получение пользователей по роли.
     *
     * @param role роль
     * @return список пользователей
     */
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Поиск пользователей по имени.
     *
     * @param name часть имени
     * @return список найденных пользователей
     */
    public List<User> searchByName(String name) {
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }

    /**
     * Обновление данных пользователя.
     *
     * @param user обновлённый пользователь
     * @return сохранённый пользователь
     */
    @Transactional
    public User update(User user) {
        return userRepository.save(user);
    }

    /**
     * Изменение роли пользователя (доступно только администратору).
     *
     * @param userId  ID пользователя
     * @param newRole новая роль
     * @return обновлённый пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    @Transactional
    public User changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Пользователь с ID " + userId + " не найден"));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Удаление пользователя по ID.
     *
     * @param id идентификатор пользователя
     */
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Подсчёт общего количества пользователей.
     *
     * @return количество пользователей
     */
    public long count() {
        return userRepository.count();
    }

    /**
     * Подсчёт пользователей по роли.
     *
     * @param role роль
     * @return количество пользователей
     */
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
