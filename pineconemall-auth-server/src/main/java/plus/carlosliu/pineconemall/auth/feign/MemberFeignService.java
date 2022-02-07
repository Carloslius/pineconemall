package plus.carlosliu.pineconemall.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import plus.carlosliu.pineconemall.auth.utils.R;
import plus.carlosliu.pineconemall.auth.vo.SocialUserVo;
import plus.carlosliu.pineconemall.auth.vo.UserLoginVo;
import plus.carlosliu.pineconemall.auth.vo.UserRegisterVo;

@FeignClient("pineconemall-member")
public interface MemberFeignService {


    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo serRegisterVo);


    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);


    @PostMapping("/member/member/oauthLogin")
    R oauthLogin(@RequestBody SocialUserVo socialUserVo) throws Exception;
}
