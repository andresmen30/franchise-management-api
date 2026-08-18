package co.com.nequi.franchise.domain.exception;

public class FranchiseNotFoundException extends DomainException {

	private final String franchiseId;

	public FranchiseNotFoundException(String franchiseId) {
		super("No existe la franquicia " + franchiseId);
		this.franchiseId = franchiseId;
	}

	public String franchiseId() {
		return franchiseId;
	}

}
