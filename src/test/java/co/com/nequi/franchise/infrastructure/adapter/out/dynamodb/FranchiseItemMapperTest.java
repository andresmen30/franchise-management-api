package co.com.nequi.franchise.infrastructure.adapter.out.dynamodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.model.Product;

class FranchiseItemMapperTest {

	private static final String FRANCHISE_ID = "f-1";

	@Test
	void convierteLaFranquiciaEnSuItemDeMetadatos() {
		FranchiseItem item = FranchiseItemMapper.franchiseItem(new Franchise(FRANCHISE_ID, "Nequi Store", List.of()));

		assertThat(item.getPk()).isEqualTo("FRANCHISE#f-1");
		assertThat(item.getSk()).isEqualTo("METADATA");
		assertThat(item.getEntityType()).isEqualTo("FRANCHISE");
		assertThat(item.getName()).isEqualTo("Nequi Store");
		assertThat(item.getStock()).isNull();
	}

	@Test
	void convierteLaSucursalEnSuItem() {
		FranchiseItem item = FranchiseItemMapper.branchItem(FRANCHISE_ID, new Branch("b-1", "Centro", List.of()));

		assertThat(item.getPk()).isEqualTo("FRANCHISE#f-1");
		assertThat(item.getSk()).isEqualTo("BRANCH#b-1");
		assertThat(item.getEntityType()).isEqualTo("BRANCH");
		assertThat(item.getStock()).isNull();
	}

	@Test
	void convierteElProductoEnSuItemConservandoElStock() {
		FranchiseItem item = FranchiseItemMapper.productItem(FRANCHISE_ID, "b-1", new Product("p-1", "Cafe", 40));

		assertThat(item.getSk()).isEqualTo("BRANCH#b-1#PRODUCT#p-1");
		assertThat(item.getEntityType()).isEqualTo("PRODUCT");
		assertThat(item.getStock()).isEqualTo(40);
	}

	@Test
	void reconstruyeElAgregadoAgrupandoCadaProductoConSuSucursal() {
		List<FranchiseItem> items = List.of(item("METADATA", "FRANCHISE", "Nequi Store", null),
				item("BRANCH#b-1", "BRANCH", "Centro", null), item("BRANCH#b-2", "BRANCH", "Norte", null),
				item("BRANCH#b-1#PRODUCT#p-1", "PRODUCT", "Cafe", 40),
				item("BRANCH#b-1#PRODUCT#p-2", "PRODUCT", "Te", 90),
				item("BRANCH#b-2#PRODUCT#p-3", "PRODUCT", "Pan", 120));

		Franchise franchise = FranchiseItemMapper.toFranchise(FRANCHISE_ID, items);

		assertThat(franchise.name()).isEqualTo("Nequi Store");
		assertThat(franchise.branches()).hasSize(2);
		assertThat(franchise.requireBranch("b-1").products())
			.extracting(Product::name)
			.containsExactly("Cafe", "Te");
		assertThat(franchise.requireBranch("b-2").products()).extracting(Product::name).containsExactly("Pan");
	}

	@Test
	void devuelveNuloCuandoNoExisteElItemDeMetadatos() {
		List<FranchiseItem> huerfanos = List.of(item("BRANCH#b-1", "BRANCH", "Centro", null),
				item("BRANCH#b-1#PRODUCT#p-1", "PRODUCT", "Cafe", 40));

		assertThat(FranchiseItemMapper.toFranchise(FRANCHISE_ID, huerfanos)).isNull();
	}

	@Test
	void reconstruyeUnaFranquiciaSinSucursales() {
		Franchise franchise = FranchiseItemMapper.toFranchise(FRANCHISE_ID,
				List.of(item("METADATA", "FRANCHISE", "Nequi Store", null)));

		assertThat(franchise.branches()).isEmpty();
	}

	@Test
	void conservaLaSucursalAunqueNoTengaProductos() {
		Franchise franchise = FranchiseItemMapper.toFranchise(FRANCHISE_ID,
				List.of(item("METADATA", "FRANCHISE", "Nequi Store", null), item("BRANCH#b-1", "BRANCH", "Vacia", null)));

		assertThat(franchise.requireBranch("b-1").products()).isEmpty();
	}

	@Test
	void conservaElStockEnCeroAlReconstruir() {
		Franchise franchise = FranchiseItemMapper.toFranchise(FRANCHISE_ID,
				List.of(item("METADATA", "FRANCHISE", "Nequi Store", null), item("BRANCH#b-1", "BRANCH", "Centro", null),
						item("BRANCH#b-1#PRODUCT#p-1", "PRODUCT", "Agotado", 0)));

		assertThat(franchise.requireBranch("b-1").requireProduct("p-1").stock()).isZero();
	}

	private static FranchiseItem item(String sortKey, String entityType, String name, Integer stock) {
		FranchiseItem item = new FranchiseItem();
		item.setPk(FranchiseKeys.partition(FRANCHISE_ID));
		item.setSk(sortKey);
		item.setEntityType(entityType);
		item.setName(name);
		item.setStock(stock);
		return item;
	}

}
