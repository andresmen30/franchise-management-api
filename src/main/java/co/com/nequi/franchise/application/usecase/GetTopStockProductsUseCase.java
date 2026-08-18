package co.com.nequi.franchise.application.usecase;

import java.util.List;
import java.util.stream.Stream;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.BranchTopProduct;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Flux;

public class GetTopStockProductsUseCase {

	private final FranchiseLookup lookup;

	public GetTopStockProductsUseCase(FranchiseRepository repository) {
		this.lookup = new FranchiseLookup(repository);
	}

	public Flux<BranchTopProduct> execute(String franchiseId) {
		return lookup.requireFranchise(franchiseId).flatMapIterable(GetTopStockProductsUseCase::topProductPerBranch);
	}

	private static List<BranchTopProduct> topProductPerBranch(Franchise franchise) {
		return franchise.branches().stream().flatMap(GetTopStockProductsUseCase::topProduct).toList();
	}

	private static Stream<BranchTopProduct> topProduct(Branch branch) {
		return branch.productWithHighestStock().stream().map(product -> BranchTopProduct.of(branch, product));
	}

}
