package co.com.nequi.franchise.domain.exception;

/**
 * El almacenamiento no pudo atender la operacion. Representa un fallo de la dependencia, no una
 * regla de negocio incumplida, por lo que reintentar la misma peticion puede tener exito.
 */
public class RepositoryUnavailableException extends DomainException {

	public RepositoryUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
