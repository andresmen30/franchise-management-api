package co.com.nequi.franchise.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.BranchTopProduct;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import reactor.test.StepVerifier;

class GetTopStockProductsUseCaseTest {

	private final InMemoryFranchiseRepository repository = new InMemoryFranchiseRepository();

	private final GetTopStockProductsUseCase useCase = new GetTopStockProductsUseCase(repository);

	@Test
	void devuelveElProductoDeMayorStockDeCadaSucursalConSuSucursal() {
		Product cafeCentro = Product.create("Cafe", 5);
		Product teCentro = Product.create("Te", 90);
		Branch centro = new Branch("sucursal-centro", "Centro", List.of(cafeCentro, teCentro));

		Product panNorte = Product.create("Pan", 120);
		Product jugoNorte = Product.create("Jugo", 30);
		Branch norte = new Branch("sucursal-norte", "Norte", List.of(panNorte, jugoNorte));

		repository.seed(new Franchise("franquicia-1", "Nequi Store", List.of(centro, norte)));

		StepVerifier.create(useCase.execute("franquicia-1"))
				.expectNext(new BranchTopProduct("sucursal-centro", "Centro", teCentro))
				.expectNext(new BranchTopProduct("sucursal-norte", "Norte", panNorte))
				.verifyComplete();
	}

	@Test
	void omiteLasSucursalesSinProductos() {
		Product cafe = Product.create("Cafe", 5);
		Branch conProductos = new Branch("sucursal-1", "Centro", List.of(cafe));
		Branch vacia = new Branch("sucursal-2", "Norte", List.of());

		repository.seed(new Franchise("franquicia-1", "Nequi Store", List.of(conProductos, vacia)));

		StepVerifier.create(useCase.execute("franquicia-1"))
				.assertNext(top -> assertThat(top.branchId()).isEqualTo("sucursal-1"))
				.verifyComplete();
	}

	@Test
	void noDevuelveNadaCuandoLaFranquiciaNoTieneSucursales() {
		Franchise franchise = Franchise.create("Nequi Store");
		repository.seed(franchise);

		StepVerifier.create(useCase.execute(franchise.id())).verifyComplete();
	}

	@Test
	void fallaSiLaFranquiciaNoExiste() {
		StepVerifier.create(useCase.execute("inexistente")).verifyError(FranchiseNotFoundException.class);
	}

}
