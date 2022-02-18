package plus.carlosliu.pineconemall.ware.listener;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plus.carlosliu.common.to.mq.OrderTo;
import plus.carlosliu.common.to.mq.StockLockedTo;
import plus.carlosliu.pineconemall.ware.service.WareSkuService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(List<StockLockedTo> tos, Message message, Channel channel){
        log.info("************************收到库存解锁的消息********************************");
        String s = JSON.toJSONString(tos);
        List<StockLockedTo> lockedToList = JSON.parseArray(s, StockLockedTo.class);
        Boolean flag = false;
        for (StockLockedTo to : lockedToList) {
            try {
                wareSkuService.unLockStocks(to);
                flag = true;
            } catch (Exception e) {
                flag = false;
            }
        }
        if (flag){
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @RabbitHandler
    public void handleStockLockedRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("************************从订单模块收到库存解锁的消息********************************");
        try {
            wareSkuService.unlockStocks(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
