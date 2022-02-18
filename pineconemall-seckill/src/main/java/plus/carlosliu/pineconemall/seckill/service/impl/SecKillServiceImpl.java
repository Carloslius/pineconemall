package plus.carlosliu.pineconemall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import plus.carlosliu.common.constant.SecKillConstant;
import plus.carlosliu.common.to.MemberRespTo;
import plus.carlosliu.common.to.mq.SeckillOrderTo;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.seckill.feign.CouponFeignService;
import plus.carlosliu.pineconemall.seckill.feign.ProductFeignService;
import plus.carlosliu.pineconemall.seckill.interceptor.LoginUserInterceptor;
import plus.carlosliu.pineconemall.seckill.service.SecKillService;
import plus.carlosliu.pineconemall.seckill.to.SeckillSessionWithSkusTo;
import plus.carlosliu.pineconemall.seckill.to.SeckillSkuRedisTo;
import plus.carlosliu.pineconemall.seckill.to.SkuInfoTo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service("secKillService")
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public void uploadSecKillSkuLatest3Days() {
        R sessionsIn3Days = couponFeignService.getSecKillSessionsIn3Days();
        if (sessionsIn3Days.getCode() == 0){
            List<SeckillSessionWithSkusTo> sessionData = sessionsIn3Days.getData(new TypeReference<List<SeckillSessionWithSkusTo>>() {});
            if (sessionData != null) {
                // 上架商品
                // 1、缓存活动信息
                this.saveSessionInfo(sessionData);
                // 2、缓存活动的关联商品信息
                this.saveSkuInfo(sessionData);
            }
        }
    }
    private void saveSessionInfo(List<SeckillSessionWithSkusTo> sessionData) {
        sessionData.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SecKillConstant.SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> skuIds = session.getRelationSkus().stream().map(item ->
                        item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }
    private void saveSkuInfo(List<SeckillSessionWithSkusTo> sessionData) {
        sessionData.stream().forEach(session -> {
            // 准备hash操作
            BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(secKillSkuTo -> {
                Boolean hasKey = ops.hasKey(secKillSkuTo.getPromotionSessionId() + "_" + secKillSkuTo.getSkuId().toString());
                if (!hasKey) {
                    // 缓存商品
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    // 1、sku基本数据
                    R r = productFeignService.getSkuInfo(secKillSkuTo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>() {
                        });
                        redisTo.setSkuInfoTo(skuInfo);
                    }
                    // 2、sku秒杀信息
                    BeanUtils.copyProperties(secKillSkuTo, redisTo);
                    // 3、设置当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    // 4、商品的随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisTo.setRandomCode(token);
                    // 5、使用库存作为分布式信号量，限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(secKillSkuTo.getSeckillCount());

                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(secKillSkuTo.getPromotionSessionId() + "_" + secKillSkuTo.getSkuId().toString(), jsonString);
                }
            });
        });
    }


    public List<SeckillSkuRedisTo> blockHandlerSkus(BlockException e){
        log.error("getCurrentSecKillSkus方法被限流了");
        return null;
    }
    @SentinelResource(value = "getCurrentSecKillSkus", blockHandler = "blockHandlerSkus")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSecKillSkus() {
        Long nowTime = new Date().getTime();

        // 双重限流，外层先生效、内层后生效
        try (Entry secKillSkus = SphU.entry("secKillSkus")) {
            Set<String> keys = stringRedisTemplate.keys(SecKillConstant.SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys){
                String replace = key.replace(SecKillConstant.SESSIONS_CACHE_PREFIX, "");
                String[] times = replace.split("_");
                Long startTime = Long.parseLong(times[0]);
                Long endTime = Long.parseLong(times[1]);
                if (nowTime >= startTime && nowTime <= endTime){
                    // 获取当前场次需要的所有商品信息
                    List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
                    // 这里key不需要分割_
                    List<String> list = hashOps.multiGet(range);
                    if (list != null){
                        List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                            return JSON.parseObject(item, SeckillSkuRedisTo.class);
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        }catch (BlockException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSecKillSkuInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0){
            String regx = "\\d_" + skuId;
            for (String key : keys){
                if (Pattern.matches(regx, key)){
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    // 处理随机码
                    Long nowTime = new Date().getTime();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if (nowTime >= startTime && nowTime <= endTime){
                    }else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String code, Integer num) {
        MemberRespTo memberRespTo = LoginUserInterceptor.loginUser.get();
        // 1、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
        String skuInfo = hashOps.get(killId);
        if (!StringUtils.isEmpty(skuInfo)){
            SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfo, SeckillSkuRedisTo.class);
            // 1、校验时间合法性
            Long nowTime = new Date().getTime();
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            Long ttl = endTime - nowTime;
            if (nowTime >= startTime && nowTime <= endTime){
                // 2、校验随机码和商品id
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(code) && skuId.equals(killId)){
                    // 3、验证购物数量是否合理
                    if (num <= redisTo.getSeckillLimit()){
                        // 4、验证这个人是否购买过。幂等性：只要秒杀成功，就去占位。 userId_SessionId_skuId
                        String redisKey = memberRespTo.getId() + "_" + skuId;
                        // 自动过期
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean){
                            // 占位成功，从来没买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + randomCode);
                            Boolean b = semaphore.tryAcquire(num);
                            // 秒杀成功
                            // 快速下单。发送MQ消息
                            if (b) {
                                Integer uuid = UUID.randomUUID().toString().hashCode();
                                uuid = uuid < 0 ? -uuid : uuid; //String.hashCode() 值会为空
                                String timeId = uuid.toString();
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setOrderSn(timeId);
                                seckillOrderTo.setMemberId(memberRespTo.getId());
                                seckillOrderTo.setNum(num);
                                seckillOrderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                seckillOrderTo.setSkuId(redisTo.getSkuId());
                                seckillOrderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                seckillOrderTo.setRandomCode(randomCode);
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                return timeId;
                            }else {
                                return null;
                            }
                        }else {
                            // 占位失败，已经买过了
                            return null;
                        }
                    }else {
                        return null;
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
}
