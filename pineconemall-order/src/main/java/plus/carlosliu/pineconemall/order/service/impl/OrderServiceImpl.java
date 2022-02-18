package plus.carlosliu.pineconemall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import plus.carlosliu.common.constant.OrderConstant;
import plus.carlosliu.common.constant.SecKillConstant;
import plus.carlosliu.common.exception.NoStockException;
import plus.carlosliu.common.to.MemberRespTo;
import plus.carlosliu.common.to.mq.OrderTo;
import plus.carlosliu.common.to.mq.SeckillOrderTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.order.dao.OrderDao;
import plus.carlosliu.pineconemall.order.entity.OrderEntity;
import plus.carlosliu.pineconemall.order.entity.OrderItemEntity;
import plus.carlosliu.pineconemall.order.entity.PaymentInfoEntity;
import plus.carlosliu.pineconemall.order.enume.OrderStatusEnum;
import plus.carlosliu.pineconemall.order.feign.CartFeignService;
import plus.carlosliu.pineconemall.order.feign.MemberFeignService;
import plus.carlosliu.pineconemall.order.feign.ProductFeignService;
import plus.carlosliu.pineconemall.order.feign.WareFeignService;
import plus.carlosliu.pineconemall.order.interceptor.LoginUserInterceptor;
import plus.carlosliu.pineconemall.order.service.OrderItemService;
import plus.carlosliu.pineconemall.order.service.OrderService;
import plus.carlosliu.pineconemall.order.service.PaymentInfoService;
import plus.carlosliu.pineconemall.order.to.*;
import plus.carlosliu.pineconemall.order.vo.*;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespTo memberRespTo = LoginUserInterceptor.loginUser.get();

        /**
         * 解决异步编排Feign丢失上下文问题：
         *      RequestContextHolder使用ThreadLocal实现共享，只要线程不一样，共享的就不一样
         */
        // 获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 每一个线程都来共享之前的请求，只要线程不一样，共享的就不一样
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1、远程查询所有的收货地址列表
            List<MemberAddressTo> address = memberFeignService.getAddress(memberRespTo.getId());
            orderConfirmVo.setMemberAddresses(address);
        }, executor);

        // feign在远程调用之前要构造请求，调用很多的拦截器
        // RequestInterceptor interceptor : requestInterceptors
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 每一个线程都来共享之前的请求，只要线程不一样，共享的就不一样
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2、远程查询购物车所有选择的购物项
            List<OrderItemTo> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            // 查询商品是否有货
            List<OrderItemTo> items = orderConfirmVo.getItems();
            List<Long> skuId = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            Map<Long, Boolean> skuHasStock = wareFeignService.getSkuHasStock(skuId);
            orderConfirmVo.setStocks(skuHasStock);
        });

        // 3、查询用户积分
        Integer integration = memberRespTo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        // 4、其他自动计算
        // 5、防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespTo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);

        try {
            CompletableFuture.allOf(getAddressFuture, cartFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return orderConfirmVo;
    }

    //@GlobalTransactional
    @Transactional // 本地事务
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        confirmVoThreadLocal.set(submitVo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespTo memberRespTo = LoginUserInterceptor.loginUser.get();
        // 1、验证令牌，保持原子性     0令牌失败、1删除成功
        Long execute = null;
        if (!submitVo.getIsSecKillOrder()){
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            execute = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                    Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespTo.getId()), submitVo.getOrderToken());
        }
        if (submitVo.getIsSecKillOrder() || (execute != null && execute.equals(1L))){
            // 令牌验证成功
            // 下单：去创建订单、验令牌、验价格、锁库存
            // 1、创建订单，订单项等信息
            OrderCreateTo order = this.createOrder(submitVo);
            // 2、验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            // TODO: 2022/2/16 秒杀订单的验价，前端无法传递运费
            if (Math.abs(payPrice.subtract(payAmount).doubleValue()) <= 0.01 || submitVo.getIsSecKillOrder()){
                // 3、保存订单
                this.saveOrder(order);
                // 4、库存锁定，只要有异常，回滚订单数据
                WareSkuLockTo lockTo = new WareSkuLockTo();
                lockTo.setOrderSn(order.getOrder().getOrderSn());
                // 封装需要锁定的商品信息
                List<OrderItemTo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemTo orderItemTo = new OrderItemTo();
                    orderItemTo.setSkuId(item.getSkuId());
                    orderItemTo.setCount(item.getSkuQuantity());
                    orderItemTo.setTitle(item.getSkuName());
                    return orderItemTo;
                }).collect(Collectors.toList());
                lockTo.setLocks(locks);
                R r = wareFeignService.orderLockStock(lockTo);
                if (r.getCode() == 0){
                    // 库存锁定成功
                    responseVo.setCode(0);
                    responseVo.setOrder(order.getOrder());
                    //int i = 10/0; //订单回滚，库存不滚
                    if (!submitVo.getIsSecKillOrder()) {
                        // 不是秒杀单，才会在这里发消息，秒杀单在对应的方法中发消息
                        OrderTo orderTo = new OrderTo();
                        BeanUtils.copyProperties(order.getOrder(), orderTo);
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderTo);
                    }
                    return responseVo;
                }else {
                    // 库存锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            }else {
                // 验价失败
                responseVo.setCode(2);
                return responseVo;
            }
        }else {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }
    }
    private OrderCreateTo createOrder(OrderSubmitVo submitVo){
        OrderCreateTo createTo = new OrderCreateTo();
        // 1、创建订单号
        String orderSn = IdWorker.getTimeId();
        // 2、构建订单
        OrderEntity orderEntity = this.buildOrder(orderSn);
        // 3、构建订单项
        List<OrderItemEntity> orderItemEntities = this.buildOrderItems(orderSn, submitVo);
        // 4、验价
        this.computePrice(orderEntity, orderItemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(orderItemEntities);
        return createTo;
    }
    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        // 1、设置订单号
        orderEntity.setOrderSn(orderSn);
        // 2、获取运费、收货地址、收货人信息
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        R fare = wareFeignService.getFare(submitVo.getAddrId());
        if (fare.getCode() == 0){
            FareVo fareData = fare.getData(new TypeReference<FareVo>(){});
            orderEntity.setFreightAmount(fareData.getFare());
            orderEntity.setReceiverProvince(fareData.getAddress().getProvince());
            orderEntity.setReceiverCity(fareData.getAddress().getCity());
            orderEntity.setReceiverRegion(fareData.getAddress().getRegion());
            orderEntity.setReceiverDetailAddress(fareData.getAddress().getDetailAddress());
            orderEntity.setReceiverName(fareData.getAddress().getName());
            orderEntity.setReceiverPhone(fareData.getAddress().getPhone());
            orderEntity.setReceiverPostCode(fareData.getAddress().getPostCode());
        }
        // 3、设置用户信息
        MemberRespTo memberRespTo = LoginUserInterceptor.loginUser.get();
        if (memberRespTo != null) {
            orderEntity.setMemberId(memberRespTo.getId());
            orderEntity.setMemberUsername(memberRespTo.getUsername());
        }
        // 4、设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setConfirmStatus(0);
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setCreateTime(new Date());

        return orderEntity;
    }
    private List<OrderItemEntity> buildOrderItems(String orderSn, OrderSubmitVo submitVo) {
        // 构建订单项
        List<OrderItemTo> currentUserCartItems = null;
        if (!submitVo.getIsSecKillOrder()) {
            currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        }else {
            SeckillOrderTo seckillOrderTo = submitVo.getSeckillOrderTo();
            String killId = seckillOrderTo.getPromotionSessionId() + "_" + seckillOrderTo.getSkuId();
            BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SecKillConstant.SKUKILL_CACHE_PREFIX);
            String skuInfo = hashOps.get(killId);
            if (!org.apache.commons.lang.StringUtils.isEmpty(skuInfo)) {
                SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfo, SeckillSkuRedisTo.class);
                OrderItemTo orderItemTo = new OrderItemTo();
                orderItemTo.setSkuId(redisTo.getSkuId());
                orderItemTo.setTitle(redisTo.getSkuInfoTo().getSkuTitle());
                orderItemTo.setImage(redisTo.getSkuInfoTo().getSkuDefaultImg());
                List<String> list = new ArrayList<>();
                list.add(redisTo.getSkuInfoTo().getSkuName());
                orderItemTo.setSkuAttrValues(list);
                orderItemTo.setPrice(redisTo.getSeckillPrice());
                orderItemTo.setCount(seckillOrderTo.getNum());
                orderItemTo.setTotalPrice(redisTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum())));
                currentUserCartItems = new ArrayList<>();
                currentUserCartItems.add(orderItemTo);
            }
        }
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = this.buildOrderItem(cartItem);
                // 设置所属订单号
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }
    private OrderItemEntity buildOrderItem(OrderItemTo cartItem) {
        // 构建单个订单项
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        Long skuId = cartItem.getSkuId();
        // 2) 设置sku相关属性
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";"));
        // 3) 通过skuId查询spu相关属性并设置
        R r = productFeignService.getSpuBySkuId(skuId);
        if (r.getCode() == 0) {
            SpuInfoTo spuInfo = r.getData(new TypeReference<SpuInfoTo>() {});
            orderItemEntity.setSpuId(spuInfo.getId());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
            orderItemEntity.setSpuBrand(spuInfo.getBrandName());
            orderItemEntity.setCategoryId(spuInfo.getCatalogId());
        }
        // 4) 商品的优惠信息(不做)

        // 5) 商品的积分成长，为价格x数量
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());

        // 6) 订单项订单价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        // 7) 实际价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // 相当于引用，修改形参orderEntity就是修改原来的对象
        //总价
        BigDecimal total = BigDecimal.ZERO;
        //优惠价格
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        //积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total = total.add(orderItemEntity.getRealAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //付款价格=商品价格+运费
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);
    }
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        baseMapper.insert(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return baseMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                .eq(orderSn != null, OrderEntity::getOrderSn, orderSn));
    }

    @Override
    public void closeOrder(OrderTo order) {
        //因为消息发送过来的订单已经是很久前的了，中间可能被改动，因此要查询最新的订单
        OrderEntity newOrderEntity = this.getById(order.getId());
        //如果订单还处于新创建的状态，说明超时未支付，进行关单
        if (Objects.equals(newOrderEntity.getStatus(), OrderStatusEnum.CREATE_NEW.getCode())) {
            newOrderEntity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(newOrderEntity);

            //关单后发送消息通知其他服务进行关单相关的操作，如解锁库存
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(newOrderEntity, orderTo);
            orderTo.setRandomCode(order.getRandomCode());
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn, String type) {
        PayVo payVo = new PayVo();
        OrderEntity order = null;
        if ("common".equals(type)) {
            order = this.getOrderByOrderSn(orderSn);
        }else if ("seckill".equals(type)){
            order = baseMapper.selectOne(new LambdaQueryWrapper<OrderEntity>()
                    .eq(orderSn != null, OrderEntity::getCouponId, Long.parseLong(orderSn)));
            orderSn = order.getOrderSn();
        }
        payVo.setOut_trade_no(orderSn);
        BigDecimal payAmount = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(payAmount.toString());
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new LambdaQueryWrapper<OrderItemEntity>().eq(orderSn != null, OrderItemEntity::getOrderSn, orderSn));
        OrderItemEntity orderItemEntity = orderItemEntities.get(0);
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils listOrderItems(Map<String, Object> params) {
        MemberRespTo memberRespTo = LoginUserInterceptor.loginUser.get();
        LambdaQueryWrapper<OrderEntity> queryWrapper =  new LambdaQueryWrapper<>();
        queryWrapper.eq(memberRespTo.getId() != null, OrderEntity::getMemberId, memberRespTo.getId())
                .orderByDesc(OrderEntity::getId);
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params), queryWrapper);
        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            LambdaQueryWrapper<OrderItemEntity> lqw = new LambdaQueryWrapper<>();
            lqw.eq(order.getOrderSn() != null, OrderItemEntity::getOrderSn, order.getOrderSn());
            List<OrderItemEntity> orderItemList = orderItemService.list(lqw);
            order.setItemEntities(orderItemList);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(collect);
        return new PageUtils(page);
    }

    @Override
    public void createSecKillOrder(SeckillOrderTo seckillOrder) {

        R r = memberFeignService.getMemberById(seckillOrder.getMemberId());
        if (r.getCode() == 0){
            MemberRespTo member = r.getData("member", new TypeReference<MemberRespTo>(){});
            LoginUserInterceptor.loginUser.set(member);
        }

        OrderSubmitVo orderSubmitVo = new OrderSubmitVo();
        List<MemberAddressTo> addressList = memberFeignService.getAddress(seckillOrder.getMemberId());
        if (addressList != null){
            List<MemberAddressTo> defaultAddress = addressList.stream().filter(address -> {
                return address.getDefaultStatus() == 1;
            }).collect(Collectors.toList());
            MemberAddressTo memberAddressTo = defaultAddress.get(0);
            Long addrId = memberAddressTo.getId();
            orderSubmitVo.setAddrId(addrId);
        }
        orderSubmitVo.setPayType(0);
        orderSubmitVo.setPayPrice(seckillOrder.getSeckillPrice());
        orderSubmitVo.setRemarks("");
        // 设置成true标记，不用验证token
        orderSubmitVo.setIsSecKillOrder(true);
        orderSubmitVo.setSeckillOrderTo(seckillOrder);

        SubmitOrderResponseVo responseVo = this.submitOrder(orderSubmitVo);
        OrderEntity order = responseVo.getOrder();
        Long couponId = Long.parseLong(seckillOrder.getOrderSn());
        order.setCouponId(couponId);
        baseMapper.updateById(order);

        // TODO: 2022/2/15 将信号量修改回去
        OrderTo orderTo = new OrderTo();
        BeanUtils.copyProperties(order, orderTo);
        orderTo.setRandomCode(seckillOrder.getRandomCode());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderTo);
    }

    @Override
    public void handPayResult(PayAsyncVo payAsyncVo) {
        //保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        String orderSn = payAsyncVo.getOut_trade_no();
        infoEntity.setOrderSn(orderSn);
        infoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        infoEntity.setTotalAmount(new BigDecimal(payAsyncVo.getBuyer_pay_amount()));
        infoEntity.setSubject(payAsyncVo.getSubject());
        String trade_status = payAsyncVo.getTrade_status();
        infoEntity.setPaymentStatus(trade_status);
        infoEntity.setCreateTime(new Date());
        infoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        infoEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        paymentInfoService.save(infoEntity);

        //判断交易状态是否成功
        if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
    }

}