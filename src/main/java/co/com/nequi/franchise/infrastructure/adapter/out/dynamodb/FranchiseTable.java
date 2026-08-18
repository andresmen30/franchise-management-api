package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import java.util.concurrent.CompletionException;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public final class FranchiseTable {

	private static final String PARTITION_KEY = "pk";

	private static final String SORT_KEY = "sk";

	private FranchiseTable() {
	}

	public static void createIfMissing(DynamoDbAsyncClient client, String tableName) {
		try {
			client.createTable(request -> request.tableName(tableName)
					.keySchema(KeySchemaElement.builder().attributeName(PARTITION_KEY).keyType(KeyType.HASH).build(),
							KeySchemaElement.builder().attributeName(SORT_KEY).keyType(KeyType.RANGE).build())
					.attributeDefinitions(
							AttributeDefinition.builder()
									.attributeName(PARTITION_KEY)
									.attributeType(ScalarAttributeType.S)
									.build(),
							AttributeDefinition.builder()
									.attributeName(SORT_KEY)
									.attributeType(ScalarAttributeType.S)
									.build())
					.billingMode(BillingMode.PAY_PER_REQUEST)).join();
		}
		catch (CompletionException ex) {
			if (!(ex.getCause() instanceof ResourceInUseException)) {
				throw ex;
			}
		}
		client.waiter().waitUntilTableExists(request -> request.tableName(tableName)).join();
	}

}
