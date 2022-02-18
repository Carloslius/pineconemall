package plus.carlosliu.pineconemall.member.controller;

import java.util.Arrays;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import plus.carlosliu.common.exception.BizCodeEnume;
import plus.carlosliu.pineconemall.member.entity.MemberEntity;
import plus.carlosliu.pineconemall.member.exception.LoginAcctException;
import plus.carlosliu.pineconemall.member.exception.LoginPwdException;
import plus.carlosliu.pineconemall.member.exception.PhoneNumExistException;
import plus.carlosliu.pineconemall.member.exception.UserExistException;
import plus.carlosliu.pineconemall.member.feign.CouponFeignService;
import plus.carlosliu.pineconemall.member.service.MemberService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.member.to.MemberRegisterTo;
import plus.carlosliu.pineconemall.member.to.MemberLoginTo;
import plus.carlosliu.pineconemall.member.to.SocialUserTo;


/**
 * 会员
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:08:15
 */
@Slf4j
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("刘浩松");
        R memberCoupon = couponFeignService.memberCoupon();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupon.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 注册用户信息
     * @param memberRegisterTo 注册信息
     * @return 成功
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterTo memberRegisterTo){
        try {
            memberService.register(memberRegisterTo);
        }catch (UserExistException e){
            log.error("注册异常：{}", e);
            return R.error(BizCodeEnume.USERNAME_EXIST_EXCEPTION.getCode(), BizCodeEnume.USERNAME_EXIST_EXCEPTION.getMsg());
        }catch (PhoneNumExistException e){
            log.error("注册异常：{}", e);
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 登录
     * @param memberLoginTo 登录信息
     * @return 成功
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginTo memberLoginTo){
        try {
            MemberEntity loginMember = memberService.login(memberLoginTo);
            return R.ok().setData(loginMember);
        }catch (LoginAcctException e){
            return R.error(BizCodeEnume.LOGINACCT_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_INVALID_EXCEPTION.getMsg());
        }catch (LoginPwdException e){
            return R.error(BizCodeEnume.LOGINPWD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINPWD_INVALID_EXCEPTION.getMsg());
        }
    }

    /**
     * 社交登录
     * @param socialUserTo 登录信息
     * @return 成功
     */
    @PostMapping("/oauthLogin")
    public R oauthLogin(@RequestBody SocialUserTo socialUserTo){
        try {
            MemberEntity member = memberService.oauthLogin(socialUserTo);
            return R.ok().setData(member);
        }catch (LoginAcctException e){
            return R.error(BizCodeEnume.LOGINACCT_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_INVALID_EXCEPTION.getMsg());
        }catch (LoginPwdException e){
            return R.error(BizCodeEnume.LOGINPWD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINPWD_INVALID_EXCEPTION.getMsg());
        }catch (Exception e) {
            e.printStackTrace();
            return R.error();
        }
    }


}
