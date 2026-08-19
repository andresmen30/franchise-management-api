package co.com.nequi.franchise.domain.exception;

public class FranchiseNotFoundException extends DomainException {

	public FranchiseNotFoundException(String franchiseId) {
		super("No existe la franquicia " + franchiseId);
	}

}
