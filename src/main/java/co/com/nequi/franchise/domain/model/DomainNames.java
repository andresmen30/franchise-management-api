package co.com.nequi.franchise.domain.model;

import co.com.nequi.franchise.domain.exception.DomainValidationException;

final class DomainNames {

	// Acota el tamano del item persistido; DynamoDB rechaza items por encima de 400 KB.
	private static final int MAX_LENGTH = 120;

	private DomainNames() {
	}

	static String require(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new DomainValidationException("El campo " + field + " es obligatorio");
		}
		String trimmed = value.trim();
		if (trimmed.length() > MAX_LENGTH) {
			throw new DomainValidationException(
					"El campo " + field + " no puede superar " + MAX_LENGTH + " caracteres");
		}
		return trimmed;
	}

}
