package plus.carlosliu.pineconemall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.pineconemall.member.to.MemberLoginTo;
import plus.carlosliu.pineconemall.member.to.MemberRegisterTo;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.member.entity.MemberEntity;
import plus.carlosliu.pineconemall.member.to.SocialUserTo;

import java.util.Map;

/**
 * 会员
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:08:15
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 注册用户信息
     * @param memberRegisterTo 注册信息
     */
    void register(MemberRegisterTo memberRegisterTo);

    /**
     * 用户登录
     * @param memberLoginTo 登录信息
     * @return 成功返回用户信息，失败抛出对应异常
     */
    MemberEntity login(MemberLoginTo memberLoginTo);

    /**
     * 用户登录，社交认证，具有登录和注册合并功能
     * @param socialUserTo 登录信息
     */
    MemberEntity oauthLogin(SocialUserTo socialUserTo) throws Exception;
}

