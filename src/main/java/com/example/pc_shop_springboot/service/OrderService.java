package com.example.pc_shop_springboot.service;

import com.example.pc_shop_springboot.entity.Inventory;
import com.example.pc_shop_springboot.entity.OrderItem;
import com.example.pc_shop_springboot.entity.Orders;
import com.example.pc_shop_springboot.entity.Product;
import com.example.pc_shop_springboot.repository.InventoryRepository;
import com.example.pc_shop_springboot.repository.OrderRepository;
import com.example.pc_shop_springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public Orders placeOrder(Integer customerId, Integer productId, Integer quantityToBuy) {

        // 1. Tìm thông tin Sản phẩm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        // 2. Tìm thông tin Tồn kho
        Inventory inventory = inventoryRepository.findAll().stream()
                .filter(inv -> inv.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Inventory record not found!"));

        // 3. Kiểm tra xem còn đủ hàng không?
        if (inventory.getQuantityOnHand() < quantityToBuy) {
            throw new RuntimeException("Not enough stock available!");
        }

        // 4. Trừ kho
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantityToBuy);
        inventoryRepository.save(inventory);

        // 5. Tạo Giỏ hàng (Orders)
        Orders newOrder = new Orders();
        newOrder.setCustomerId(customerId);
        newOrder.setStatus("COMPLETED");
        newOrder.setOrderDate(LocalDateTime.now());
        BigDecimal total = product.getCurrentDynamicPrice().multiply(new BigDecimal(quantityToBuy));
        newOrder.setTotalAmount(total);

        // 6. Tạo Chi tiết Đơn hàng (OrderItem)
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantityToBuy);
        item.setUnitPrice(product.getCurrentDynamicPrice());
        // Thêm giá trị discount mặc định là 0 để tránh lỗi Null
        item.setDiscount(BigDecimal.ZERO);

        // 7. Bỏ Chi tiết vào Giỏ và Lưu xuống DB
        newOrder.addOrderItem(item);

        return orderRepository.save(newOrder);
    }
}