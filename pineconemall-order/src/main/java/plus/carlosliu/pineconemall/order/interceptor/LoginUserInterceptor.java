package plus.carlosliu.pineconemall.order.interceptor;


import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import plus.carlosliu.common.constant.AuthServerConstant;
import plus.carlosliu.common.to.MemberRespTo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespTo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match1 = antPathMatcher.match("/order/order/status/**", request.getRequestURI());
        //boolean match2 = antPathMatcher.match("/payed/**", request.getRequestURI());
        //System.out.println(request.getContextPath() + request.getRequestURI());
        //System.out.println(request.getRequestURL());
        if (match1){
            return true;
        }

        HttpSession session = request.getSession();
        MemberRespTo attribute = (MemberRespTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null){
            loginUser.set(attribute);
            return true;
        }else {
            // 没登陆去登陆
            session.setAttribute("orderMsg", "请先登录！");
            response.sendRedirect("http://auth.pineconemall.com/login.html");
            return false;
        }
    }
}
