package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.DomainValidationException;
import reactor.test.StepVerifier;

class CreateFranchiseUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final CreateFranchiseUseCase useCase = new CreateFranchiseUseCase(repository);

	@Test
	void creaLaFranquiciaYLaPersiste() {
		StepVerifier.create(useCase.execute("Nequi Store"))
				.assertNext(franchise -> {
					assertThat(franchise.id()).isNotBlank();
					assertThat(franchise.name()).isEqualTo("Nequi Store");
					assertThat(franchise.branches()).isEmpty();
					StepVerifier.create(repository.findById(franchise.id())).expectNext(franchise).verifyComplete();
				})
				.verifyComplete();
	}

	@Test
	void rechazaUnNombreVacioSinTocarElRepositorio() {
		StepVerifier.create(useCase.execute("  ")).verifyError(DomainValidationException.class);
	}

}
