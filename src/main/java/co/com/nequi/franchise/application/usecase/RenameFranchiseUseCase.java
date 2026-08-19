package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class RenameFranchiseUseCase {

	private final FranchiseRepository repository;

	private final FranchiseLookup lookup;

	public RenameFranchiseUseCase(FranchiseRepository repository) {
		this.repository = repository;
		this.lookup = new FranchiseLookup(repository);
	}

	public Mono<Franchise> execute(String franchiseId, String newName) {
		return lookup.requireFranchise(franchiseId)
			.map(franchise -> franchise.withName(newName))
			.flatMap(repository::saveFranchise);
	}

}
