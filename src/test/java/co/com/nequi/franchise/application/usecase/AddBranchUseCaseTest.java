package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.model.Franchise;
import reactor.test.StepVerifier;

class AddBranchUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final AddBranchUseCase useCase = new AddBranchUseCase(repository);

	private final Franchise franchise = Franchise.create("Nequi Store");

	@Test
	void agregaLaSucursalALaFranquicia() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "Centro"))
				.assertNext(branch -> assertThat(branch.name()).isEqualTo("Centro"))
				.verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
				.assertNext(stored -> assertThat(stored.branches()).singleElement()
						.satisfies(branch -> assertThat(branch.name()).isEqualTo("Centro")))
				.verifyComplete();
	}

	@Test
	void fallaSiLaFranquiciaNoExiste() {
		StepVerifier.create(useCase.execute("franquicia-inexistente", "Centro"))
				.verifyError(FranchiseNotFoundException.class);
	}

	@Test
	void rechazaUnNombreDeSucursalVacio() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "")).verifyError(DomainValidationException.class);
	}

}
