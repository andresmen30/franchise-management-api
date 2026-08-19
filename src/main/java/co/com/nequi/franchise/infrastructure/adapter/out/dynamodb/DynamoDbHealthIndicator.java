package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import java.time.Duration;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

/**
 * Sin esta comprobacion la aplicacion se reporta disponible aunque la tabla sea inalcanzable, y el
 * fallo solo aparece cuando llega la primera peticion de negocio.
 */
public class DynamoDbHealthIndicator implements ReactiveHealthIndicator {

	private static final Duration TIMEOUT = Duration.ofSeconds(3);

	private final DynamoDbAsyncClient client;

	private final String tableName;

	public DynamoDbHealthIndicator(DynamoDbAsyncClient client, String tableName) {
		this.client = client;
		this.tableName = tableName;
	}

	@Override
	public Mono<Health> health() {
		return Mono.fromFuture(() -> client.describeTable(request -> request.tableName(tableName)))
			.map(response -> Health.up()
				.withDetail("table", tableName)
				.withDetail("tableStatus", response.table().tableStatusAsString())
				.build())
			.timeout(TIMEOUT)
			.onErrorResume(error -> Mono.just(Health.down().withDetail("table", tableName).build()));
	}

}
