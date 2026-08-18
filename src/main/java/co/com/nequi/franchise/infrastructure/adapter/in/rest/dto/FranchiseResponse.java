package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import co.com.nequi.franchise.domain.model.Franchise;

public record FranchiseResponse(String id, String name) {

	public static FranchiseResponse from(Franchise franchise) {
		return new FranchiseResponse(franchise.id(), franchise.name());
	}

}
