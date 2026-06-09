package dev.nbcsparta.assignment.nbccheckoutsystem;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest
class NbcCheckoutSystemApplicationTests {

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @Test
    void contextLoads() {
    }

}
