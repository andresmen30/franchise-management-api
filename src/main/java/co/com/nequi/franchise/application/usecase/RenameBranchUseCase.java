package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class RenameBranchUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public RenameBranchUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Branch> execute(String franchiseId, String branchId, String newName) {
		return lookup.requireBranch(franchiseId, branchId)
			.map(branch -> branch.withName(newName))
			.flatMap(branch -> repository.saveBranch(franchiseId, branch));
	}

}
