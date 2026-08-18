package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class AddProductUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public AddProductUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Product> execute(String franchiseId, String branchId, String productName, int stock) {
		return lookup.requireBranch(franchiseId, branchId)
				.then(Mono.fromCallable(() -> Product.create(productName, stock)))
				.flatMap(product -> repository.saveProduct(franchiseId, branchId, product));
	}

}
