package co.com.nequi.franchise.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.BranchResponse;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.FranchiseResponse;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.ProductResponse;
import co.com.nequi.franchise.infrastructure.adapter.in.rest.dto.TopStockProductResponse;

@Testcontainers
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FranchiseApiIT {

	private static final String FRANCHISES = "/api/v1/franchises";

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
		registry.add("app.dynamodb.table-name", () -> "franchises-e2e");
		registry.add("app.dynamodb.create-table-on-startup", () -> true);
		registry.add("management.endpoint.health.show-details", () -> "always");
	}

	@Autowired
	private WebTestClient client;

	@Test
	void recorreElCicloCompletoDeLaApi() {
		WebTestClient http = client.mutate().responseTimeout(Duration.ofSeconds(20)).build();

		String franchiseId = crearFranquicia(http, "Nequi Store");
		String centro = crearSucursal(http, franchiseId, "Centro");
		String norte = crearSucursal(http, franchiseId, "Norte");

		String cafe = crearProducto(http, franchiseId, centro, "Cafe", 40);
		crearProducto(http, franchiseId, centro, "Te", 90);
		crearProducto(http, franchiseId, norte, "Pan", 120);

		assertThat(topStock(http, franchiseId))
			.extracting(TopStockProductResponse::branchName, TopStockProductResponse::productName,
					TopStockProductResponse::stock)
			.containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("Centro", "Te", 90),
					org.assertj.core.groups.Tuple.tuple("Norte", "Pan", 120));

		http.put()
			.uri(FRANCHISES + "/{f}/branches/{b}/products/{p}/stock", franchiseId, centro, cafe)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"stock\":500}")
			.exchange()
			.expectStatus()
			.isOk();

		assertThat(topStock(http, franchiseId))
			.filteredOn(top -> top.branchName().equals("Centro"))
			.singleElement()
			.satisfies(top -> {
				assertThat(top.productName()).isEqualTo("Cafe");
				assertThat(top.stock()).isEqualTo(500);
			});

		http.delete()
			.uri(FRANCHISES + "/{f}/branches/{b}/products/{p}", franchiseId, centro, cafe)
			.exchange()
			.expectStatus()
			.isNoContent();

		assertThat(topStock(http, franchiseId))
			.filteredOn(top -> top.branchName().equals("Centro"))
			.singleElement()
			.satisfies(top -> assertThat(top.productName()).isEqualTo("Te"));
	}

	@Test
	void elReadinessReflejaElEstadoDeLaBaseDeDatos() {
		client.get()
			.uri("/actuator/health/readiness")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.status")
			.isEqualTo("UP")
			.jsonPath("$.components.dynamoDb.status")
			.isEqualTo("UP");
	}

	@Test
	void devuelveProblemDetailCuandoLaFranquiciaNoExiste() {
		client.get()
			.uri(FRANCHISES + "/inexistente/branches/top-stock-products")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.code")
			.isEqualTo("FRANCHISE_NOT_FOUND");
	}

	@Test
	void noFiltraDetallesInternosEnLosErrores() {
		client.post()
			.uri(FRANCHISES)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"\"}")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody(String.class)
			.value(body -> assertThat(body).doesNotContain("java.", "software.amazon", "org.springframework",
					"Exception", "at co.com.nequi"));
	}

	private String crearFranquicia(WebTestClient http, String name) {
		FranchiseResponse response = http.post()
			.uri(FRANCHISES)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"" + name + "\"}")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.exists("Location")
			.expectBody(FranchiseResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(response).isNotNull();
		return response.id();
	}

	private String crearSucursal(WebTestClient http, String franchiseId, String name) {
		BranchResponse response = http.post()
			.uri(FRANCHISES + "/{f}/branches", franchiseId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"" + name + "\"}")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectBody(BranchResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(response).isNotNull();
		return response.id();
	}

	private String crearProducto(WebTestClient http, String franchiseId, String branchId, String name, int stock) {
		ProductResponse response = http.post()
			.uri(FRANCHISES + "/{f}/branches/{b}/products", franchiseId, branchId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"name\":\"" + name + "\",\"stock\":" + stock + "}")
			.exchange()
			.expectStatus()
			.isCreated()
			.expectBody(ProductResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(response).isNotNull();
		return response.id();
	}

	private List<TopStockProductResponse> topStock(WebTestClient http, String franchiseId) {
		return http.get()
			.uri(FRANCHISES + "/{f}/branches/top-stock-products", franchiseId)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBodyList(TopStockProductResponse.class)
			.returnResult()
			.getResponseBody();
	}

}
