package co.com.nequi.franchise.domain.exception;

public class ProductNotFoundException extends DomainException {

	private final String productId;

	public ProductNotFoundException(String productId) {
		super("No existe el producto " + productId);
		this.productId = productId;
	}

	public String productId() {
		return productId;
	}

}
