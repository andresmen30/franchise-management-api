package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import reactor.test.StepVerifier;

class RenameFranchiseUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final RenameFranchiseUseCase useCase = new RenameFranchiseUseCase(repository);

	private final Branch centro = Branch.create("Centro");

	private final Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of(centro));

	@Test
	void renombraLaFranquiciaConservandoIdentidad() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "Nequi Store SAS")).assertNext(renamed -> {
			assertThat(renamed.id()).isEqualTo(franchise.id());
			assertThat(renamed.name()).isEqualTo("Nequi Store SAS");
		}).verifyComplete();
	}

	@Test
	void noPierdeLasSucursalesAlRenombrar() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "Nequi Store SAS")).expectNextCount(1).verifyComplete();

		StepVerifier.create(repository.findById(franchise.id())).assertNext(stored -> {
			assertThat(stored.name()).isEqualTo("Nequi Store SAS");
			assertThat(stored.branches()).containsExactly(centro);
		}).verifyComplete();
	}

	@Test
	void fallaSiLaFranquiciaNoExiste() {
		StepVerifier.create(useCase.execute("inexistente", "Otro nombre"))
			.verifyError(FranchiseNotFoundException.class);
	}

	@Test
	void rechazaUnNombreVacio() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "   ")).verifyError(DomainValidationException.class);
	}

}
