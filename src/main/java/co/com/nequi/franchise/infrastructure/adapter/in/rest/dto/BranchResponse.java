package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import co.com.nequi.franchise.domain.model.Branch;

public record BranchResponse(String id, String name) {

	public static BranchResponse from(Branch branch) {
		return new BranchResponse(branch.id(), branch.name());
	}

}
