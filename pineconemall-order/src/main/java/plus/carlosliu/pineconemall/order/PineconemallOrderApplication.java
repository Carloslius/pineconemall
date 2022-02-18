package plus.carlosliu.pineconemall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景：RabbitAutoConfiguration 就会自动生效
 * 2、给容器中自动配置了 RabbitTemplate AmqpAdmin CachingConnectionFactory
 *          所有的配置都在这个类中
 *          @ConfigurationProperties(
 *              prefix = "spring.rabbitmq"
 *          )
 *          public class RabbitProperties {
 * 3、在配置文件中配置
 * 4、@EnableRabbit：开启功能
 * 5、监听消息：使用@RabbitListener；必须有@EnableRabbit
 *          @RabbitListener： 类+方法上（用来标识监听哪些队列）
 *          @RabbitHandler： 方法上（重载区分不同的消息）
 */
@EnableRedisHttpSession
@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class PineconemallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(PineconemallOrderApplication.class, args);
    }

}
