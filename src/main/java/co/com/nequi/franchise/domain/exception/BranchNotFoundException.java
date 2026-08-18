package co.com.nequi.franchise.domain.exception;

public class BranchNotFoundException extends DomainException {

	private final String branchId;

	public BranchNotFoundException(String branchId) {
		super("No existe la sucursal " + branchId);
		this.branchId = branchId;
	}

	public String branchId() {
		return branchId;
	}

}
