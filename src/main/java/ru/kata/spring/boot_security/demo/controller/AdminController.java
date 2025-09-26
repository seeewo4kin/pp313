package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String showAdminPage(Principal principal, Model model) {
        User authUser = userService.findUserByUsername(principal.getName());
        model.addAttribute("authUser", authUser);
        model.addAttribute("users", userService.findAll());
        return "admin";
    }

    @GetMapping("/new")
    public String showNewUserForm(Principal principal, Model model) {
        User authUser = userService.findUserByUsername(principal.getName());
        model.addAttribute("authUser", authUser);
        model.addAttribute("user", new User()); // Add empty user object for the form
        model.addAttribute("allRoles", roleService.getRoles()); // Add roles for selection
        return "new-user"; // Return the template that contains your form
    }

    @PostMapping("/new")
    public String createUser(@ModelAttribute("user") User user,
                             @RequestParam("roles") List<Long> roles) {
        // Set roles based on selected role IDs
        user.setRoles(roleService.getRoles().stream().collect(Collectors.toSet()));
        userService.save(user);
        return "redirect:/admin";
    }
}