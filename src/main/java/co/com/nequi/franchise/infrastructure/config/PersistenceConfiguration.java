package co.com.nequi.franchise.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import co.com.nequi.franchise.infrastructure.adapter.out.dynamodb.DynamoDbFranchiseRepositoryFactory;
import co.com.nequi.franchise.infrastructure.adapter.out.dynamodb.DynamoDbHealthIndicator;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@Configuration(proxyBeanMethods = false)
class PersistenceConfiguration {

	@Bean
	FranchiseRepository franchiseRepository(DynamoDbEnhancedAsyncClient client, DynamoDbProperties properties) {
		return DynamoDbFranchiseRepositoryFactory.create(client, properties.tableName());
	}

	@Bean
	DynamoDbHealthIndicator dynamoDbHealthIndicator(DynamoDbAsyncClient client, DynamoDbProperties properties) {
		return new DynamoDbHealthIndicator(client, properties.tableName());
	}

}
