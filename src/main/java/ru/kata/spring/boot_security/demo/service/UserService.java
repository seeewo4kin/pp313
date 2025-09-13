package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Загрузка пользователя для Spring Security
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Получить всех пользователей
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Получить пользователя по ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Найти пользователя по username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Найти пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Сохранить пользователя (создание или обновление)
    @Transactional
    public User saveUser(User user) {
        // Если это новый пользователь или пароль был изменен - хешируем пароль
        if (user.getId() == null || !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Если роли не установлены, устанавливаем роль USER по умолчанию
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            user.setRoles(Set.of(userRole));
        }

        return userRepository.save(user);
    }

    // Обновить пользователя
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        // Обновляем поля
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());

        // Если пароль не пустой и не захеширован - обновляем
        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty() &&
                !updatedUser.getPassword().startsWith("$2a$")) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Обновляем роли
        if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
            existingUser.setRoles(updatedUser.getRoles());
        }

        return userRepository.save(existingUser);
    }

    // Удалить пользователя
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Получить все роли
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Найти роль по имени
    public Optional<Role> findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    // Создать новую роль
    @Transactional
    public Role createRole(String name) {
        Role role = new Role(name);
        return roleRepository.save(role);
    }

    // Проверить, существует ли пользователь с таким username
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Проверить, существует ли пользователь с таким email
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}