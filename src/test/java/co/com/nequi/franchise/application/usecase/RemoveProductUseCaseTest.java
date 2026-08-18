package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.BranchNotFoundException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.exception.ProductNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.test.StepVerifier;

class RemoveProductUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final RemoveProductUseCase useCase = new RemoveProductUseCase(repository);

	private final Product cafe = Product.create("Cafe", 40);

	private final Product te = Product.create("Te", 10);

	private final Branch branch = new Branch("sucursal-1", "Centro", List.of(cafe, te));

	private final Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of(branch));

	@Test
	void eliminaElProductoDeLaSucursal() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), cafe.id())).verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
				.assertNext(stored -> assertThat(stored.requireBranch(branch.id()).products()).containsExactly(te))
				.verifyComplete();
	}

	@Test
	void fallaSiElProductoNoExiste() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), "producto-inexistente"))
				.verifyError(ProductNotFoundException.class);
	}

	@Test
	void fallaSiLaSucursalNoExiste() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "otra-sucursal", cafe.id()))
				.verifyError(BranchNotFoundException.class);
	}

	@Test
	void fallaSiLaFranquiciaNoExiste() {
		StepVerifier.create(useCase.execute("otra", branch.id(), cafe.id()))
				.verifyError(FranchiseNotFoundException.class);
	}

}
