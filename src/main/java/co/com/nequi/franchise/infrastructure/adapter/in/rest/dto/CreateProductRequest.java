package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(@NotBlank @Size(max = 120) String name, @NotNull @Min(0) Integer stock) {
}
