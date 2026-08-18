package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.exception.ProductNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.test.StepVerifier;

class UpdateProductStockUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final UpdateProductStockUseCase useCase = new UpdateProductStockUseCase(repository);

	private final Product cafe = Product.create("Cafe", 40);

	private final Branch branch = new Branch("sucursal-1", "Centro", List.of(cafe));

	private final Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of(branch));

	@Test
	void actualizaElStockConservandoIdentidadYNombre() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), cafe.id(), 99))
				.assertNext(product -> {
					assertThat(product.id()).isEqualTo(cafe.id());
					assertThat(product.name()).isEqualTo("Cafe");
					assertThat(product.stock()).isEqualTo(99);
				})
				.verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
				.assertNext(stored -> assertThat(stored.requireBranch(branch.id()).requireProduct(cafe.id()).stock())
						.isEqualTo(99))
				.verifyComplete();
	}

	@Test
	void permiteDejarElStockEnCero() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), cafe.id(), 0))
				.assertNext(product -> assertThat(product.stock()).isZero())
				.verifyComplete();
	}

	@Test
	void rechazaStockNegativo() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), cafe.id(), -3))
				.verifyError(DomainValidationException.class);
	}

	@Test
	void fallaSiElProductoNoExiste() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), "otro", 5))
				.verifyError(ProductNotFoundException.class);
	}

}
