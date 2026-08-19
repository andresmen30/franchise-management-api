package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class RenameProductUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public RenameProductUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Product> execute(String franchiseId, String branchId, String productId, String newName) {
		return lookup.requireProduct(franchiseId, branchId, productId)
			.map(product -> product.withName(newName))
			.flatMap(product -> repository.saveProduct(franchiseId, branchId, product));
	}

}
