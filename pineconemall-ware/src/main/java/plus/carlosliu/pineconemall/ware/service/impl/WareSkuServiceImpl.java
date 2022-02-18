package plus.carlosliu.pineconemall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.constant.SecKillConstant;
import plus.carlosliu.common.constant.WareStockConstant;
import plus.carlosliu.common.enume.OrderStatusEnum;
import plus.carlosliu.common.exception.NoStockException;
import plus.carlosliu.common.to.SkuHasStockTo;
import plus.carlosliu.common.to.mq.OrderTo;
import plus.carlosliu.common.to.mq.StockDetailTo;
import plus.carlosliu.common.to.mq.StockLockedTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.ware.dao.WareSkuDao;
import plus.carlosliu.pineconemall.ware.entity.WareOrderTaskDetailEntity;
import plus.carlosliu.pineconemall.ware.entity.WareOrderTaskEntity;
import plus.carlosliu.pineconemall.ware.entity.WareSkuEntity;
import plus.carlosliu.pineconemall.ware.feign.OrderFeignService;
import plus.carlosliu.pineconemall.ware.feign.ProductFeignService;
import plus.carlosliu.pineconemall.ware.service.WareOrderTaskDetailService;
import plus.carlosliu.pineconemall.ware.service.WareOrderTaskService;
import plus.carlosliu.pineconemall.ware.service.WareSkuService;
import plus.carlosliu.pineconemall.ware.to.OrderItemTo;
import plus.carlosliu.pineconemall.ware.to.SecKillStockTo;
import plus.carlosliu.pineconemall.ware.to.WareSkuLockTo;

