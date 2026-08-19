package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.BranchNotFoundException;
import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.test.StepVerifier;

class RenameBranchUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final RenameBranchUseCase useCase = new RenameBranchUseCase(repository);

	private final Product cafe = Product.create("Cafe", 40);

	private final Branch centro = new Branch("sucursal-1", "Centro", List.of(cafe));

	private final Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of(centro));

	@Test
	void renombraLaSucursalConservandoIdentidad() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), "Centro Mayor")).assertNext(renamed -> {
			assertThat(renamed.id()).isEqualTo(centro.id());
			assertThat(renamed.name()).isEqualTo("Centro Mayor");
		}).verifyComplete();
	}

	@Test
	void noPierdeLosProductosAlRenombrar() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), "Centro Mayor")).expectNextCount(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(franchise.id())).assertNext(stored -> {
			Branch renamed = stored.requireBranch(centro.id());
			assertThat(renamed.name()).isEqualTo("Centro Mayor");
			assertThat(renamed.products()).containsExactly(cafe);
		}).verifyComplete();
	}

	@Test
	void fallaSiLaSucursalNoExiste() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), "otra", "Centro Mayor"))
			.verifyError(BranchNotFoundException.class);
	}

	@Test
	void rechazaUnNombreVacio() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), ""))
			.verifyError(DomainValidationException.class);
	}

}
