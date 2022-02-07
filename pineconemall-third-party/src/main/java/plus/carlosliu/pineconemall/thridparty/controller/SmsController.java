package plus.carlosliu.pineconemall.thridparty.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plus.carlosliu.pineconemall.thridparty.component.SmsComponent;
import plus.carlosliu.pineconemall.thridparty.utils.R;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用
     * @param phone 手机号
     * @param code 验证码
     * @param minute 分钟
     * @return 成功/失败
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone,
                      @RequestParam("code") String code,
                      @RequestParam("minute") String minute){
        smsComponent.sendSms(phone, code, minute);
        return R.ok();
    }

}
