package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.nequi.franchise.application.usecase.CreateFranchiseUseCase;
import co.com.nequi.franchise.application.usecase.GetTopStockProductsUseCase;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.CreateFranchiseRequest;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.FranchiseResponse;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.TopStockProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Franquicias")
@RestController
@RequestMapping("/api/v1/franchises")
class FranchiseController {

	private final CreateFranchiseUseCase createFranchise;

	private final GetTopStockProductsUseCase getTopStockProducts;

	FranchiseController(CreateFranchiseUseCase createFranchise, GetTopStockProductsUseCase getTopStockProducts) {
		this.createFranchise = createFranchise;
		this.getTopStockProducts = getTopStockProducts;
	}

	@Operation(summary = "Agregar una nueva franquicia")
	@PostMapping
	Mono<ResponseEntity<FranchiseResponse>> create(@Valid @RequestBody CreateFranchiseRequest request) {
		return createFranchise.execute(request.name())
			.map(FranchiseResponse::from)
			.map(response -> ResponseEntity.created(URI.create("/api/v1/franchises/" + response.id()))
				.body(response));
	}

	@Operation(summary = "Producto con mayor stock de cada sucursal de una franquicia",
			description = "Las sucursales sin productos se omiten del listado.")
	@GetMapping("/{franchiseId}/branches/top-stock-products")
	Flux<TopStockProductResponse> topStockProducts(@PathVariable String franchiseId) {
		return getTopStockProducts.execute(franchiseId).map(TopStockProductResponse::from);
	}

}
