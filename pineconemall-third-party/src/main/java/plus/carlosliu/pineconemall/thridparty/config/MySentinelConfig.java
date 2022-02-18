package plus.carlosliu.pineconemall.thridparty.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Configuration;
import plus.carlosliu.common.exception.BizCodeEnume;
import plus.carlosliu.common.utils.R;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class MySentinelConfig implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        // BlockException 异常接口，其子类为Sentinel五种规则异常的实现类
        // AuthorityException 授权异常
        // DegradeException 降级异常
        // FlowException 限流异常
        // ParamFlowException 参数限流异常
        // SystemBlockException 系统负载异常
        String msg = null;
        if (e instanceof FlowException) {
            msg = "服务限流";
        } else if (e instanceof DegradeException) {
            msg = "服务降级";
        } else if (e instanceof ParamFlowException) {
            msg = "热点参数限流";
        } else if (e instanceof SystemBlockException) {
            msg = "系统规则（负载/...不满足要求）";
        } else if (e instanceof AuthorityException) {
            msg = "授权规则不通过";
        }

        R error = R.error(BizCodeEnume.TOO_MANY_REQUEST.getCode(), BizCodeEnume.TOO_MANY_REQUEST.getMsg()+msg);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(error));
    }
}
