package co.com.nequi.franchise.domain.port.out;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

	/**
	 * Devuelve el agregado completo de la franquicia, o vacio si no existe. La ausencia no es un
	 * error: cada caso de uso decide como traducirla.
	 */
	Mono<Franchise> findById(String franchiseId);

	Mono<Franchise> saveFranchise(Franchise franchise);

	Mono<Branch> saveBranch(String franchiseId, Branch branch);

	Mono<Product> saveProduct(String franchiseId, String branchId, Product product);

	/**
	 * Falla con {@link co.com.nequi.franchise.domain.exception.ProductNotFoundException} si el
	 * producto no existe, en lugar de completar en silencio.
	 */
	Mono<Void> deleteProduct(String franchiseId, String branchId, String productId);

}
