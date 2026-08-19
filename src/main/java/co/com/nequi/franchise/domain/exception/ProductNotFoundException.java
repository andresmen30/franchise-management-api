package co.com.nequi.franchise.domain.exception;

public class ProductNotFoundException extends DomainException {

	public ProductNotFoundException(String productId) {
		super("No existe el producto " + productId);
	}

}
