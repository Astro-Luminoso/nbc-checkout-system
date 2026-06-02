package dev.nbcsparta.assignment.nbccheckoutsystem.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.exception.ProductNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	private ProductService productService;

	@BeforeEach
	void setUp() {
		productService = new ProductService(productRepository);
	}

	@Test
	void getProductsReturnsAllProducts() throws Exception {
		when(productRepository.findAll()).thenReturn(List.of(
			product(
				1L,
				"무선 마우스",
				"저소음 무선 마우스",
				25000,
				100,
				"DIGITAL",
				"ON_SALE"
			),
			product(
				2L,
				"기계식 키보드",
				"갈축 기계식 키보드",
				89000,
				30,
				"DIGITAL",
				"ON_SALE"
			)
		));

		List<ProductResponse> responses = productService.getProducts();

		assertThat(responses).hasSize(2);
		assertThat(responses)
			.extracting(ProductResponse::name)
			.containsExactlyInAnyOrder("무선 마우스", "기계식 키보드");
		assertThat(responses)
			.filteredOn(product -> product.name().equals("무선 마우스"))
			.singleElement()
			.satisfies(product -> {
				assertThat(product.description()).isEqualTo("저소음 무선 마우스");
				assertThat(product.price()).isEqualTo(25000);
				assertThat(product.stockQuantity()).isEqualTo(100);
				assertThat(product.category()).isEqualTo("DIGITAL");
				assertThat(product.salePrice()).isEqualTo("ON_SALE");
			});
	}

	@Test
	void getProductReturnsProductDetail() throws Exception {
		Product product = product(
			1L,
			"무선 마우스",
			"저소음 무선 마우스",
			25000,
			100,
			"DIGITAL",
			"ON_SALE"
		);
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		ProductResponse response = productService.getProduct(1L);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("무선 마우스");
		assertThat(response.description()).isEqualTo("저소음 무선 마우스");
		assertThat(response.price()).isEqualTo(25000);
		assertThat(response.stockQuantity()).isEqualTo(100);
		assertThat(response.category()).isEqualTo("DIGITAL");
		assertThat(response.salePrice()).isEqualTo("ON_SALE");
	}

	@Test
	void getProductThrowsNotFoundWhenProductDoesNotExist() {
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getProduct(999L))
			.isInstanceOf(ProductNotFoundException.class)
			.hasMessage("상품을 찾을 수 없습니다.");
	}

	private Product product(
		Long id,
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
		ReflectionTestUtils.setField(product, "id", id);
		ReflectionTestUtils.setField(product, "name", name);
		ReflectionTestUtils.setField(product, "description", description);
		ReflectionTestUtils.setField(product, "price", price);
		ReflectionTestUtils.setField(product, "stockQuantity", stockQuantity);
		ReflectionTestUtils.setField(product, "category", category);
		ReflectionTestUtils.setField(product, "salePrice", salePrice);
		ReflectionTestUtils.setField(product, "saleStatus", SaleStatus.ON_SALE);
		ReflectionTestUtils.setField(product, "createdDate", LocalDateTime.now());
		ReflectionTestUtils.setField(product, "updatedDate", LocalDateTime.now());
		return product;
	}
}