@Transactional
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        String skuId = (String) params.get("skuId");
        queryWrapper.eq(!StringUtils.isEmpty(skuId), WareSkuEntity::getSkuId, skuId);

        String wareId = (String) params.get("wareId");
        queryWrapper.eq(!StringUtils.isEmpty(wareId), WareSkuEntity::getWareId, wareId);

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(skuId != null, WareSkuEntity::getSkuId, skuId);
        queryWrapper.eq(wareId != null, WareSkuEntity::getWareId, wareId);
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(queryWrapper);
        if (wareSkuEntities == null || wareSkuEntities.isEmpty()){
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(skuNum);
            wareSku.setStockLocked(0);
            // 远程查询sku的名字，如果失败，整个事务无需回滚
            // 1、自己catch异常
            // 2、TODO：还可以用什么办法让异常出现以后不回滚？高级部分
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0){
                    wareSku.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
            }
            wareSkuDao.insert(wareSku);
        }else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> stockTos = skuIds.stream().map(skuId -> {
            SkuHasStockTo to = new SkuHasStockTo();
            Long count = baseMapper.getSkuStock(skuId);
            to.setSkuId(skuId);
            to.setHasStock(count != null && count > 0);
            return to;
        }).collect(Collectors.toList());
        return stockTos;
    }

    @Transactional
    @Override
    public void orderLockStock(WareSkuLockTo wareSkuLockTo) {
        // TODO: 2022/2/10 按照下单地址寻找就近仓库
        // 因为可能出现订单回滚后，库存锁定不回滚的情况，但订单已经回滚，得不到库存锁定信息，因此要有库存工作单
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockTo.getOrderSn());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);

        List<OrderItemTo> itemVos = wareSkuLockTo.getLocks();
        List<SkuLockTo> skuLockTos = itemVos.stream().map((item) -> {
            SkuLockTo skuLockTo = new SkuLockTo();
            skuLockTo.setSkuId(item.getSkuId());
            skuLockTo.setNum(item.getCount());
            // 找出所有库存大于商品数的仓库
            List<Long> wareIds = baseMapper.listWareIdsHasStock(item.getSkuId(), item.getCount());
            skuLockTo.setWareIds(wareIds);
            return skuLockTo;
        }).collect(Collectors.toList());

        List<StockLockedTo> lockedListTo = new ArrayList<>();
        for (SkuLockTo lockTo : skuLockTos) {
            Boolean lock = false;
            Long skuId = lockTo.getSkuId();
            List<Long> wareIds = lockTo.getWareIds();
            Integer num = lockTo.getNum();
            // 如果没有满足条件的仓库，抛出异常
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }else {
                for (Long wareId : wareIds) {
                    Long count = baseMapper.lockWareSku(skuId, num, wareId);
                    if (count == 0){
                        // 当前仓库锁失败，尝试下一个仓库
                        lock = false;
                    }else {
                        //锁定成功，保存工作单详情
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", num, taskEntity.getId(), wareId, 1);
                        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                        //发送库存锁定消息至延迟队列
                        StockLockedTo lockedTo = new StockLockedTo();
                        lockedTo.setId(taskEntity.getId());
                        StockDetailTo detailTo = new StockDetailTo();
                        BeanUtils.copyProperties(wareOrderTaskDetailEntity, detailTo);
                        lockedTo.setDetailTo(detailTo);
                        lockedListTo.add(lockedTo);

                        lock = true;
                        break;
                    }
                }
            }
            if (!lock) {
                throw new NoStockException(skuId);
            }
        }
        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedListTo);
    }

    @Data
    static class SkuLockTo{
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

    @Override
    public void unLockStocks(StockLockedTo to) {
        StockDetailTo detailTo = to.getDetailTo();
        Long detailId = detailTo.getId();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailId);
        // 解锁：查询数据库关于这个订单的锁定库存信息
        //      有：证明库存锁定成功了
        //          订单情况：
        //              1、没有这个订单。必须解锁
        //              2、有这个订单。
        //                  订单状态：已取消：解锁库存；没取消：不能解锁
        //      没有：库存锁定失败了，库存回滚了。无需解锁
        if (detailEntity != null){
            Long taskId = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = taskEntity.getOrderSn(); // 根据订单号查询订单状态
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0){
                OrderTo data = orderStatus.getData(new TypeReference<OrderTo>() {});
                if (data == null || Objects.equals(data.getStatus(), OrderStatusEnum.CANCLED.getCode())){
                    // 订单不存在
                    // 订单已经被取消了，才能解锁库存
                    if (detailEntity.getLockStatus() == WareStockConstant.WareStockStatusEnum.LOCKED.getCode()) {
                        this.unLockStock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailTo.getTaskId());
                    }
                }
            }else {
                throw new RuntimeException("远程调用订单服务失败");
            }
        }else {
            //无需解锁
        }
    }

    @Override
    public void unlockStocks(OrderTo orderTo) {
        //为防止重复解锁，需要重新查询工作单
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOne((new LambdaQueryWrapper<WareOrderTaskEntity>().eq(orderSn != null, WareOrderTaskEntity::getOrderSn, orderSn)));
        //查询出当前订单相关的且处于锁定状态的工作单详情
        Long taskId = taskEntity.getId();
        List<WareOrderTaskDetailEntity> lockDetails = wareOrderTaskDetailService.list(new LambdaQueryWrapper<WareOrderTaskDetailEntity>()
                .eq(taskId != null, WareOrderTaskDetailEntity::getTaskId, taskId)
                .eq(WareOrderTaskDetailEntity::getLockStatus, WareStockConstant.WareStockStatusEnum.LOCKED.getCode()));
        // 购物车订单
        for (WareOrderTaskDetailEntity lockDetail : lockDetails) {
            unLockStock(lockDetail.getSkuId(), lockDetail.getWareId(), lockDetail.getSkuNum(), lockDetail.getTaskId());
        }
        if (orderTo.getCouponId() != null) {
            // 秒杀订单
            String randomCode = orderTo.getRandomCode();
            RSemaphore semaphore = redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + randomCode);
            Integer skuNum = lockDetails.get(0).getSkuNum();
            semaphore.release(skuNum);
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        baseMapper.unLockStock(skuId, wareId, num);
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setLockStatus(WareStockConstant.WareStockStatusEnum.UNLOCKER.getCode());
        wareOrderTaskDetailService.update(entity, new LambdaQueryWrapper<WareOrderTaskDetailEntity>()
                .eq(taskDetailId != null, WareOrderTaskDetailEntity::getTaskId, taskDetailId)
                .eq(skuId != null, WareOrderTaskDetailEntity::getSkuId, skuId)
                .eq(wareId != null, WareOrderTaskDetailEntity::getWareId, wareId));
    }

}