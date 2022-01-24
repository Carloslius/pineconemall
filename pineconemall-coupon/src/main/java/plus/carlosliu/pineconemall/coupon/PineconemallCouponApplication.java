package plus.carlosliu.pineconemall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineconemallCouponApplication.class, args);
    }

}
