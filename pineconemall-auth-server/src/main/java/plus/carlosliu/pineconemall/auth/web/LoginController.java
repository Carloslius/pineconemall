package plus.carlosliu.pineconemall.auth.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import plus.carlosliu.common.to.MemberRespTo;
import plus.carlosliu.pineconemall.auth.constant.AuthServerConstant;
import plus.carlosliu.pineconemall.auth.exception.BizCodeEnum;
import plus.carlosliu.pineconemall.auth.feign.MemberFeignService;
import plus.carlosliu.pineconemall.auth.feign.ThirdPartFeignService;
import plus.carlosliu.pineconemall.auth.utils.R;
import plus.carlosliu.pineconemall.auth.vo.UserLoginVo;
import plus.carlosliu.pineconemall.auth.vo.UserRegisterVo;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class LoginController {

    /**
     * 发送一个请求直接跳转到一个页面：
     *      SpringMVC viewcontroller：将请求和页面映射过来
     */

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/login.html")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/register.html")
    public String registerPage(){
        return "register";
    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){

        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String prePhone = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;
        String redisCode = ops.get(prePhone);
        if (!StringUtils.isEmpty(redisCode)) {
            long pre = Long.parseLong(redisCode.split("_")[1]);
            //如果存储的时间小于60s，说明60s内发送过验证码
            if (System.currentTimeMillis() - pre < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //如果存在的话，删除之前的验证码
        redisTemplate.delete(prePhone);
        //获取到6位数字的验证码
        String code = UUID.randomUUID().toString().substring(0, 6);
        //在redis中进行存储并设置过期时间
        ops.set(prePhone,code+"_"+System.currentTimeMillis(),10, TimeUnit.MINUTES);
        R r = thirdPartFeignService.sendCode(phone, code, "5");
        if (r.getCode() == 0) {
            log.info("发送成功:{}", code);
        }
        return R.ok();
    }

//    @ResponseBody
//    @PostMapping("/regist")
//    public String register(@Valid UserRegisterVo registerVo, BindingResult result, RedirectAttributes attributes) {
//        //1.判断校验是否通过
//        Map<String, String> errors = new HashMap<>();
//        if (result.hasErrors()){
//            //1.1 如果校验不通过，则封装校验结果
//            result.getFieldErrors().forEach(item->{
//                errors.put(item.getField(), item.getDefaultMessage());
//                //1.2 将错误信息封装到session中
//            });
//            // 重定向携带数据利用session，只要跳到下一个页面，取出数据后，session里面的数据就会被删除
//            attributes.addFlashAttribute("errors", errors);
//            //1.2 重定向到注册页，防止表单重复提交，重定向以/拼接当前路径，需要指定不以/开头的绝对路径
//            return "redirect:http://auth.pineconemall.com/register.html";
//        }else {
//            //2.若JSR303校验通过
//            //判断验证码是否正确
//            String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
//            //2.1 如果对应手机的验证码不为空且与提交上的相等 ==>> 验证码正确
//            if (!StringUtils.isEmpty(code) && registerVo.getCode().equals(code.split("_")[0])) {
//                //2.1.1 使得验证后的验证码失效，令牌机制
//                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
//
//                //2.1.2 远程调用会员服务注册
//                R r = memberFeignService.register(registerVo);
//                if (r.getCode() == 0) {
//                    //调用成功，重定向登录页
//                    System.out.println("登陆成功");
//                    return "redirect:http://auth.pineconemall.com/login.html";
//                }else {
//                    //调用失败，返回注册页并显示错误信息
//                    String msg = (String) r.get("msg");
//                    errors.put("msg", msg);
//                    attributes.addFlashAttribute("errors", errors);
//                    return "redirect:http://auth.pineconemall.com/register.html";
//                }
//            }else {
//                //2.2 验证码错误
//                errors.put("code", "验证码错误");
//                attributes.addFlashAttribute("errors", errors);
//                return "redirect:http://auth.pineconemall.com/register.html";
//            }
//        }
//    }

    @ResponseBody
    @PostMapping("/regist")
    public String register(@Valid @RequestBody UserRegisterVo registerVo, BindingResult result) {
        //1.判断校验是否通过
        Map<String, String> errors = new HashMap<>();
        if (result.hasErrors()){
            //1.1 如果校验不通过，则封装校验结果
            result.getFieldErrors().forEach(item->{
                errors.put(item.getField(), item.getDefaultMessage());
            });
            return JSON.toJSONString(errors);
        }else {
            //2.若JSR303校验通过
            //判断验证码是否正确
            String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
            //2.1 如果对应手机的验证码不为空且与提交上的相等 ==>> 验证码正确
            if (!StringUtils.isEmpty(code) && registerVo.getCode().equals(code.split("_")[0])) {
                //2.1.1 使得验证后的验证码失效，令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());

                //2.1.2 远程调用会员服务注册
                R r = memberFeignService.register(registerVo);
                if (r.getCode() == 0) {
                    //调用成功，重定向登录页
                    System.out.println("注册成功");
                    return "true";
                }else {
                    //调用失败，返回注册页并显示错误信息
                    String msg = (String) r.get("msg");
                    log.info("注册会员服务调用失败：{}", msg);
                    errors.put("msg", msg);
                    return JSON.toJSONString(errors);
                }
            }else {
                //2.2 验证码错误
                errors.put("code", "验证码错误");
                return JSON.toJSONString(errors);
            }
        }
    }

    @ResponseBody
    @PostMapping("/login")
    public String login(@RequestBody UserLoginVo userLoginVo, HttpSession session){
        R login = memberFeignService.login(userLoginVo);
        if (login.getCode() == 0){
            MemberRespTo data = login.getData(new TypeReference<MemberRespTo>() {});
            // 配置Session配置类后，千万不要转成JSON在保存到session中
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return JSON.toJSONString("true");
            //return "redirect:http://pineconemall.com";
        }else {
            String msg = (String) login.get("msg");
            //Map<String, String> errors = new HashMap<>();
            //errors.put("msg", msg);
            //redirectAttributes.addFlashAttribute("errors", errors);
            //return "redirect:http://auth.pineconemall.com/login.html";
            return JSON.toJSONString(msg);
        }
    }

}
