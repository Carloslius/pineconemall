package plus.carlosliu.pineconemall.seckill.schedule;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import plus.carlosliu.pineconemall.seckill.service.SecKillService;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 */
@Service
public class SecKillSchedule {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SecKillService secKillService;

    //秒杀商品上架功能的锁
    private final String upload_lock = "seckill:upload:lock";

    /**
     * 定时任务
     * 每天三点上架最近三天的秒杀商品
     */
    @Async
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //为避免分布式情况下多服务同时上架的情况，使用分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            // 幂等性处理，锁的业务执行完成，状态已经更新完成。释放锁以后，其他人获取到就会拿到最新状态。
            secKillService.uploadSecKillSkuLatest3Days();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
