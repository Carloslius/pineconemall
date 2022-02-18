package plus.carlosliu.pineconemall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class MyFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                System.out.println("feign远程之前先进行RequestInterceptor.apply()");
                // RequestContextHolder拿到刚进来的请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest(); // 老请求
                    // 同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    // 给新请求同步了老请求的cookie
                    requestTemplate.header("Cookie", cookie);
                }
            }
        };
    }
}
