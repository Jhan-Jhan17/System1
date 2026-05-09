package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Service.ImageStorageService;
import com.example.demo.model.Item;
import com.example.demo.repository.ItemRepository;

@Controller
public class FoundItController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ImageStorageService imageStorageService; // Module 3 injected!

    // 1. Show the Dashboard
    @GetMapping("/")
    public String viewDashboard(Model model) {
        model.addAttribute("items", itemRepository.findAll());
        // We send an empty Item object to the form so Thymeleaf knows what fields to expect
        model.addAttribute("newItem", new Item()); 
        return "index";
    }

    // 2. Catch the "Report Lost Item" form submission
    @PostMapping("/report")
    public String reportLostItem(@ModelAttribute Item newItem, @RequestParam("imageFile") MultipartFile file) {
        // Use our service to save the picture
        String filename = imageStorageService.saveImage(file);
        newItem.setImageFilename(filename); // Attach the filename to the OOP object
        
        // Save the item to the MySQL database
        itemRepository.save(newItem);
        
        // Refresh the page
        return "redirect:/"; 
    }
}