package com.example.system1.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.system1.Service.ImageStorageService;
import com.example.system1.model.ClaimRequest;
import com.example.system1.model.Item;
import com.example.system1.model.Message;
import com.example.system1.model.User; 
import com.example.system1.repository.ClaimRequestRepository;
import com.example.system1.repository.ItemRepository;
import com.example.system1.repository.MessageRepository;

@Controller
public class FoundItController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ClaimRequestRepository claimRepository;
    
    @Autowired
    private MessageRepository messageRepository;

    // --- UPDATED STUDENT DASHBOARD ROUTE ---
    @GetMapping("/student")
    public String viewDashboard(HttpSession session, Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        
        // 1. Secure the route
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/"; 
        }

        // 2. Pass the user to the HTML template to prevent the Error 500 crash
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("user", loggedInUser);

        // 3. Search logic
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("items", itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword));
        } else {
            model.addAttribute("items", itemRepository.findAll());
        }
        
        model.addAttribute("newItem", new Item());
        return "Index";
    }

    // 1. My Items (Claims & Tickets) Route
    @GetMapping("/my-items")
    public String viewMyItems(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/"; // Kick out if not logged in

        model.addAttribute("myClaims", claimRepository.findByStudentId(loggedInUser.getStudentId()));
        return "my-items";
    }

    // 2. My Profile Route
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/"; // Kick out if not logged in

        model.addAttribute("user", loggedInUser);
        return "profile";
    }

    @GetMapping("/contact")
    public String viewContactPage(Model model) {
        model.addAttribute("message", new Message());
        model.addAttribute("allMessages", messageRepository.findAll());
        model.addAttribute("myClaims", claimRepository.findAll());
        return "contact";
    }

    @PostMapping("/send-message")
    public String sendMessage(@ModelAttribute Message message) {
        messageRepository.save(message);
        return "redirect:/contact?success";
    }

    @GetMapping("/admin")
    public String viewAdminPanel(HttpSession session, Model model) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/";
        }
        try {
            model.addAttribute("pendingClaims", claimRepository.findAll());
            model.addAttribute("adminMessages", messageRepository.findAll());
            model.addAttribute("allItems", itemRepository.findAll()); 
        } catch (Exception e) {
            model.addAttribute("pendingClaims", new java.util.ArrayList<>());
            model.addAttribute("adminMessages", new java.util.ArrayList<>());
            model.addAttribute("allItems", new java.util.ArrayList<>());
        }
        return "admin";
    }

    @PostMapping("/admin/verify/{claimId}")
    public String verifyClaim(@PathVariable Long claimId, @RequestParam String action, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) return "redirect:/";

        ClaimRequest claim = claimRepository.findById(claimId).orElse(null);
        if (claim == null) return "redirect:/admin?error=notfound";

        if ("approve".equals(action)) {
            claim.setStatus("APPROVED");
            claimRepository.save(claim);
            return "redirect:/admin?qrgen=success";
            
        } else if ("reject".equals(action)) {
            Item item = claim.getItem();
            if (item != null) {
                item.setStatus("AVAILABLE");
                itemRepository.save(item);
            }
            claimRepository.delete(claim);
            return "redirect:/admin?rejected=success";
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/admin/finalize/{claimId}")
    public String finalizeClaim(@PathVariable Long claimId, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) return "redirect:/";

        ClaimRequest claim = claimRepository.findById(claimId).orElse(null);
        if (claim == null) return "redirect:/admin?error=notfound";

        Item item = claim.getItem();

        claim.setStatus("RETURNED");
        if (item != null) {
            item.setStatus("RETURNED");
            itemRepository.save(item);
        }
        claimRepository.save(claim);
        
        return "redirect:/admin?released=success";
    }

    @PostMapping("/admin/item/delete/{itemId}")
    public String deleteItem(@PathVariable Long itemId, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) return "redirect:/";

        Item item = itemRepository.findById(itemId).orElse(null);
        if (item != null) {
            for (ClaimRequest claim : claimRepository.findAll()) {
                if (claim.getItem() != null && claim.getItem().getId().equals(itemId)) {
                    claimRepository.delete(claim);
                }
            }
            itemRepository.delete(item);
        }
        
        return "redirect:/admin?deleted=success";
    }

    @PostMapping("/report")
    public String reportLostItem(@ModelAttribute Item newItem, @RequestParam("imageFile") MultipartFile file) {
        String filename = imageStorageService.saveImage(file);
        newItem.setImageFilename(filename);
        itemRepository.save(newItem);
        return "redirect:/student";
    }

    @PostMapping("/claim/{itemId}")
    public String submitClaim(@PathVariable Long itemId,
                            @RequestParam String ownershipDetails,
                            @RequestParam String studentId,
                            @RequestParam(value = "proofFile", required = false) MultipartFile proofFile) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) return "redirect:/student?error=notfound";
        
        ClaimRequest claim = new ClaimRequest();
        claim.setItem(item);
        claim.setOwnershipDetails(ownershipDetails);
        claim.setStudentId(studentId);
        claim.setStatus("PENDING");
        claim.setClaimToken("QR-" + System.currentTimeMillis() + "-" + studentId);

        if (proofFile != null && !proofFile.isEmpty()) {
            String filename = imageStorageService.saveImage(proofFile);
            claim.setProofImage(filename);
        }

        claimRepository.save(claim);
        item.setStatus("CLAIMING");
        itemRepository.save(item);
        
        return "redirect:/student";
    }

    @PostMapping("/admin/reply/{messageId}")
    public String replyToMessage(@PathVariable Long messageId, @RequestParam String adminReply, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) return "redirect:/";

        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setAdminReply(adminReply);
            messageRepository.save(message);
        }

        return "redirect:/admin";
    }
}