package co.com.nequi.franchise.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.nequi.franchise.infrastructure.adapter.out.dynamodb.FranchiseTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "app.dynamodb", name = "create-table-on-startup", havingValue = "true")
class TableInitializationConfiguration {

	@Bean
	ApplicationRunner franchiseTableInitializer(DynamoDbAsyncClient client, DynamoDbProperties properties) {
		return args -> FranchiseTable.createIfMissing(client, properties.tableName());
	}

}
