package co.com.nequi.franchise.infrastructure.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@Configuration(proxyBeanMethods = false)
class DynamoDbConfig {

	@Bean(destroyMethod = "close")
	DynamoDbAsyncClient dynamoDbAsyncClient(DynamoDbProperties properties) {
		DynamoDbAsyncClientBuilder builder = DynamoDbAsyncClient.builder().region(Region.of(properties.region()));
		if (properties.usesLocalEndpoint()) {
			builder.endpointOverride(URI.create(properties.endpoint()))
					.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")));
		}
		else {
			builder.credentialsProvider(DefaultCredentialsProvider.create());
		}
		return builder.build();
	}

	@Bean
	DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient(DynamoDbAsyncClient client) {
		return DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(client).build();
	}

}
