package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.nequi.franchise.application.usecase.AddProductUseCase;
import co.com.nequi.franchise.application.usecase.RemoveProductUseCase;
import co.com.nequi.franchise.application.usecase.UpdateProductStockUseCase;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.CreateProductRequest;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.ProductResponse;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.UpdateStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Tag(name = "Productos")
@RestController
@RequestMapping("/api/v1/franchises/{franchiseId}/branches/{branchId}/products")
class ProductController {

	private final AddProductUseCase addProduct;

	private final RemoveProductUseCase removeProduct;

	private final UpdateProductStockUseCase updateProductStock;

	ProductController(AddProductUseCase addProduct, RemoveProductUseCase removeProduct,
			UpdateProductStockUseCase updateProductStock) {
		this.addProduct = addProduct;
		this.removeProduct = removeProduct;
		this.updateProductStock = updateProductStock;
	}

	@Operation(summary = "Agregar un nuevo producto a una sucursal")
	@PostMapping
	Mono<ResponseEntity<ProductResponse>> create(@PathVariable String franchiseId, @PathVariable String branchId,
			@Valid @RequestBody CreateProductRequest request) {
		return addProduct.execute(franchiseId, branchId, request.name(), request.stock())
			.map(ProductResponse::from)
			.map(response -> ResponseEntity.created(URI.create("/api/v1/franchises/" + franchiseId + "/branches/"
					+ branchId + "/products/" + response.id())).body(response));
	}

	@Operation(summary = "Eliminar un producto de una sucursal")
	@DeleteMapping("/{productId}")
	Mono<ResponseEntity<Void>> delete(@PathVariable String franchiseId, @PathVariable String branchId,
			@PathVariable String productId) {
		return removeProduct.execute(franchiseId, branchId, productId)
			.thenReturn(ResponseEntity.noContent().build());
	}

	@Operation(summary = "Modificar el stock de un producto",
			description = "Reemplaza el stock por el valor indicado, por lo que la operacion es idempotente.")
	@PutMapping("/{productId}/stock")
	Mono<ProductResponse> updateStock(@PathVariable String franchiseId, @PathVariable String branchId,
			@PathVariable String productId, @Valid @RequestBody UpdateStockRequest request) {
		return updateProductStock.execute(franchiseId, branchId, productId, request.stock())
			.map(ProductResponse::from);
	}

}
