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

class RenameProductUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final RenameProductUseCase useCase = new RenameProductUseCase(repository);

	private final Product cafe = Product.create("Cafe", 40);

	private final Branch centro = new Branch("sucursal-1", "Centro", List.of(cafe));

	private final Franchise franchise = new Franchise("franquicia-1", "Nequi Store", List.of(centro));

	@Test
	void renombraElProductoConservandoIdentidadYStock() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), cafe.id(), "Cafe premium"))
			.assertNext(renamed -> {
				assertThat(renamed.id()).isEqualTo(cafe.id());
				assertThat(renamed.name()).isEqualTo("Cafe premium");
				assertThat(renamed.stock()).isEqualTo(40);
			})
			.verifyComplete();
	}

	@Test
	void persisteElNuevoNombreSinAlterarElStock() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), cafe.id(), "Cafe premium"))
			.expectNextCount(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(franchise.id())).assertNext(stored -> {
			Product renamed = stored.requireBranch(centro.id()).requireProduct(cafe.id());
			assertThat(renamed.name()).isEqualTo("Cafe premium");
			assertThat(renamed.stock()).isEqualTo(40);
		}).verifyComplete();
	}

	@Test
	void fallaSiElProductoNoExiste() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), "otro", "Nuevo"))
			.verifyError(ProductNotFoundException.class);
	}

	@Test
	void rechazaUnNombreVacio() {
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id(), centro.id(), cafe.id(), "  "))
			.verifyError(DomainValidationException.class);
	}

}
