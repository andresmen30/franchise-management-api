package co.com.nequi.franchise.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.com.nequi.franchise.domain.exception.BranchNotFoundException;

public record Franchise(String id, String name, List<Branch> branches) {

	public Franchise {
		id = DomainNames.require(id, "id de la franquicia");
		name = DomainNames.require(name, "nombre de la franquicia");
		branches = branches == null ? List.of() : List.copyOf(branches);
	}

	public static Franchise create(String name) {
		return new Franchise(UUID.randomUUID().toString(), name, List.of());
	}

	public Franchise withName(String newName) {
		return new Franchise(id, newName, branches);
	}

	public Optional<Branch> findBranch(String branchId) {
		return branches.stream().filter(branch -> branch.id().equals(branchId)).findFirst();
	}

	public Branch requireBranch(String branchId) {
		return findBranch(branchId).orElseThrow(() -> new BranchNotFoundException(branchId));
	}

}
