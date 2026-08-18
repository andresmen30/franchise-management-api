package co.com.nequi.franchise.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFranchiseRequest(@NotBlank @Size(max = 120) String name) {
}
