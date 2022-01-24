package plus.carlosliu.pineconemall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineconemallOrderApplication.class, args);
    }

}
