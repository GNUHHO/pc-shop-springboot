package com.example.pc_shop_springboot;

import com.example.pc_shop_springboot.repository.CategoryRepository;
import com.example.pc_shop_springboot.repository.InventoryRepository;
import com.example.pc_shop_springboot.repository.OrderItemRepository;
import com.example.pc_shop_springboot.repository.OrderRepository;
import com.example.pc_shop_springboot.repository.ProductRepository;
import com.example.pc_shop_springboot.repository.ProductPriceHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
				"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
				"org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
})
class PcShopSpringbootApplicationTests {

	@MockitoBean
	private ProductRepository productRepository;

	@MockitoBean
	private ProductPriceHistoryRepository productPriceHistoryRepository;

	@MockitoBean
	private CategoryRepository categoryRepository;

	@MockitoBean
	private InventoryRepository inventoryRepository;

	@MockitoBean
	private OrderRepository orderRepository;

	@MockitoBean
	private OrderItemRepository orderItemRepository;

	@Test
	void contextLoads() {
	}

}
