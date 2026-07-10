package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.entity.Inventory;
import com.example.pc_shop_springboot.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
}
