package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

/**
 * Esquema de claves de la tabla. El formato de {@code sk} es un contrato con los datos ya
 * almacenados: cambiarlo deja inalcanzables los items existentes y exige migrarlos.
 */
final class FranchiseKeys {

	static final String METADATA = "METADATA";

	static final String FRANCHISE = "FRANCHISE";

	static final String BRANCH = "BRANCH";

	static final String PRODUCT = "PRODUCT";

	private static final String FRANCHISE_PREFIX = "FRANCHISE#";

	private static final String BRANCH_PREFIX = "BRANCH#";

	private static final String PRODUCT_SEPARATOR = "#PRODUCT#";

	private FranchiseKeys() {
	}

	static String partition(String franchiseId) {
		return FRANCHISE_PREFIX + franchiseId;
	}

	static String branchSort(String branchId) {
		return BRANCH_PREFIX + branchId;
	}

	static String productSort(String branchId, String productId) {
		return BRANCH_PREFIX + branchId + PRODUCT_SEPARATOR + productId;
	}

	static boolean isMetadata(String sortKey) {
		return METADATA.equals(sortKey);
	}

	static boolean isProduct(String sortKey) {
		return sortKey.contains(PRODUCT_SEPARATOR);
	}

	static boolean isBranch(String sortKey) {
		return sortKey.startsWith(BRANCH_PREFIX) && !isProduct(sortKey);
	}

	static String branchIdFrom(String sortKey) {
		String withoutPrefix = sortKey.substring(BRANCH_PREFIX.length());
		int separator = withoutPrefix.indexOf(PRODUCT_SEPARATOR);
		return separator < 0 ? withoutPrefix : withoutPrefix.substring(0, separator);
	}

	static String productIdFrom(String sortKey) {
		return sortKey.substring(sortKey.indexOf(PRODUCT_SEPARATOR) + PRODUCT_SEPARATOR.length());
	}

}
