package plus.carlosliu.pineconemall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import plus.carlosliu.pineconemall.order.vo.PayVo;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000119614046";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCjQEtji2J2EJzMyyHcmUkvMmSOZeXzYAQGIFqqsrqfIKhcZABePIbmrrXoEcP7L6u97Llx/omQuaap3IPH7XjQt0RgoHfr72Veghhgz0VIh1vEG3GoJJs/AxTP2UcnrfD1RS7FgEuNON3t3kvj2dKyjp7tgujciI/rG/J6e2YrL+DWh6DzXrJcmz+gyH4m86H7ne1SDyEPQ0Zr9LqdGo12KFdJPjdhzBRLuPUm3uCnRlkQm0iRW6oCV9f7toDxAP06B7lYs9zesDQOjfLLhNSfzTvDZ0pfcDvveSwdw9ViacdUIJP1tAOX9YkOLMcCJgg8a0QO2EqGR8SYOrRWgQPXAgMBAAECggEAQcJhW8zGsaZJcxkUH8dDOpbC+LVFJn7zwAACZaYvFHBUQTsrBsq80Glp7vxRBHqUZKHZpXiRs1GFOAESuFMJCpH4IjAhflFldOdUqJJ1ZxzXMyyW4NM2EfDMzljIl8wyNqpSh84BIPbaNroHudWBUNmwXR4RbNw5lMwiJYJVimlLji78pb5kQvm/6PSUd3mymj8ORllLErM2PHnXayDM9vExwQpt0PVht1ZR4Cl7KwQXl5AHeC7Sq+AKTubPH7imOkk8OLJ81arOJEwRjc2efOvoV0YQjH4pm33anTJoJvHhDcWGHnDoscfCN+5HgQ4VmpGrecICUM1oOIE6Ti3yqQKBgQD72WxJx0aje+JZXkYUG7NWn2FtF4hdUNhSs8veqlpmE9Nx2PeX2IXJE3CEnnKux+J3KNDofMI3JkRzbbzdHXNxk3dm4YE0oMcthjihsiCD/jHzXbazIStkrXqX8mdALHtBNQwedzuZ/NU4sCmFJ9e8kpjQYAev79MzDWDqzta88wKBgQCl8REwZSwQTL2kfs1/IpdOzatdkQBz0YTORXL1Ie0Psi2OnbTEVXgwlYDMZprElqhZdSIFAELhx96KX/DVou7DBedK6Xvbc7RyyunD29TcA/ty4ScBtu+IS7Tqsr73+YHf/XGmxDuUbrLF2AKVeram47tfLbWihZ+a5rMWRU3GjQKBgDrgXGFvFcN4XbUYxfdDdoyEnAkd3EI8eSX0ZG+8kX+VmPPfjhpQgw473i0Swaq8jePfhd8j819jMdNuTx2+GdPubpZ9l3APOiEZZLngY5uX3GORf/mmgnd52tQU1jB5daML3LZul19rbQlgnYREiTnLZ7AnEwT9YvlnYkXibu8jAoGAWw1DWUKqQudYx4GsUC4yx9KiTNzbWrGEJYqF4WuUICCsTqzKG1Cqej4ORhmmomfoR+21lPjnuetpeJPcGd0lFW2P09L5WxjJ0pL0YbuluHOW7RoNxSsTk722r3EUyAvn73epfM//wRpcjaJRCKQkxL4AeeEi4OJhCSW87mNooaECgYEArTcpm4p2o0xWgolxlt9hbe9SaBX6dsAxY3VaTgI6tq1i3am2WDTjvNgCf0Ds/kpjoRVXL6+DpWURa3hE3MKSrkwFf7Ugd1S3u4b2InzMJkUE/plHUrr8UFFL8rQKonjDTMLyGUbrFIkzJJgR0xLa4shJq6M5b3Lxk6uiJYv3SEg=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnx5JBt9KDb+xQbYn7M5NVgjPBVHgl1is2MNv12or/5i+Pj/KHot84uIDkidQ/E4jXBhA3bvucCnIgjaVBf4X6Jh3W0pPZ0a1WlH01HXPUo3BXJHYp5+qceb+eVaPPr2bRTWvzaoQDKnP1N/3d8JRdxmUTs9UmtSsMMGnJMs9wxRpMjKPF9P/xGzoTJ/OlyFWVETQjqZ1Jlri/WiQsmh5ISvs0VSz6nrWzIdM2VOS8Gl1Zg1THmdS7b0hPK7XmzR2V4FtiSXKpj7Ma07Ir9xemAY8/lruwLd9MhSqxZc5uI4iZtEZClXHKK849ZsKrsy4VgHIm/F6aP96RNnWexYRwQIDAQAB";
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://570f4be5.nat123.fun/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.pineconemall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 超时时间
    private String timeout = "5m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                +"\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
