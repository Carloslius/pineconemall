package plus.carlosliu.pineconemall.member.to;

import lombok.Data;

/**
 * 用以封装社交登录认证后换回的令牌等信息
 */
@Data
public class SocialUserTo {

    private String OAuthType;

    /**
     * 令牌
     */
    private String access_token;
    /**
     * 令牌过期时间
     */
    private long expires_in;

    /**
     * gitee
     */
    private String token_type;
    private String refresh_token;
    private String scope;
    private long created_at;


    /**
     * weibo
     */
    private String remind_in;
    private String uid;
    private String isRealName;
}
