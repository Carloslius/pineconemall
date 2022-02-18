package plus.carlosliu.pineconemall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.order.to.MemberAddressTo;

import java.util.List;

@FeignClient("pineconemall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressTo> getAddress(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/member/info/{id}")
    R getMemberById(@PathVariable("id") Long id);
}
