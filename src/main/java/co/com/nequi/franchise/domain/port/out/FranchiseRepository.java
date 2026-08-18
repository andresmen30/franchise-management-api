package co.com.nequi.franchise.domain.port.out;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

	Mono<Franchise> findById(String franchiseId);

	Mono<Franchise> saveFranchise(Franchise franchise);

	Mono<Branch> saveBranch(String franchiseId, Branch branch);

	Mono<Product> saveProduct(String franchiseId, String branchId, Product product);

	Mono<Void> deleteProduct(String franchiseId, String branchId, String productId);

}
