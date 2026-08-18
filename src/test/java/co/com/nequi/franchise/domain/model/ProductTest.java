package co.com.nequi.franchise.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.com.nequi.franchise.domain.exception.DomainValidationException;

class ProductTest {

	@Test
	void creaUnProductoConIdentificadorGenerado() {
		Product product = Product.create("Cafe", 10);

		assertThat(product.id()).isNotBlank();
		assertThat(product.name()).isEqualTo("Cafe");
		assertThat(product.stock()).isEqualTo(10);
	}

	@Test
	void recortaLosEspaciosDelNombre() {
		assertThat(Product.create("  Cafe  ", 1).name()).isEqualTo("Cafe");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "\t" })
	void rechazaNombresVacios(String name) {
		assertThatThrownBy(() -> Product.create(name, 1))
				.isInstanceOf(DomainValidationException.class)
				.hasMessageContaining("nombre del producto");
	}

	@Test
	void rechazaNombresDemasiadoLargos() {
		String tooLong = "a".repeat(121);

		assertThatThrownBy(() -> Product.create(tooLong, 1))
				.isInstanceOf(DomainValidationException.class)
				.hasMessageContaining("120");
	}

	@Test
	void rechazaStockNegativo() {
		assertThatThrownBy(() -> Product.create("Cafe", -1))
				.isInstanceOf(DomainValidationException.class)
				.hasMessageContaining("stock");
	}

	@Test
	void aceptaStockEnCero() {
		assertThat(Product.create("Cafe", 0).stock()).isZero();
	}

	@Test
	void cambiarElStockConservaIdentidadYNombre() {
		Product original = Product.create("Cafe", 10);

		Product updated = original.withStock(25);

		assertThat(updated.id()).isEqualTo(original.id());
		assertThat(updated.name()).isEqualTo(original.name());
		assertThat(updated.stock()).isEqualTo(25);
		assertThat(original.stock()).isEqualTo(10);
	}

	@Test
	void cambiarElStockAUnValorNegativoFalla() {
		Product product = Product.create("Cafe", 10);

		assertThatThrownBy(() -> product.withStock(-5)).isInstanceOf(DomainValidationException.class);
	}

	@Test
	void cambiarElNombreConservaIdentidadYStock() {
		Product original = Product.create("Cafe", 10);

		Product updated = original.withName("Cafe premium");

		assertThat(updated.id()).isEqualTo(original.id());
		assertThat(updated.name()).isEqualTo("Cafe premium");
		assertThat(updated.stock()).isEqualTo(10);
	}

}
