package co.com.nequi.franchise.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.dynamodb")
public record DynamoDbProperties(@NotBlank String tableName, @NotBlank String region, String endpoint,
		boolean createTableOnStartup) {

	public boolean usesLocalEndpoint() {
		return endpoint != null && !endpoint.isBlank();
	}

}
