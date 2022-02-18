package plus.carlosliu.pineconemall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import plus.carlosliu.common.constant.AuthServerConstant;
import plus.carlosliu.pineconemall.member.constant.OAuthConstant;
import plus.carlosliu.pineconemall.member.exception.LoginAcctException;
import plus.carlosliu.pineconemall.member.exception.LoginPwdException;
import plus.carlosliu.pineconemall.member.to.*;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.member.dao.MemberDao;
import plus.carlosliu.pineconemall.member.entity.MemberEntity;
import plus.carlosliu.pineconemall.member.entity.MemberLevelEntity;
import plus.carlosliu.pineconemall.member.exception.PhoneNumExistException;
import plus.carlosliu.pineconemall.member.exception.UserExistException;
import plus.carlosliu.pineconemall.member.service.MemberLevelService;
import plus.carlosliu.pineconemall.member.service.MemberService;
import plus.carlosliu.pineconemall.member.utils.HttpUtils;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterTo memberRegisterTo) {
        MemberEntity memberEntity = new MemberEntity();

        checkUserNameUnique(memberRegisterTo.getUsername());
        memberEntity.setUsername(memberRegisterTo.getUsername());

        checkPhoneUnique(memberRegisterTo.getPhone());
        memberEntity.setMobile(memberRegisterTo.getPhone());

        MemberLevelEntity defaultMemberLevel = memberLevelService.getOne(new LambdaQueryWrapper<MemberLevelEntity>().eq(MemberLevelEntity::getDefaultStatus, 1));
        memberEntity.setLevelId(defaultMemberLevel.getId());

        memberEntity.setNickname(memberRegisterTo.getUsername());
        memberEntity.setIntegration(0);
        memberEntity.setGrowth(0);
        memberEntity.setStatus(1);
        memberEntity.setCreateTime(new Date());

        //密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(memberRegisterTo.getPassword());
        memberEntity.setPassword(password);

        baseMapper.insert(memberEntity);
    }
    private void checkUserNameUnique(String userName) {
        Integer count = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName));
        if (count > 0) {
            throw new UserExistException();
        }
    }
    private void checkPhoneUnique(String phone) {
        Integer count = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone));
        if (count > 0) {
            throw new PhoneNumExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginTo memberLoginTo) {
        String loginAcct = memberLoginTo.getLoginAcct();
        String loginPassword = memberLoginTo.getLoginPassword();
        LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(loginAcct != null, MemberEntity::getUsername, loginAcct)
                .or().eq(loginAcct != null, MemberEntity::getMobile, loginAcct);
        MemberEntity member = baseMapper.selectOne(queryWrapper);
        if (member != null){
            // 获取到数据库的password
            String passwordDB = member.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(loginPassword, passwordDB);
            if (matches){
                return member;
            }else {
                throw new LoginPwdException();
            }
        }else {
            throw new LoginAcctException();
        }
    }

    @Override
    public MemberEntity oauthLogin(SocialUserTo socialUserTo) throws Exception {
        String oAuthType = socialUserTo.getOAuthType();
        if (AuthServerConstant.GITEE_OAUTH.equals(oAuthType)){
            return this.oauthLoginGitee(socialUserTo);
        }else if (AuthServerConstant.WEIBO_OAUTH.equals(oAuthType)){
            return this.oauthLoginWeibo(socialUserTo);
        }else {
            throw new LoginAcctException();
        }
    }
    private MemberEntity oauthLoginGitee(SocialUserTo socialUserTo) throws Exception {
        // 登录和注册功能合并
        String access_token = socialUserTo.getAccess_token();
        Long expires_in = socialUserTo.getExpires_in();

        Map<String, String> map = new HashMap<>();
        map.put("access_token", access_token);
        HttpResponse response = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "post", new HashMap<>(), map);
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            GiteeSocialUserInfo giteeSocialUserInfo = JSON.parseObject(json, GiteeSocialUserInfo.class);
            String uid = giteeSocialUserInfo.getId();

            MemberEntity memberEntity = baseMapper.selectOne(new LambdaQueryWrapper<MemberEntity>().eq(uid != null, MemberEntity::getSocialUid, uid));
            if (memberEntity != null) {
                // 这个用户已经注册过，覆盖redis中的access_token
                stringRedisTemplate.opsForValue().set(OAuthConstant.GITEE_OAUTH_PREFIX + "access_token_" + uid, access_token);
                stringRedisTemplate.opsForValue().set(OAuthConstant.GITEE_OAUTH_PREFIX + "expires_in_" + uid, expires_in.toString());
                return memberEntity;
            } else {
                // 没有注册过，需要进行注册
                MemberEntity member = new MemberEntity();
                stringRedisTemplate.opsForValue().set(OAuthConstant.GITEE_OAUTH_PREFIX + "access_token_" + uid, access_token);
                stringRedisTemplate.opsForValue().set(OAuthConstant.GITEE_OAUTH_PREFIX + "expires_in_" + uid, expires_in.toString());
                MemberLevelEntity defaultLevel = memberLevelService.getOne(new LambdaQueryWrapper<MemberLevelEntity>().eq(MemberLevelEntity::getDefaultStatus, 1));
                member.setLevelId(defaultLevel.getId());
                member.setSocialUid(giteeSocialUserInfo.getId());
                member.setUsername(giteeSocialUserInfo.getLogin());
                member.setNickname(giteeSocialUserInfo.getName());
                member.setHeader(giteeSocialUserInfo.getAvatar_url());
                member.setEmail(giteeSocialUserInfo.getEmail());
                member.setIntegration(0);
                member.setGrowth(0);
                member.setStatus(1);
                member.setCreateTime(new Date());
                baseMapper.insert(member);
                return member;
            }
        }else {
            throw new LoginAcctException();
        }
    }
    private MemberEntity oauthLoginWeibo(SocialUserTo socialUserTo) throws Exception {
        // 登录和注册功能合并
        String access_token = socialUserTo.getAccess_token();
        Long expires_in = socialUserTo.getExpires_in();
        String uid = socialUserTo.getUid();

        Map<String, String> query = new HashMap<>();
        query.put("access_token", access_token);
        query.put("uid", uid);
        HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            WeiboSocialUserInfo weiboSocialUserInfo = JSON.parseObject(json, WeiboSocialUserInfo.class);

            MemberEntity memberEntity = baseMapper.selectOne(new LambdaQueryWrapper<MemberEntity>().eq(uid != null, MemberEntity::getSocialUid, uid));
            if (memberEntity != null) {
                // 这个用户已经注册过，覆盖redis中的access_token
                stringRedisTemplate.opsForValue().set(OAuthConstant.WEIBO_OAUTH_PREFIX + "access_token_" + uid, access_token);
                stringRedisTemplate.opsForValue().set(OAuthConstant.WEIBO_OAUTH_PREFIX + "expires_in_" + uid, expires_in.toString());
                return memberEntity;
            } else {
                // 没有注册过，需要进行注册
                MemberEntity member = new MemberEntity();
                stringRedisTemplate.opsForValue().set(OAuthConstant.WEIBO_OAUTH_PREFIX + "access_token_" + uid, access_token);
                stringRedisTemplate.opsForValue().set(OAuthConstant.WEIBO_OAUTH_PREFIX + "expires_in_" + uid, expires_in.toString());
                MemberLevelEntity defaultLevel = memberLevelService.getOne(new LambdaQueryWrapper<MemberLevelEntity>().eq(MemberLevelEntity::getDefaultStatus, 1));
                member.setLevelId(defaultLevel.getId());
                member.setSocialUid(weiboSocialUserInfo.getId()+"");
                member.setUsername(weiboSocialUserInfo.getScreen_name());
                member.setNickname(weiboSocialUserInfo.getName());
                member.setHeader(weiboSocialUserInfo.getProfile_image_url());
                member.setIntegration(0);
                member.setGrowth(0);
                member.setStatus(1);
                member.setCreateTime(new Date());
                baseMapper.insert(member);
                return member;
            }
        }else {
            throw new LoginAcctException();
        }
    }

}