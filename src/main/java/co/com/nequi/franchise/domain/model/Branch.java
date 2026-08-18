package co.com.nequi.franchise.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.com.nequi.franchise.domain.exception.ProductNotFoundException;

public record Branch(String id, String name, List<Product> products) {

	private static final Comparator<Product> HIGHEST_STOCK_FIRST =
			Comparator.comparingInt(Product::stock).reversed().thenComparing(Product::name);

	public Branch {
		id = DomainNames.require(id, "id de la sucursal");
		name = DomainNames.require(name, "nombre de la sucursal");
		products = products == null ? List.of() : List.copyOf(products);
	}

	public static Branch create(String name) {
		return new Branch(UUID.randomUUID().toString(), name, List.of());
	}

	public Branch withName(String newName) {
		return new Branch(id, newName, products);
	}

	public Optional<Product> findProduct(String productId) {
		return products.stream().filter(product -> product.id().equals(productId)).findFirst();
	}

	public Product requireProduct(String productId) {
		return findProduct(productId).orElseThrow(() -> new ProductNotFoundException(productId));
	}

	public Optional<Product> productWithHighestStock() {
		return products.stream().min(HIGHEST_STOCK_FIRST);
	}

}
