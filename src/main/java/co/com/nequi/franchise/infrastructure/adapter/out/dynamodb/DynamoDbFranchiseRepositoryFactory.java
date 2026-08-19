package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public final class DynamoDbFranchiseRepositoryFactory {

	private DynamoDbFranchiseRepositoryFactory() {
	}

	public static FranchiseRepository create(DynamoDbEnhancedAsyncClient client, String tableName) {
		return new DynamoDbFranchiseRepository(client.table(tableName, TableSchema.fromBean(FranchiseItem.class)));
	}

}
