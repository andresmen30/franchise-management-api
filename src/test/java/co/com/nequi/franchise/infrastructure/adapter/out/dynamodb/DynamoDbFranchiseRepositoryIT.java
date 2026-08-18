package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.com.nequi.franchise.domain.exception.ProductNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest
class DynamoDbFranchiseRepositoryIT {

	@Container
	static final GenericContainer<?> DYNAMODB = new GenericContainer<>(
			DockerImageName.parse("amazon/dynamodb-local:3.3.1"))
		.withExposedPorts(8000)
		.withCommand("-jar", "DynamoDBLocal.jar", "-sharedDb", "-inMemory")
		.waitingFor(Wait.forLogMessage(".*Initializing DynamoDB Local.*", 1));

	@DynamicPropertySource
	static void dynamoDbProperties(DynamicPropertyRegistry registry) {
		registry.add("app.dynamodb.endpoint",
				() -> "http://" + DYNAMODB.getHost() + ":" + DYNAMODB.getMappedPort(8000));
		registry.add("app.dynamodb.table-name", () -> "franchises-it");
		registry.add("app.dynamodb.create-table-on-startup", () -> true);
	}

	@Autowired
	private FranchiseRepository repository;

	@Test
	void guardaYRecuperaUnaFranquiciaSinSucursales() {
		Franchise franchise = Franchise.create("Nequi Store");

		StepVerifier.create(repository.saveFranchise(franchise)).expectNext(franchise).verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
			.assertNext(stored -> {
				assertThat(stored.id()).isEqualTo(franchise.id());
				assertThat(stored.name()).isEqualTo("Nequi Store");
				assertThat(stored.branches()).isEmpty();
			})
			.verifyComplete();
	}

	@Test
	void noDevuelveNadaCuandoLaFranquiciaNoExiste() {
		StepVerifier.create(repository.findById("franquicia-inexistente")).verifyComplete();
	}

	@Test
	void reconstruyeElAgregadoCompletoEnUnaSolaConsulta() {
		Franchise franchise = Franchise.create("Nequi Store");
		Branch centro = Branch.create("Centro");
		Branch norte = Branch.create("Norte");
		Product cafe = Product.create("Cafe", 40);
		Product te = Product.create("Te", 90);
		Product pan = Product.create("Pan", 7);

		StepVerifier.create(repository.saveFranchise(franchise)
			.then(repository.saveBranch(franchise.id(), centro))
			.then(repository.saveBranch(franchise.id(), norte))
			.then(repository.saveProduct(franchise.id(), centro.id(), cafe))
			.then(repository.saveProduct(franchise.id(), centro.id(), te))
			.then(repository.saveProduct(franchise.id(), norte.id(), pan))).expectNextCount(1).verifyComplete();

		StepVerifier.create(repository.findById(franchise.id())).assertNext(stored -> {
			assertThat(stored.branches()).hasSize(2);
			assertThat(stored.requireBranch(centro.id()).products()).containsExactlyInAnyOrder(cafe, te);
			assertThat(stored.requireBranch(norte.id()).products()).containsExactly(pan);
		}).verifyComplete();
	}

	@Test
	void conservaElStockEnCero() {
		Franchise franchise = Franchise.create("Nequi Store");
		Branch centro = Branch.create("Centro");
		Product agotado = Product.create("Agotado", 0);

		StepVerifier.create(repository.saveFranchise(franchise)
			.then(repository.saveBranch(franchise.id(), centro))
			.then(repository.saveProduct(franchise.id(), centro.id(), agotado))).expectNextCount(1).verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
			.assertNext(stored -> assertThat(stored.requireBranch(centro.id()).requireProduct(agotado.id()).stock())
				.isZero())
			.verifyComplete();
	}

	@Test
	void sobreescribeElProductoAlGuardarloDeNuevo() {
		Franchise franchise = Franchise.create("Nequi Store");
		Branch centro = Branch.create("Centro");
		Product cafe = Product.create("Cafe", 40);

		StepVerifier.create(repository.saveFranchise(franchise)
			.then(repository.saveBranch(franchise.id(), centro))
			.then(repository.saveProduct(franchise.id(), centro.id(), cafe))
			.then(repository.saveProduct(franchise.id(), centro.id(), cafe.withStock(99)))).expectNextCount(1)
			.verifyComplete();

		StepVerifier.create(repository.findById(franchise.id())).assertNext(stored -> {
			List<Product> products = stored.requireBranch(centro.id()).products();
			assertThat(products).hasSize(1);
			assertThat(products.getFirst().stock()).isEqualTo(99);
		}).verifyComplete();
	}

	@Test
	void eliminaSoloElProductoIndicado() {
		Franchise franchise = Franchise.create("Nequi Store");
		Branch centro = Branch.create("Centro");
		Product cafe = Product.create("Cafe", 40);
		Product te = Product.create("Te", 10);

		StepVerifier.create(repository.saveFranchise(franchise)
			.then(repository.saveBranch(franchise.id(), centro))
			.then(repository.saveProduct(franchise.id(), centro.id(), cafe))
			.then(repository.saveProduct(franchise.id(), centro.id(), te))).expectNextCount(1).verifyComplete();

		StepVerifier.create(repository.deleteProduct(franchise.id(), centro.id(), cafe.id())).verifyComplete();

		StepVerifier.create(repository.findById(franchise.id()))
			.assertNext(stored -> assertThat(stored.requireBranch(centro.id()).products()).containsExactly(te))
			.verifyComplete();
	}

	@Test
	void fallaAlEliminarUnProductoInexistente() {
		Franchise franchise = Franchise.create("Nequi Store");
		Branch centro = Branch.create("Centro");

		StepVerifier.create(repository.saveFranchise(franchise).then(repository.saveBranch(franchise.id(), centro)))
			.expectNextCount(1)
			.verifyComplete();

		StepVerifier.create(repository.deleteProduct(franchise.id(), centro.id(), "producto-inexistente"))
			.verifyError(ProductNotFoundException.class);
	}

}
