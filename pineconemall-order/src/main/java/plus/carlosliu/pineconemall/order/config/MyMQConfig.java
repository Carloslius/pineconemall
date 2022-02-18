package plus.carlosliu.pineconemall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {

    @Bean
    public Queue orderCreateOrderQueue(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order-event-exchange");
        args.put("x-dead-letter-routing-key", "order.release.order");
        args.put("x-message-ttl", 300000L);
        return new Queue("order.delay.queue", true, false, false, args);
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.order", null);
    }

    /**
     * 与库存解锁队列绑定，order.release.order.#包括order.release.order，topic广播
     * @return 绑定关系
     */
    @Bean
    public Binding orderReleaseStockBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.other.#", null);
    }


    @Bean
    public Queue orderSecKillOrderQueue(){
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean
    public Binding orderSecKillBinding(){
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.seckill.order", null);
    }

}
