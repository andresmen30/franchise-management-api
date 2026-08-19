package co.com.nequi.franchise.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.DomainValidationException;

class BranchTest {

	@Test
	void creaUnaSucursalSinProductos() {
		Branch branch = Branch.create("Centro");

		assertThat(branch.id()).isNotBlank();
		assertThat(branch.name()).isEqualTo("Centro");
		assertThat(branch.products()).isEmpty();
	}

	@Test
	void rechazaNombreVacio() {
		assertThatThrownBy(() -> Branch.create("  "))
				.isInstanceOf(DomainValidationException.class)
				.hasMessageContaining("nombre de la sucursal");
	}

	@Test
	void trataUnaListaNulaDeProductosComoVacia() {
		Branch branch = new Branch("id-1", "Centro", null);

		assertThat(branch.products()).isEmpty();
	}

	@Test
	void noExponeLaListaInternaDeProductos() {
		Branch branch = new Branch("id-1", "Centro", List.of(Product.create("Cafe", 5)));

		assertThatThrownBy(() -> branch.products().add(Product.create("Te", 1)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void copiaLosProductosRecibidosEnLaConstruccion() {
		List<Product> mutable = new ArrayList<>(List.of(Product.create("Cafe", 5)));
		Branch branch = new Branch("id-1", "Centro", mutable);

		mutable.clear();

		assertThat(branch.products()).hasSize(1);
	}

	@Test
	void encuentraUnProductoPorSuIdentificador() {
		Product cafe = Product.create("Cafe", 5);
		Branch branch = new Branch("id-1", "Centro", List.of(cafe, Product.create("Te", 1)));

		assertThat(branch.findProduct(cafe.id())).contains(cafe);
	}

	@Test
	void noEncuentraUnProductoInexistente() {
		Branch branch = new Branch("id-1", "Centro", List.of(Product.create("Cafe", 5)));

		assertThat(branch.findProduct("otro-id")).isEmpty();
	}

	@Test
	void devuelveElProductoConMayorStock() {
		Product cafe = Product.create("Cafe", 5);
		Product te = Product.create("Te", 90);
		Product pan = Product.create("Pan", 12);
		Branch branch = new Branch("id-1", "Centro", List.of(cafe, te, pan));

		assertThat(branch.productWithHighestStock()).contains(te);
	}

	@Test
	void desempataPorNombreAscendenteCuandoElStockEsIgual() {
		Product zanahoria = Product.create("Zanahoria", 50);
		Product aguacate = Product.create("Aguacate", 50);
		Branch branch = new Branch("id-1", "Centro", List.of(zanahoria, aguacate));

		assertThat(branch.productWithHighestStock()).contains(aguacate);
	}

	@Test
	void noDevuelveProductoCuandoLaSucursalEstaVacia() {
		assertThat(Branch.create("Centro").productWithHighestStock()).isEmpty();
	}

	@Test
	void cambiarElNombreConservaIdentidadYProductos() {
		Branch original = new Branch("id-1", "Centro", List.of(Product.create("Cafe", 5)));

		Branch updated = original.withName("Centro Mayor");

		assertThat(updated.id()).isEqualTo("id-1");
		assertThat(updated.name()).isEqualTo("Centro Mayor");
		assertThat(updated.products()).isEqualTo(original.products());
	}

}
