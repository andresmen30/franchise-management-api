package co.com.nequi.franchise.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

class InMemoryFranchiseRepository implements FranchiseRepository {

	private final Map<String, Franchise> franchises = new ConcurrentHashMap<>();

	void seed(Franchise franchise) {
		franchises.put(franchise.id(), franchise);
	}

	@Override
	public Mono<Franchise> findById(String franchiseId) {
		return Mono.fromCallable(() -> franchises.get(franchiseId));
	}

	@Override
	public Mono<Franchise> saveFranchise(Franchise franchise) {
		return Mono.fromCallable(() -> {
			franchises.put(franchise.id(), franchise);
			return franchise;
		});
	}

	@Override
	public Mono<Branch> saveBranch(String franchiseId, Branch branch) {
		return Mono.fromCallable(() -> {
			Franchise franchise = stored(franchiseId);
			List<Branch> branches = upsert(franchise.branches(), branch, Branch::id);
			franchises.put(franchiseId, new Franchise(franchise.id(), franchise.name(), branches));
			return branch;
		});
	}

	@Override
	public Mono<Product> saveProduct(String franchiseId, String branchId, Product product) {
		return Mono.fromCallable(() -> {
			Branch branch = stored(franchiseId).requireBranch(branchId);
			List<Product> products = upsert(branch.products(), product, Product::id);
			replaceBranch(franchiseId, new Branch(branch.id(), branch.name(), products));
			return product;
		});
	}

	@Override
	public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
		return Mono.fromRunnable(() -> {
			Branch branch = stored(franchiseId).requireBranch(branchId);
			List<Product> products = new ArrayList<>(branch.products());
			products.removeIf(product -> product.id().equals(productId));
			replaceBranch(franchiseId, new Branch(branch.id(), branch.name(), products));
		});
	}

	private Franchise stored(String franchiseId) {
		Franchise franchise = franchises.get(franchiseId);
		if (franchise == null) {
			throw new IllegalStateException("La franquicia " + franchiseId + " no fue creada en el escenario");
		}
		return franchise;
	}

	private void replaceBranch(String franchiseId, Branch branch) {
		Franchise franchise = stored(franchiseId);
		List<Branch> branches = upsert(franchise.branches(), branch, Branch::id);
		franchises.put(franchiseId, new Franchise(franchise.id(), franchise.name(), branches));
	}

	private static <T> List<T> upsert(List<T> current, T element, Function<T, String> identity) {
		List<T> result = new ArrayList<>(current);
		result.removeIf(existing -> identity.apply(existing).equals(identity.apply(element)));
		result.add(element);
		return result;
	}

}
