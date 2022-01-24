package plus.carlosliu.pineconemall.thridparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallThridPartyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PineconemallThridPartyApplication.class, args);
	}

}
