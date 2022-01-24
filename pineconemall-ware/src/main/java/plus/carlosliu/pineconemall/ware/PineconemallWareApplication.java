package plus.carlosliu.pineconemall.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineconemallWareApplication.class, args);
    }

}
