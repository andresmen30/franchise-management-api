package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import co.com.nequi.franchise.application.usecase.AddBranchUseCase;
import co.com.nequi.franchise.application.usecase.RenameBranchUseCase;
import co.com.nequi.franchise.domain.exception.BranchNotFoundException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = BranchController.class)
class BranchControllerTest {

	@Autowired
	private WebTestClient client;

	@MockitoBean
	private AddBranchUseCase addBranch;

	@MockitoBean
	private RenameBranchUseCase renameBranch;

	@Test
	void agregaUnaSucursalYDevuelveLaUbicacion() {
		when(addBranch.execute("franquicia-1", "Centro"))
			.thenReturn(Mono.just(new Branch("sucursal-1", "Centro", List.of())));

		client.post()
			.uri("/api/v1/franchises/franquicia-1/branches")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Centro\"}")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.valueEquals("Location", "/api/v1/franchises/franquicia-1/branches/sucursal-1")
			.expectBody()
			.jsonPath("$.name")
			.isEqualTo("Centro");
	}

	@Test
	void devuelve404CuandoLaFranquiciaNoExiste() {
		when(addBranch.execute(any(), any())).thenReturn(Mono.error(new FranchiseNotFoundException("otra")));

		client.post()
			.uri("/api/v1/franchises/otra/branches")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Centro\"}")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("FRANCHISE_NOT_FOUND");
	}

	@Test
	void rechazaUnNombreDeSucursalVacio() {
		client.post()
			.uri("/api/v1/franchises/franquicia-1/branches")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"\"}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.errors[0].field")
			.isEqualTo("name");
	}

	@Test
	void renombraLaSucursal() {
		when(renameBranch.execute("franquicia-1", "sucursal-1", "Centro Mayor"))
			.thenReturn(Mono.just(new Branch("sucursal-1", "Centro Mayor", List.of())));

		client.put()
			.uri("/api/v1/franchises/franquicia-1/branches/sucursal-1/name")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Centro Mayor\"}")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.name")
			.isEqualTo("Centro Mayor");
	}

	@Test
	void devuelve404AlRenombrarUnaSucursalInexistente() {
		when(renameBranch.execute(any(), any(), any()))
			.thenReturn(Mono.error(new BranchNotFoundException("otra")));

		client.put()
			.uri("/api/v1/franchises/franquicia-1/branches/otra/name")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"Nuevo\"}")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("BRANCH_NOT_FOUND");
	}

}
