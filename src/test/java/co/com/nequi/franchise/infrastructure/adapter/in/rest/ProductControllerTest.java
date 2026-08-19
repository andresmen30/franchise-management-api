package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import co.com.nequi.franchise.application.usecase.AddProductUseCase;
import co.com.nequi.franchise.application.usecase.RemoveProductUseCase;
import co.com.nequi.franchise.application.usecase.RenameProductUseCase;
import co.com.nequi.franchise.application.usecase.UpdateProductStockUseCase;
import co.com.nequi.franchise.domain.exception.BranchNotFoundException;
import co.com.nequi.franchise.domain.exception.ProductNotFoundException;
import co.com.nequi.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = ProductController.class)
class ProductControllerTest {

	private static final String PRODUCTS = "/api/v1/franchises/franquicia-1/branches/sucursal-1/products";

	@Autowired
	private WebTestClient client;

	@MockitoBean
	private AddProductUseCase addProduct;

	@MockitoBean
	private RemoveProductUseCase removeProduct;

	@MockitoBean
	private UpdateProductStockUseCase updateProductStock;

	@MockitoBean
	private RenameProductUseCase renameProduct;

	@Test
	void agregaUnProductoYDevuelveLaUbicacion() {
		when(addProduct.execute("franquicia-1", "sucursal-1", "Cafe", 40))
			.thenReturn(Mono.just(new Product("producto-1", "Cafe", 40)));

		client.post()
			.uri(PRODUCTS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Cafe\",\"stock\":40}")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.valueEquals("Location", PRODUCTS + "/producto-1")
			.expectBody()
			.jsonPath("$.stock")
			.isEqualTo(40);
	}

	@Test
	void rechazaStockNegativoAlCrear() {
		client.post()
			.uri(PRODUCTS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Cafe\",\"stock\":-1}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errors[0].field")
			.isEqualTo("stock");
	}

	@Test
	void rechazaStockAusenteAlCrear() {
		client.post()
			.uri(PRODUCTS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Cafe\"}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errors[0].field")
			.isEqualTo("stock");
	}

	@Test
	void devuelve404CuandoLaSucursalNoExiste() {
		when(addProduct.execute(any(), any(), any(), anyInt()))
			.thenReturn(Mono.error(new BranchNotFoundException("sucursal-1")));

		client.post()
			.uri(PRODUCTS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Cafe\",\"stock\":1}")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("BRANCH_NOT_FOUND");
	}

	@Test
	void eliminaUnProductoSinCuerpoDeRespuesta() {
		when(removeProduct.execute("franquicia-1", "sucursal-1", "producto-1")).thenReturn(Mono.empty());

		client.delete().uri(PRODUCTS + "/producto-1").exchange().expectStatus().isNoContent().expectBody().isEmpty();
	}

	@Test
	void devuelve404AlEliminarUnProductoInexistente() {
		when(removeProduct.execute(any(), any(), any()))
			.thenReturn(Mono.error(new ProductNotFoundException("producto-9")));

		client.delete()
			.uri(PRODUCTS + "/producto-9")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("PRODUCT_NOT_FOUND");
	}

	@Test
	void actualizaElStockDeUnProducto() {
		when(updateProductStock.execute("franquicia-1", "sucursal-1", "producto-1", 99))
			.thenReturn(Mono.just(new Product("producto-1", "Cafe", 99)));

		client.put()
			.uri(PRODUCTS + "/producto-1/stock")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"stock\":99}")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.stock")
			.isEqualTo(99);
	}

	@Test
	void rechazaStockNegativoAlActualizar() {
		client.put()
			.uri(PRODUCTS + "/producto-1/stock")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"stock\":-5}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("VALIDATION_ERROR");
	}

	@Test
	void renombraElProductoConservandoElStock() {
		when(renameProduct.execute("franquicia-1", "sucursal-1", "producto-1", "Cafe premium"))
			.thenReturn(Mono.just(new Product("producto-1", "Cafe premium", 40)));

		client.put()
			.uri(PRODUCTS + "/producto-1/name")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Cafe premium\"}")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.name")
			.isEqualTo("Cafe premium")
			.jsonPath("$.stock")
			.isEqualTo(40);
	}

	@Test
	void devuelve404AlRenombrarUnProductoInexistente() {
		when(renameProduct.execute(any(), any(), any(), any()))
			.thenReturn(Mono.error(new ProductNotFoundException("otro")));

		client.put()
			.uri(PRODUCTS + "/otro/name")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Nuevo\"}")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("PRODUCT_NOT_FOUND");
	}

}
