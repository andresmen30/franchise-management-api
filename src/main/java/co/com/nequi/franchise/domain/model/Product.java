package co.com.nequi.franchise.domain.model;

import java.util.UUID;

import co.com.nequi.franchise.domain.exception.DomainValidationException;

public record Product(String id, String name, int stock) {

	public Product {
		id = DomainNames.require(id, "id del producto");
		name = DomainNames.require(name, "nombre del producto");
		if (stock < 0) {
			throw new DomainValidationException("El stock del producto no puede ser negativo");
		}
	}

	public static Product create(String name, int stock) {
		return new Product(UUID.randomUUID().toString(), name, stock);
	}

	public Product withName(String newName) {
		return new Product(id, newName, stock);
	}

	public Product withStock(int newStock) {
		return new Product(id, name, newStock);
	}

}
