package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FranchiseKeysTest {

	private static final String FRANCHISE = "f-1";

	private static final String BRANCH = "b-1";

	private static final String PRODUCT = "p-1";

	@Test
	void componeLaClaveDeParticionDesdeLaFranquicia() {
		assertThat(FranchiseKeys.partition(FRANCHISE)).isEqualTo("FRANCHISE#f-1");
	}

	@Test
	void componeLaClaveDeOrdenamientoDeUnaSucursal() {
		assertThat(FranchiseKeys.branchSort(BRANCH)).isEqualTo("BRANCH#b-1");
	}

	@Test
	void componeLaClaveDeOrdenamientoDeUnProductoAnidadoEnSuSucursal() {
		assertThat(FranchiseKeys.productSort(BRANCH, PRODUCT)).isEqualTo("BRANCH#b-1#PRODUCT#p-1");
	}

	@Test
	void reconoceLaClaveDeMetadatosDeLaFranquicia() {
		assertThat(FranchiseKeys.isMetadata(FranchiseKeys.METADATA)).isTrue();
		assertThat(FranchiseKeys.isMetadata(FranchiseKeys.branchSort(BRANCH))).isFalse();
	}

	@Test
	void distingueUnaSucursalDeUnProducto() {
		String sucursal = FranchiseKeys.branchSort(BRANCH);
		String producto = FranchiseKeys.productSort(BRANCH, PRODUCT);

		assertThat(FranchiseKeys.isBranch(sucursal)).isTrue();
		assertThat(FranchiseKeys.isProduct(sucursal)).isFalse();

		assertThat(FranchiseKeys.isBranch(producto)).isFalse();
		assertThat(FranchiseKeys.isProduct(producto)).isTrue();
	}

	@Test
	void noConfundeLosMetadatosConUnaSucursal() {
		assertThat(FranchiseKeys.isBranch(FranchiseKeys.METADATA)).isFalse();
		assertThat(FranchiseKeys.isProduct(FranchiseKeys.METADATA)).isFalse();
	}

	@Test
	void extraeLaSucursalDesdeSuPropiaClave() {
		assertThat(FranchiseKeys.branchIdFrom(FranchiseKeys.branchSort(BRANCH))).isEqualTo(BRANCH);
	}

	@Test
	void extraeLaSucursalDesdeLaClaveDeUnProducto() {
		assertThat(FranchiseKeys.branchIdFrom(FranchiseKeys.productSort(BRANCH, PRODUCT))).isEqualTo(BRANCH);
	}

	@Test
	void extraeElProductoDesdeSuClave() {
		assertThat(FranchiseKeys.productIdFrom(FranchiseKeys.productSort(BRANCH, PRODUCT))).isEqualTo(PRODUCT);
	}

	@Test
	void soportaIdentificadoresQueContienenGuiones() {
		String branchId = "8401fab9-46eb-4d2f-9087-4762f8af9061";
		String productId = "2bd25b15-dea4-4eb0-8009-216c2b58acf2";
		String sortKey = FranchiseKeys.productSort(branchId, productId);

		assertThat(FranchiseKeys.branchIdFrom(sortKey)).isEqualTo(branchId);
		assertThat(FranchiseKeys.productIdFrom(sortKey)).isEqualTo(productId);
	}

}
