package co.com.nequi.franchise.infrastructure.adapter.in.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import co.com.nequi.franchise.domain.exception.BranchNotFoundException;
import co.com.nequi.franchise.domain.exception.DomainException;
import co.com.nequi.franchise.domain.exception.DomainValidationException;
import co.com.nequi.franchise.domain.exception.FranchiseNotFoundException;
import co.com.nequi.franchise.domain.exception.ProductNotFoundException;
import co.com.nequi.franchise.domain.exception.RepositoryUnavailableException;
import reactor.core.publisher.Mono;

/**
 * No se registra un handler de {@code Exception}: en WebFlux atrapa tambien las senales de
 * cancelacion del cliente y las convertiria en respuestas de error.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final URI NOT_FOUND_TYPE = URI.create("urn:franchise-api:recurso-no-encontrado");

	private static final URI VALIDATION_TYPE = URI.create("urn:franchise-api:solicitud-invalida");

	private static final URI UNAVAILABLE_TYPE = URI.create("urn:franchise-api:dependencia-no-disponible");

	@ExceptionHandler({ FranchiseNotFoundException.class, BranchNotFoundException.class,
			ProductNotFoundException.class })
	ProblemDetail handleNotFound(DomainException exception, ServerWebExchange exchange) {
		return problem(HttpStatus.NOT_FOUND, NOT_FOUND_TYPE, "Recurso no encontrado", exception.getMessage(),
				codeFor(exception), exchange);
	}

	@ExceptionHandler(DomainValidationException.class)
	ProblemDetail handleDomainValidation(DomainValidationException exception, ServerWebExchange exchange) {
		return problem(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Solicitud invalida", exception.getMessage(),
				codeFor(exception), exchange);
	}

	@ExceptionHandler(RepositoryUnavailableException.class)
	ProblemDetail handleRepositoryUnavailable(RepositoryUnavailableException exception,
			ServerWebExchange exchange) {
		logger.error("El almacenamiento rechazo la operacion", exception);
		return problem(HttpStatus.SERVICE_UNAVAILABLE, UNAVAILABLE_TYPE, "Dependencia no disponible",
				"El almacenamiento no esta disponible en este momento", "REPOSITORY_UNAVAILABLE", exchange);
	}

	@Override
	protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(WebExchangeBindException exception,
			HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
		ProblemDetail body = problem(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Solicitud invalida",
				"Uno o mas campos de la solicitud no son validos", "VALIDATION_ERROR", exchange);
		List<Map<String, String>> errors = exception.getFieldErrors()
			.stream()
			.map(error -> Map.of("field", error.getField(), "message",
					error.getDefaultMessage() == null ? "valor invalido" : error.getDefaultMessage()))
			.toList();
		body.setProperty("errors", errors);
		return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
	}

	@Override
	protected Mono<ResponseEntity<Object>> createResponseEntity(Object body, HttpHeaders headers,
			HttpStatusCode status, ServerWebExchange exchange) {
		if (body instanceof ProblemDetail problem) {
			problem.setInstance(URI.create(exchange.getRequest().getPath().value()));
			if (problem.getProperties() == null || !problem.getProperties().containsKey("code")) {
				problem.setProperty("code", HttpStatus.valueOf(status.value()).name());
			}
		}
		return super.createResponseEntity(body, headers, status, exchange);
	}

	private static ProblemDetail problem(HttpStatus status, URI type, String title, String detail, String code,
			ServerWebExchange exchange) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(type);
		problem.setTitle(title);
		problem.setInstance(URI.create(exchange.getRequest().getPath().value()));
		problem.setProperty("code", code);
		return problem;
	}

	private static String codeFor(DomainException exception) {
		return switch (exception) {
			case FranchiseNotFoundException ignored -> "FRANCHISE_NOT_FOUND";
			case BranchNotFoundException ignored -> "BRANCH_NOT_FOUND";
			case ProductNotFoundException ignored -> "PRODUCT_NOT_FOUND";
			case DomainValidationException ignored -> "VALIDATION_ERROR";
			case RepositoryUnavailableException ignored -> "REPOSITORY_UNAVAILABLE";
			default -> "DOMAIN_ERROR";
		};
	}

}
