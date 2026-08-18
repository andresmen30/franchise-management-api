package co.com.nequi.franchise.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.nequi.franchise.application.usecase.AddBranchUseCase;
import co.com.nequi.franchise.application.usecase.AddProductUseCase;
import co.com.nequi.franchise.application.usecase.CreateFranchiseUseCase;
import co.com.nequi.franchise.application.usecase.GetTopStockProductsUseCase;
import co.com.nequi.franchise.application.usecase.RemoveProductUseCase;
import co.com.nequi.franchise.application.usecase.UpdateProductStockUseCase;
import co.com.nequi.franchise.domain.port.out.FranchiseRepository;

@Configuration(proxyBeanMethods = false)
class UseCaseConfiguration {

	@Bean
	CreateFranchiseUseCase createFranchiseUseCase(FranchiseRepository repository) {
		return new CreateFranchiseUseCase(repository);
	}

	@Bean
	AddBranchUseCase addBranchUseCase(FranchiseRepository repository) {
		return new AddBranchUseCase(repository);
	}

	@Bean
	AddProductUseCase addProductUseCase(FranchiseRepository repository) {
		return new AddProductUseCase(repository);
	}

	@Bean
	RemoveProductUseCase removeProductUseCase(FranchiseRepository repository) {
		return new RemoveProductUseCase(repository);
	}

	@Bean
	UpdateProductStockUseCase updateProductStockUseCase(FranchiseRepository repository) {
		return new UpdateProductStockUseCase(repository);
	}

	@Bean
	GetTopStockProductsUseCase getTopStockProductsUseCase(FranchiseRepository repository) {
		return new GetTopStockProductsUseCase(repository);
	}

}
