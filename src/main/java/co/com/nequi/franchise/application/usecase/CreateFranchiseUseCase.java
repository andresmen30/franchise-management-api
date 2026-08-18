package co.com.nequi.franchise.application.usecase;

import co.com.nequi.franchise.domain.model.Franchise;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;
import reactor.core.publisher.Mono;

public class CreateFranchiseUseCase {

	private final FranchiseRepository repository;

	public CreateFranchiseUseCase(FranchiseRepository repository) {
		this.repository = repository;
	}

	public Mono<Franchise> execute(String name) {
		return Mono.fromCallable(() -> Franchise.create(name)).flatMap(repository::saveFranchise);
	}

}
