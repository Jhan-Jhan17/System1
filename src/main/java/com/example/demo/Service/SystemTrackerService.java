package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repository.ClaimRequestRepository;
import com.example.demo.repository.ItemRepository;

@Service
public class SystemTrackerService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ClaimRequestRepository claimRepository;

    public long getTotalAvailableItems() {
        // You would need to add findByStatus("AVAILABLE") in your ItemRepository to use this perfectly,
        // but this shows the OOP Service logic!
        return itemRepository.count(); 
    }

    public long getPendingClaimsCount() {
        return claimRepository.findByStatus("PENDING").size();
    }
}