package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // 1. Show the Landing Page at the very start
    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    // 2. Handle Admin Login Attempt
    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        
        // Hardcoded admin credentials for the project
        if ("admin".equals(username) && "password123".equals(password)) {
            session.setAttribute("isAdmin", true); // Create a secure session
            return "redirect:/admin";
        }
        
        // If wrong credentials, reload login page with error
        model.addAttribute("error", "Invalid username or password.");
        return "login";
    }

    // 3. Handle Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destroy the session
        return "redirect:/"; // Send back to landing page
    }
}