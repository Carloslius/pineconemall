package plus.carlosliu.pineconemall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pineconemall.thread")
@Data
public class ThreadPoolConfigProperties {
    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;

}
