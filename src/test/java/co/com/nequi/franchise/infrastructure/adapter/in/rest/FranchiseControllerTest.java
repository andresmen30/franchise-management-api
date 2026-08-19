package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import co.com.nequi.franchise.application.usecase.CreateFranchiseUseCase;
import co.com.nequi.franchise.application.usecase.GetTopStockProductsUseCase;
import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.exception.RepositoryUnavailableException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.BranchTopProduct;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = FranchiseController.class)
class FranchiseControllerTest {

	@Autowired
	private WebTestClient client;

	@MockitoBean
	private CreateFranchiseUseCase createFranchise;

	@MockitoBean
	private GetTopStockProductsUseCase getTopStockProducts;

	@Test
	void creaUnaFranquiciaYDevuelveLaUbicacion() {
		Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of());
		when(createFranchise.execute("Nequi Store")).thenReturn(Mono.just(franchise));

		client.post()
			.uri("/api/v1/franchises")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Nequi Store\"}")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.valueEquals("Location", "/api/v1/franchises/franquicia-1")
			.expectBody()
			.jsonPath("$.id")
			.isEqualTo("franquicia-1")
			.jsonPath("$.name")
			.isEqualTo("Nequi Store");
	}

	@Test
	void rechazaUnNombreVacioConDetalleDeCampos() {
		client.post()
			.uri("/api/v1/franchises")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"  \"}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("VALIDATION_ERROR")
			.jsonPath("$.errors[0].field")
			.isEqualTo("name");
	}

	@Test
	void traduceUnaViolacionDeInvarianteDelDominioA400() {
		when(createFranchise.execute(any()))
			.thenReturn(Mono.error(new DomainValidationException("El campo nombre de la franquicia es obligatorio")));

		client.post()
			.uri("/api/v1/franchises")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"valido\"}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("VALIDATION_ERROR");
	}

	@Test
	void devuelveUnProblemCoherenteCuandoElCuerpoEstaMalFormado() {
		client.post()
			.uri("/api/v1/franchises")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("BAD_REQUEST")
			.jsonPath("$.instance")
			.isEqualTo("/api/v1/franchises");
	}

	@Test
	void traduceUnFalloDelAlmacenamientoA503SinFiltrarLaCausa() {
		when(createFranchise.execute(any())).thenReturn(Mono.error(new RepositoryUnavailableException(
				"No fue posible acceder al almacenamiento", new IllegalStateException("DynamoDbException: timeout"))));

		client.post()
			.uri("/api/v1/franchises")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Nequi Store\"}")
			.exchange()
			.expectStatus()
			.isEqualTo(503)
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
			.expectBody(String.class)
			.value(body -> {
				assertThat(body).contains("REPOSITORY_UNAVAILABLE");
				assertThat(body).doesNotContain("DynamoDbException", "IllegalStateException", "timeout");
			});
	}

	@Test
	void devuelveElProductoDeMayorStockPorSucursal() {
		Branch centro = new Branch("sucursal-1", "Centro", List.of());
		Product te = new Product("producto-1", "Te", 90);
		when(getTopStockProducts.execute("franquicia-1"))
			.thenReturn(Flux.just(BranchTopProduct.of(centro, te)));

		client.get()
			.uri("/api/v1/franchises/franquicia-1/branches/top-stock-products")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$[0].branchId")
			.isEqualTo("sucursal-1")
			.jsonPath("$[0].branchName")
			.isEqualTo("Centro")
			.jsonPath("$[0].productName")
			.isEqualTo("Te")
			.jsonPath("$[0].stock")
			.isEqualTo(90);
	}

	@Test
	void devuelve404CuandoLaFranquiciaNoExiste() {
		when(getTopStockProducts.execute(eq("otra")))
			.thenReturn(Flux.error(new FranchiseNotFoundException("otra")));

		client.get()
			.uri("/api/v1/franchises/otra/branches/top-stock-products")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("FRANCHISE_NOT_FOUND")
			.jsonPath("$.instance")
			.isEqualTo("/api/v1/franchises/otra/branches/top-stock-products");
	}

}
