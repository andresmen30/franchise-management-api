package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import co.com.nequi.franchise.domain.exception.ProductNotFoundException;
import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

class DynamoDbFranchiseRepository implements FranchiseRepository {

	private static final Expression MUST_EXIST = Expression.builder().expression("attribute_exists(sk)").build();

	private final DynamoDbAsyncTable<FranchiseItem> table;

	DynamoDbFranchiseRepository(DynamoDbAsyncTable<FranchiseItem> table) {
		this.table = table;
	}

	@Override
	public Mono<Franchise> findById(String franchiseId) {
		QueryConditional wholeFranchise = QueryConditional
				.keyEqualTo(key -> key.partitionValue(FranchiseKeys.partition(franchiseId)));
		return Flux.from(table.query(wholeFranchise).items())
				.collectList()
				.flatMap(items -> Mono.justOrEmpty(FranchiseItemMapper.toFranchise(franchiseId, items)));
	}

	@Override
	public Mono<Franchise> saveFranchise(Franchise franchise) {
		return put(FranchiseItemMapper.franchiseItem(franchise)).thenReturn(franchise);
	}

	@Override
	public Mono<Branch> saveBranch(String franchiseId, Branch branch) {
		return put(FranchiseItemMapper.branchItem(franchiseId, branch)).thenReturn(branch);
	}

	@Override
	public Mono<Product> saveProduct(String franchiseId, String branchId, Product product) {
		return put(FranchiseItemMapper.productItem(franchiseId, branchId, product)).thenReturn(product);
	}

	@Override
	public Mono<Void> deleteProduct(String franchiseId, String branchId, String productId) {
		Key key = Key.builder()
				.partitionValue(FranchiseKeys.partition(franchiseId))
				.sortValue(FranchiseKeys.productSort(branchId, productId))
				.build();
		return Mono
				.fromFuture(() -> table
						.deleteItemWithResponse(request -> request.key(key).conditionExpression(MUST_EXIST)))
				.onErrorMap(DynamoDbFranchiseRepository::isConditionalCheckFailure,
						error -> new ProductNotFoundException(productId))
				.then();
	}

	private Mono<Void> put(FranchiseItem item) {
		return Mono.fromFuture(() -> table.putItem(item));
	}

	// El SDK entrega el fallo envuelto en CompletionException, por lo que comparar el tipo
	// directamente no lo detecta.
	private static boolean isConditionalCheckFailure(Throwable error) {
		for (Throwable current = error; current != null; current = current.getCause()) {
			if (current instanceof ConditionalCheckFailedException) {
				return true;
			}
		}
		return false;
	}

}
