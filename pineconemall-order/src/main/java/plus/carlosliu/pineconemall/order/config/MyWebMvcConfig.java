package plus.carlosliu.pineconemall.order.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import plus.carlosliu.pineconemall.order.interceptor.LoginUserInterceptor;

@Controller
public class MyWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/confirm.html").setViewName("confirm");
        registry.addViewController("/detail.html").setViewName("detail");
        registry.addViewController("/list.html").setViewName("list");
        registry.addViewController("/pay.html").setViewName("pay");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**").excludePathPatterns("/payed/**");
    }
}
