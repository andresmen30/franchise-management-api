package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class RemoveProductUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public RemoveProductUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Void> execute(String franchiseId, String branchId, String productId) {
		return lookup.requireProduct(franchiseId, branchId, productId)
				.flatMap(product -> repository.deleteProduct(franchiseId, branchId, product.id()));
	}

}
