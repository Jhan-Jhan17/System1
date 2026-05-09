package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Service.ImageStorageService;
import com.example.demo.model.ClaimRequest;
import com.example.demo.model.Item;
import com.example.demo.model.Message;
import com.example.demo.repository.ClaimRequestRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.MessageRepository;

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

    @GetMapping("/student")
    public String viewDashboard(Model model, @RequestParam(name = "keyword", required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("items", itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword));
        } else {
            model.addAttribute("items", itemRepository.findAll());
        }
        model.addAttribute("newItem", new Item()); 
        return "index";
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
        } catch (Exception e) {
            model.addAttribute("pendingClaims", new java.util.ArrayList<>());
            model.addAttribute("adminMessages", new java.util.ArrayList<>());
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
            // NEW REJECT LOGIC: Set item back to available, delete claim record.
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

    @PostMapping("/report")
    public String reportLostItem(@ModelAttribute Item newItem, @RequestParam("imageFile") MultipartFile file) {
        String filename = imageStorageService.saveImage(file);
        newItem.setImageFilename(filename); 
        itemRepository.save(newItem);
        return "redirect:/student"; 
    }

    // UPDATED: Added MultipartFile logic for the Image Proof
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

        // Process the Image Upload if the student provided one
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