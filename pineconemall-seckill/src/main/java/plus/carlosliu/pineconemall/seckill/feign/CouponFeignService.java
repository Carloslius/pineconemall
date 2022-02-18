package plus.carlosliu.pineconemall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import plus.carlosliu.common.utils.R;

@FeignClient(value = "pineconemall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/seckillsession/getSecKillSessionsIn3Days")
    R getSecKillSessionsIn3Days();
}
