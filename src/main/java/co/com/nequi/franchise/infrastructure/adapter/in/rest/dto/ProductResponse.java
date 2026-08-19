package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import co.com.nequi.franchise.domain.model.Product;

public record ProductResponse(String id, String name, int stock) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(product.id(), product.name(), product.stock());
	}

}
