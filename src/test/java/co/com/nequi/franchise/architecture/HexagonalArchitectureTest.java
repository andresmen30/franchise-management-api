package co.com.nequi.franchise.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class HexagonalArchitectureTest {

	private static final String ROOT = "co.com.nequi.franchise";

	private static final String DOMAIN = "..domain..";

	private static final String APPLICATION = "..application..";

	private static final String INFRASTRUCTURE = "..infrastructure..";

	private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages(ROOT);

	@Test
	void elDominioNoDependeDeLaInfraestructura() {
		noClasses().that()
			.resideInAPackage(DOMAIN)
			.should()
			.dependOnClassesThat()
			.resideInAPackage(INFRASTRUCTURE)
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void elDominioNoDependeDeLosCasosDeUso() {
		noClasses().that()
			.resideInAPackage(DOMAIN)
			.should()
			.dependOnClassesThat()
			.resideInAPackage(APPLICATION)
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void losCasosDeUsoNoDependenDeLaInfraestructura() {
		noClasses().that()
			.resideInAPackage(APPLICATION)
			.should()
			.dependOnClassesThat()
			.resideInAPackage(INFRASTRUCTURE)
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void elDominioYLosCasosDeUsoIgnoranASpring() {
		noClasses().that()
			.resideInAnyPackage(DOMAIN, APPLICATION)
			.should()
			.dependOnClassesThat()
			.resideInAPackage("org.springframework..")
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void elDominioYLosCasosDeUsoIgnoranElSdkDeAws() {
		noClasses().that()
			.resideInAnyPackage(DOMAIN, APPLICATION)
			.should()
			.dependOnClassesThat()
			.resideInAPackage("software.amazon.awssdk..")
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void elDominioSoloDependeDelJdkYDeReactor() {
		classes().that()
			.resideInAPackage(DOMAIN)
			.should()
			.onlyDependOnClassesThat()
			.resideInAnyPackage(DOMAIN, "java..", "reactor..")
			.because("el puerto expresa su contrato en tipos Reactor; cualquier otra dependencia "
					+ "externa acoplaria el dominio a un detalle de infraestructura")
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void elAdaptadorRestNoConoceElAdaptadorDePersistencia() {
		noClasses().that()
			.resideInAPackage("..adapter.in.rest..")
			.should()
			.dependOnClassesThat()
			.resideInAPackage("..adapter.out..")
			.check(PRODUCTION_CLASSES);
	}

	@Test
	void noExistenCiclosEntreLasCapas() {
		slices().matching(ROOT + ".(*)..").should().beFreeOfCycles().check(PRODUCTION_CLASSES);
	}

}
