package plus.carlosliu.pineconemall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineconemallProductApplication.class, args);
    }

}
