package plus.carlosliu.pineconemall.seckill.service;

import plus.carlosliu.pineconemall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SecKillService {

    /**
     * 定时任务
     * 每天三点上架最近三天的秒杀商品
     */
    void uploadSecKillSkuLatest3Days();

    /**
     * 返回当前时间可以参与秒杀的商品信息
     * @return 当前时间可以参与秒杀的商品信息
     */
    List<SeckillSkuRedisTo> getCurrentSecKillSkus();

    /**
     * 获取秒杀商品的预告信息
     * @param skuId 要查询的商品id
     * @return 预告信息
     */
    SeckillSkuRedisTo getSecKillSkuInfo(Long skuId);

    /**
     * 秒杀商品
     * @param killId 场次id_商品id
     * @param code 秒杀商品的随机码
     * @param num 秒杀的数量
     * @return 生成的消息单号
     */
    String kill(String killId, String code, Integer num);
}
