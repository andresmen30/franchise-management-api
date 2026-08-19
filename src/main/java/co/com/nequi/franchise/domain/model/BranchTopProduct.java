package co.com.nequi.franchise.domain.model;

public record BranchTopProduct(String branchId, String branchName, Product product) {

	public static BranchTopProduct of(Branch branch, Product product) {
		return new BranchTopProduct(branch.id(), branch.name(), product);
	}

}
