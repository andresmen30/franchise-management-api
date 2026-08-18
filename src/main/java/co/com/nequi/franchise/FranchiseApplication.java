package co.com.nequi.franchise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FranchiseApplication {

	public static void main(String[] args) {
		SpringApplication.run(FranchiseApplication.class, args);
	}

}
