package plus.carlosliu.pineconemall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     *
     * 1、服务收到消息就回调
     *      1.1、spring.rabbitmq.publisher-confirms = true
     *      1.2、设置确认回调 ConfirmCallback
     * 2、消息正确抵达队列进行回调
     *      2.1、spring.rabbitmq.publisher-returns = true
     *          spring.rabbitmq.template.mandatory = true
     *      2.2、设置确认回调 ReturnCallback
     *
     * 3、消费端确认（保证每个消息被正确消费，此时才broker才可以删除这个消息）
     *      spring.rabbitmq.listener.simple.acknowledge-mode = manual  手动签收
     *      3.1、默认是自动确认的，只要消息接收到，客户端会自动确认
     *          问题：
     *              我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了，会发生消息丢失
     *          消费者手动确认模式：
     *              只要我们没有明确告诉MQ，货物没有签收，没有ack，消息就一直是unacked状态。
     *              即使Consumer宕机，消息不会丢失，会重新变为ready，下一次有新的Consumer连接进来就会发给他
     *      3.2、如何签收
     *          channel.basicAck(deliveryTag, false); 签收，业务成功完成
     *          channel.basicNack(deliveryTag, false, true); 拒签，业务失败未完成
     */
    @PostConstruct // MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate(){
        // 设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            /**
             * 只要消息抵达broker就ack=true
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
        });

        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message 投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange 当时这个消息发送给哪个交换机
             * @param routingKey 当时这个消息用的哪个路由键
             */
            // TODO: 2022/2/14 消息重发
        });
    }
}
