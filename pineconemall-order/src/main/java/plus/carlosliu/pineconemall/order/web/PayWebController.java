package plus.carlosliu.pineconemall.order.web;

import com.alipay.api.AlipayApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import plus.carlosliu.pineconemall.order.config.AlipayTemplate;
import plus.carlosliu.pineconemall.order.service.OrderService;
import plus.carlosliu.pineconemall.order.vo.PayVo;

@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 将支付页让浏览器展示
     * 支付成功以后，我们要跳到用户的订单列表页
     * @param orderSn 订单号
     * @return 支付页
     */
    @ResponseBody
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    public String aliPayOrder(@RequestParam("orderSn") String orderSn, @RequestParam("type") String type){
        PayVo payVo = orderService.getOrderPay(orderSn, type);
        String pay = null;
        try {
            pay = alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return pay;
    }

}
