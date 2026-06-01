package dev.nbcsparta.assignment.nbccheckoutsystem.product.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void getProductsReturnsAllProducts() throws Exception {
        productRepository.save(product(
                "무선 마우스",
                "저소음 무선 마우스",
                25000,
                100,
                "DIGITAL",
                "ON_SALE"
        ));
        productRepository.save(product(
                "기계식 키보드",
                "갈축 기계식 키보드",
                89000,
                30,
                "DIGITAL",
                "ON_SALE"
        ));

        List<ProductResponse> responses = productService.getProducts();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(ProductResponse::name)
                .containsExactlyInAnyOrder("무선 마우스", "기계식 키보드");
        assertThat(responses)
                .filteredOn(response -> response.name().equals("무선 마우스"))
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.description()).isEqualTo("저소음 무선 마우스");
                    assertThat(response.price()).isEqualTo(25000);
                    assertThat(response.stockQuantity()).isEqualTo(100);
                    assertThat(response.category()).isEqualTo("DIGITAL");
                    assertThat(response.salePrice()).isEqualTo("ON_SALE");
                });
    }

    private Product product(
            String name,
            String description,
            Integer price,
            Integer stockQuantity,
            String category,
            String salePrice
    ) throws Exception {
        Constructor<Product> constructor = Product.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Product product = constructor.newInstance();
        ReflectionTestUtils.setField(product, "name", name);
        ReflectionTestUtils.setField(product, "description", description);
        ReflectionTestUtils.setField(product, "price", price);
        ReflectionTestUtils.setField(product, "stock_quantity", stockQuantity);
        ReflectionTestUtils.setField(product, "category", category);
        ReflectionTestUtils.setField(product, "sale_price", salePrice);
        ReflectionTestUtils.setField(product, "createdDate", LocalDateTime.now());
        ReflectionTestUtils.setField(product, "updatedDate", LocalDateTime.now());
        return product;
    }
}
