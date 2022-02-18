package plus.carlosliu.pineconemall.auth.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import plus.carlosliu.common.to.MemberRespTo;
import plus.carlosliu.pineconemall.auth.constant.AuthServerConstant;
import plus.carlosliu.pineconemall.auth.feign.MemberFeignService;
import plus.carlosliu.pineconemall.auth.utils.HttpUtils;
import plus.carlosliu.pineconemall.auth.utils.R;
import plus.carlosliu.pineconemall.auth.vo.SocialUserVo;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 1、根据code换取accessToken
        Map<String, String> map = new HashMap<>();
        map.put("grant_type", "authorization_code");
        map.put("client_id", "0299285598d1046e3958ee2b5889ac3517e61b8d85808870534e185e6f8fbca6");
        map.put("redirect_uri", "http://auth.pineconemall.com/oauth2.0/gitee/success");
        map.put("client_secret", "addc32c5406be7dca29e7388f2dcb57538909f2c9f1c62d06e607ae2b5255847");
        map.put("code", code);

        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), null, map);
        // 2、处理
        if (response.getStatusLine().getStatusCode() == 200){
            // 获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUserVo socialUser = JSON.parseObject(json, SocialUserVo.class);

            // 2.1、知道当前是哪个社交用户
            // 2.1.1、如果当前用户是第一次进网站，自动注册进来(为当前社交用户生成一个会员信息账号)
            socialUser.setOAuthType(AuthServerConstant.GITEE_OAUTH);
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0){
                MemberRespTo data = oauthLogin.getData(new TypeReference<MemberRespTo>() {});
                System.out.println("登陆成功：用户信息：" + data);
                log.info("登陆成功：用户信息：{}", data.toString());
                // 1、将数据放入springsession
                //String jsonString = JSON.toJSONString(data);
                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                // 2、登陆成功就跳回首页
                return "redirect:http://pineconemall.com";
            }else {
                return "redirect:http://auth.pineconemall.com/login.html";
            }
        }else {
            return "redirect:http://auth.pineconemall.com/login.html";
        }
    }

    @GetMapping("/oauth2/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 1、根据code换取accessToken
        Map<String, String> query = new HashMap<>();
        query.put("client_id", "1962528733");
        query.put("client_secret", "8f318fa0d2e300de8dc0c5d5bae40821");
        query.put("grant_type", "authorization_code");
        // 如果不携带redirect_uri 报错 HTTP/1.1 400 Bad Request
        query.put("redirect_uri", "http://auth.pineconemall.com/oauth2/weibo/success");
        query.put("code", code);

        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<String, String>(), query, new HashMap<String, String>());
        // 2、处理
        if (response.getStatusLine().getStatusCode() == 200){
            // 获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUserVo socialUser = JSON.parseObject(json, SocialUserVo.class);

            // 2.1、知道当前是哪个社交用户
            // 2.1.1、如果当前用户是第一次进网站，自动注册进来(为当前社交用户生成一个会员信息账号)
            socialUser.setOAuthType(AuthServerConstant.WEIBO_OAUTH);
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0){
                MemberRespTo data = oauthLogin.getData(new TypeReference<MemberRespTo>() {});
                System.out.println("登陆成功：用户信息：" + data);
                log.info("登陆成功：用户信息：{}", data.toString());
                // 1、将数据放入springsession
                //String jsonString = JSON.toJSONString(data);
                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                // 2、登陆成功就跳回首页
                return "redirect:http://pineconemall.com";
            }else {
                return "redirect:http://auth.pineconemall.com/login.html";
            }
        }else {
            return "redirect:http://auth.pineconemall.com/login.html";
        }
    }
}
