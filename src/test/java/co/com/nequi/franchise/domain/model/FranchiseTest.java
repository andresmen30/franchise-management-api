package co.com.nequi.franchise.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.com.nequi.franchise.domain.exception.DomainValidationException;

class FranchiseTest {

	@Test
	void creaUnaFranquiciaSinSucursales() {
		Franchise franchise = Franchise.create("Nequi Store");

		assertThat(franchise.id()).isNotBlank();
		assertThat(franchise.name()).isEqualTo("Nequi Store");
		assertThat(franchise.branches()).isEmpty();
	}

	@Test
	void rechazaNombreVacio() {
		assertThatThrownBy(() -> Franchise.create(""))
				.isInstanceOf(DomainValidationException.class)
				.hasMessageContaining("nombre de la franquicia");
	}

	@Test
	void trataUnaListaNulaDeSucursalesComoVacia() {
		assertThat(new Franchise("id-1", "Nequi Store", null).branches()).isEmpty();
	}

	@Test
	void noExponeLaListaInternaDeSucursales() {
		Franchise franchise = new Franchise("id-1", "Nequi Store", List.of(Branch.create("Centro")));

		assertThatThrownBy(() -> franchise.branches().add(Branch.create("Norte")))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void copiaLasSucursalesRecibidasEnLaConstruccion() {
		List<Branch> mutable = new ArrayList<>(List.of(Branch.create("Centro")));
		Franchise franchise = new Franchise("id-1", "Nequi Store", mutable);

		mutable.clear();

		assertThat(franchise.branches()).hasSize(1);
	}

	@Test
	void encuentraUnaSucursalPorSuIdentificador() {
		Branch centro = Branch.create("Centro");
		Franchise franchise = new Franchise("id-1", "Nequi Store", List.of(centro, Branch.create("Norte")));

		assertThat(franchise.findBranch(centro.id())).contains(centro);
	}

	@Test
	void noEncuentraUnaSucursalInexistente() {
		Franchise franchise = new Franchise("id-1", "Nequi Store", List.of(Branch.create("Centro")));

		assertThat(franchise.findBranch("otro-id")).isEmpty();
	}

	@Test
	void cambiarElNombreConservaIdentidadYSucursales() {
		Franchise original = new Franchise("id-1", "Nequi Store", List.of(Branch.create("Centro")));

		Franchise updated = original.withName("Nequi Store SAS");

		assertThat(updated.id()).isEqualTo("id-1");
		assertThat(updated.name()).isEqualTo("Nequi Store SAS");
		assertThat(updated.branches()).isEqualTo(original.branches());
	}

}
