package plus.carlosliu.pineconemall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@EnableCaching
@EnableFeignClients(basePackages = "plus.carlosliu.pineconemall.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineconemallProductApplication.class, args);
    }

}
