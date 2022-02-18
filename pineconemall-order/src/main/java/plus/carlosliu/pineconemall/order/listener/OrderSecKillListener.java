package plus.carlosliu.pineconemall.order.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plus.carlosliu.common.to.mq.SeckillOrderTo;
import plus.carlosliu.pineconemall.order.entity.OrderEntity;
import plus.carlosliu.pineconemall.order.service.OrderService;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSecKillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrder, Message message, Channel channel) throws IOException {
        System.out.println("准备创建秒杀订单的详细信息" + seckillOrder);
        try {
            orderService.createSecKillOrder(seckillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
