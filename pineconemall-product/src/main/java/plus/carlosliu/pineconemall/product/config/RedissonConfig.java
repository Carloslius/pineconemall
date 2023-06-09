package plus.carlosliu.pineconemall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 整合redisson-spring-boot-starter后不用手动配置Redisson
 */
@Configuration
public class RedissonConfig {
    //@Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://39.103.189.107:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}