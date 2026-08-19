package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Branch;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class AddBranchUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public AddBranchUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Branch> execute(String franchiseId, String branchName) {
		return lookup.requireFranchise(franchiseId)
				.then(Mono.fromCallable(() -> Branch.create(branchName)))
				.flatMap(branch -> repository.saveBranch(franchiseId, branch));
	}

}
