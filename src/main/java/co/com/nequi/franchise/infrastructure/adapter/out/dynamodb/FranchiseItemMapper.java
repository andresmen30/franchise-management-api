package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;

final class FranchiseItemMapper {

	private FranchiseItemMapper() {
	}

	static FranchiseItem franchiseItem(Franchise franchise) {
		return item(FranchiseKeys.partition(franchise.id()), FranchiseKeys.METADATA, FranchiseKeys.FRANCHISE,
				franchise.name(), null);
	}

	static FranchiseItem branchItem(String franchiseId, Branch branch) {
		return item(FranchiseKeys.partition(franchiseId), FranchiseKeys.branchSort(branch.id()), FranchiseKeys.BRANCH,
				branch.name(), null);
	}

	static FranchiseItem productItem(String franchiseId, String branchId, Product product) {
		return item(FranchiseKeys.partition(franchiseId), FranchiseKeys.productSort(branchId, product.id()),
				FranchiseKeys.PRODUCT, product.name(), product.stock());
	}

	static Franchise toFranchise(String franchiseId, List<FranchiseItem> items) {
		String franchiseName = items.stream()
				.filter(item -> FranchiseKeys.isMetadata(item.getSk()))
				.map(FranchiseItem::getName)
				.findFirst()
				.orElse(null);
		if (franchiseName == null) {
			return null;
		}

		Map<String, String> branchNames = new LinkedHashMap<>();
		Map<String, List<Product>> productsByBranch = new LinkedHashMap<>();
		for (FranchiseItem item : items) {
			String sortKey = item.getSk();
			if (FranchiseKeys.isBranch(sortKey)) {
				branchNames.put(FranchiseKeys.branchIdFrom(sortKey), item.getName());
			}
			else if (FranchiseKeys.isProduct(sortKey)) {
				productsByBranch.computeIfAbsent(FranchiseKeys.branchIdFrom(sortKey), key -> new ArrayList<>())
						.add(new Product(FranchiseKeys.productIdFrom(sortKey), item.getName(), item.getStock()));
			}
		}

		List<Branch> branches = branchNames.entrySet()
				.stream()
				.map(entry -> new Branch(entry.getKey(), entry.getValue(),
						productsByBranch.getOrDefault(entry.getKey(), List.of())))
				.toList();
		return new Franchise(franchiseId, franchiseName, branches);
	}

	private static FranchiseItem item(String pk, String sk, String entityType, String name, Integer stock) {
		FranchiseItem item = new FranchiseItem();
		item.setPk(pk);
		item.setSk(sk);
		item.setEntityType(entityType);
		item.setName(name);
		item.setStock(stock);
		return item;
	}

}
