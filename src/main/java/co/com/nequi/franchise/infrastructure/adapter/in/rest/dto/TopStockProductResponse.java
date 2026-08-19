package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import co.com.nequi.franchise.domain.model.BranchTopProduct;

public record TopStockProductResponse(String branchId, String branchName, String productId, String productName,
		int stock) {

	public static TopStockProductResponse from(BranchTopProduct top) {
		return new TopStockProductResponse(top.branchId(), top.branchName(), top.product().id(),
				top.product().name(), top.product().stock());
	}

}
