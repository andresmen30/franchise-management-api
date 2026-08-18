package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.BranchNotFoundException;
import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import reactor.test.StepVerifier;

class AddProductUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final AddProductUseCase useCase = new AddProductUseCase(repository);

	private final Branch branch = Branch.create("Centro");

	private final Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of(branch));

	@Test
	void agregaElProductoALaSucursal() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), "Cafe", 40))
				.assertNext(product -> {
					assertThat(product.name()).isEqualTo("Cafe");
					assertThat(product.stock()).isEqualTo(40);
				})
				.verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
				.assertNext(stored -> assertThat(stored.requireBranch(branch.id()).products()).hasSize(1))
				.verifyComplete();
	}

	@Test
	void fallaSiLaFranquiciaNoExiste() {
		StepVerifier.create(useCase.execute("otra", branch.id(), "Cafe", 1))
				.verifyError(FranchiseNotFoundException.class);
	}

	@Test
	void fallaSiLaSucursalNoExiste() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "sucursal-inexistente", "Cafe", 1))
				.verifyError(BranchNotFoundException.class);
	}

	@Test
	void rechazaStockNegativo() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), branch.id(), "Cafe", -1))
				.verifyError(DomainValidationException.class);
	}

}
