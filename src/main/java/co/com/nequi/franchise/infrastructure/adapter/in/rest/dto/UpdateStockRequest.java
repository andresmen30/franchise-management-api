package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(@NotNull @Min(0) Integer stock) {
}
