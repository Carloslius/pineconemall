package plus.carlosliu.pineconemall.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import plus.carlosliu.common.utils.R;

import java.util.Map;

@FeignClient("pineconemall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    R memberCoupon();

}
