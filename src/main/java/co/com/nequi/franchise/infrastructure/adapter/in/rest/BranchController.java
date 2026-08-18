package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.nequi.franchise.application.usecase.AddBranchUseCase;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.BranchResponse;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.CreateBranchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Tag(name = "Sucursales")
@RestController
@RequestMapping("/api/v1/franchises/{franchiseId}/branches")
class BranchController {

	private final AddBranchUseCase addBranch;

	BranchController(AddBranchUseCase addBranch) {
		this.addBranch = addBranch;
	}

	@Operation(summary = "Agregar una nueva sucursal a una franquicia")
	@PostMapping
	Mono<ResponseEntity<BranchResponse>> create(@PathVariable String franchiseId,
			@Valid @RequestBody CreateBranchRequest request) {
		return addBranch.execute(franchiseId, request.name())
			.map(BranchResponse::from)
			.map(response -> ResponseEntity
				.created(URI.create("/api/v1/franchises/" + franchiseId + "/branches/" + response.id()))
				.body(response));
	}

}
