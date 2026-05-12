package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }
    
    // Handles Successful Google Login
    @GetMapping("/google-success")
    public String handleGoogleLogin(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
        String email = principal.getAttribute("email");
        String fullName = principal.getAttribute("name");

        // 1. Check if the user already exists (Manual or Google)
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            // No account found at all. Create a new one.
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setStudentId("G-" + System.currentTimeMillis());
            user.setPassword("OAUTH_USER");
            userRepository.save(user);
        } else {
            // 2. ACCOUNT LINKING: If they registered manually before,
            // the 'user' object now contains their real Student ID!
            // We don't need to create a new one; we just log them in.
            System.out.println("Linking Google Login to existing account for: " + email);
        }

        // 3. Log them into the session using the FOUND user (the one with the real ID)
        session.setAttribute("loggedInUser", user);
        session.setAttribute("isStudent", true);
        
        if ("admin@batstate-u.edu.ph".equals(email)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/admin";
        }
        
        return "redirect:/student";
    }

    // Handles Admin AND Student Login
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        
        // Admin hardcoded check
        if ("admin@batstate-u.edu.ph".equals(email) && "password123".equals(password)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/admin";
        }
        
        // Student Database Check
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user);
            session.setAttribute("isStudent", true);
            return "redirect:/student";
        }
        
        model.addAttribute("error", "Invalid email or password.");
        return "login";
    }

    // Handles New Student Registration
    @PostMapping("/register")
    public String handleRegistration(@RequestParam String fullName,
                                    @RequestParam String studentId,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    Model model) {
        
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("regError", "Email already registered!");
            return "login";
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setStudentId(studentId);
        newUser.setEmail(email);
        newUser.setPassword(password); // Note: We will hash this in Phase 2
        userRepository.save(newUser);

        model.addAttribute("regSuccess", "Account created! You can now log in.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}