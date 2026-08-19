package co.com.nequi.franchise.domain.exception;

public class BranchNotFoundException extends DomainException {

	public BranchNotFoundException(String branchId) {
		super("No existe la sucursal " + branchId);
	}

}
