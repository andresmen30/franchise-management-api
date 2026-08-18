package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class UpdateProductStockUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public UpdateProductStockUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Product> execute(String franchiseId, String branchId, String productId, int stock) {
		return lookup.requireProduct(franchiseId, branchId, productId)
				.map(product -> product.withStock(stock))
				.flatMap(product -> repository.saveProduct(franchiseId, branchId, product));
	}

}
