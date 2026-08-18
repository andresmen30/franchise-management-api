package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

class FranchiseLookup {

	private final FranchiseRepository repository;

	FranchiseLookup(FranchiseRepository repository) {
		this.repository = repository;
	}

	Mono<Franchise> requireFranchise(String franchiseId) {
		return repository.findById(franchiseId)
				.switchIfEmpty(Mono.error(() -> new FranchiseNotFoundException(franchiseId)));
	}

	Mono<Branch> requireBranch(String franchiseId, String branchId) {
		return requireFranchise(franchiseId).map(franchise -> franchise.requireBranch(branchId));
	}

	Mono<Product> requireProduct(String franchiseId, String branchId, String productId) {
		return requireBranch(franchiseId, branchId).map(branch -> branch.requireProduct(productId));
	}

}
