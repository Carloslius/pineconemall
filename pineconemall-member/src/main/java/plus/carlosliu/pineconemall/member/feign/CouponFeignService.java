package plus.carlosliu.pineconemall.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import plus.carlosliu.common.utils.R;

@FeignClient("pineconemall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupon();
}
