package plus.carlosliu.pineconemall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.carlosliu.pineconemall.order.config.AlipayTemplate;
import plus.carlosliu.pineconemall.order.service.OrderService;
import plus.carlosliu.pineconemall.order.vo.PayAsyncVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 异步接收支付宝成功回调
 */
@RestController
public class OrderPayedListener {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    @PostMapping("/payed/notify")
    public String handlerAlipay(HttpServletRequest request, PayAsyncVo payAsyncVo) throws AlipayApiException {
        System.out.println("收到支付宝异步通知******************");
        // 只要收到支付宝的异步通知，返回 success 支付宝便不再通知
        // 获取支付宝POST过来反馈信息 验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
             String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
            System.out.println("key=>>" + name + ";values==>" + Arrays.toString(values));
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if (signVerified){
            System.out.println("支付宝异步通知验签成功");
            //修改订单状态
            orderService.handPayResult(payAsyncVo);
            return "success";
        }else {
            System.out.println("支付宝异步通知验签失败");
            return "error";
        }
    }

    @GetMapping("/payed/test")
    public String test() {
        return "test";
    }
}
