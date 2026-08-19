package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

class DynamoDbHealthIndicatorTest {

	private static final String TABLE = "franchises";

	private final DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

	private final DynamoDbHealthIndicator indicator = new DynamoDbHealthIndicator(client, TABLE);

	@Test
	void reportaDisponibleCuandoLaTablaResponde() {
		respondeCon(CompletableFuture.completedFuture(DescribeTableResponse.builder()
			.table(TableDescription.builder().tableStatus(TableStatus.ACTIVE).build())
			.build()));

		StepVerifier.create(indicator.health()).assertNext(health -> {
			assertThat(health.getStatus()).isEqualTo(Status.UP);
			assertThat(health.getDetails()).containsEntry("table", TABLE).containsEntry("tableStatus", "ACTIVE");
		}).verifyComplete();
	}

	@Test
	void reportaCaidaCuandoLaTablaNoExiste() {
		respondeCon(CompletableFuture.failedFuture(ResourceNotFoundException.builder().message("no existe").build()));

		StepVerifier.create(indicator.health()).assertNext(health -> {
			assertThat(health.getStatus()).isEqualTo(Status.DOWN);
			assertThat(health.getDetails()).containsEntry("table", TABLE);
		}).verifyComplete();
	}

	@Test
	void noFiltraLaCausaTecnicaEnLosDetalles() {
		respondeCon(CompletableFuture.failedFuture(new IllegalStateException("credenciales invalidas")));

		StepVerifier.create(indicator.health())
			.assertNext(health -> assertThat(health.getDetails().toString()).doesNotContain("credenciales invalidas"))
			.verifyComplete();
	}

	@SuppressWarnings("unchecked")
	private void respondeCon(CompletableFuture<DescribeTableResponse> respuesta) {
		when(client.describeTable(any(Consumer.class))).thenReturn(respuesta);
	}

}
