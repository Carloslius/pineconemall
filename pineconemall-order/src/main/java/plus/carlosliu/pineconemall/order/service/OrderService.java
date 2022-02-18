package plus.carlosliu.pineconemall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.to.mq.OrderTo;
import plus.carlosliu.common.to.mq.SeckillOrderTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.order.entity.OrderEntity;
import plus.carlosliu.pineconemall.order.vo.*;

import java.util.Map;

/**
 * 订单
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:04:00
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要的数据
     * @return 订单确认页返回需要的数据
     */
    OrderConfirmVo confirmOrder();

    /**
     * 订单提交状态和数据
     * @param submitVo 页面提交的数据
     * @return 订单提交状态和数据
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    /**
     * 根据订单号获取订单
     * @param orderSn 订单号
     * @return 订单
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭订单
     * @param orderTo 订单信息
     */
    void closeOrder(OrderTo orderTo);

    /**
     * 根据订单号获取订单支付信息
     * @param orderSn 订单号
     * @param type 秒杀或正常
     * @return 订单支付信息
     */
    PayVo getOrderPay(String orderSn, String type);

    /**
     * 获取订单项详情
     * @param params 分页参数
     * @return 分页数据
     */
    PageUtils listOrderItems(Map<String, Object> params);

    /**
     * 创建秒杀订单
     * @param seckillOrder 秒杀订单的详细信息
     */
    void createSecKillOrder(SeckillOrderTo seckillOrder);

    /**
     * 处理支付宝支付结果
     * @param payAsyncVo 支付结果
     */
    void handPayResult(PayAsyncVo payAsyncVo);
}

