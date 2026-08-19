package co.com.nequi.franchise.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DynamoDbPropertiesTest {

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	void sinEndpointResuelveContraAwsReal(String endpoint) {
		assertThat(new DynamoDbProperties("franchises", "us-east-1", endpoint, false).usesLocalEndpoint()).isFalse();
	}

	@Test
	void conEndpointApuntaALaInstanciaLocal() {
		assertThat(new DynamoDbProperties("franchises", "us-east-1", "http://localhost:8000", false)
			.usesLocalEndpoint()).isTrue();
	}

}
