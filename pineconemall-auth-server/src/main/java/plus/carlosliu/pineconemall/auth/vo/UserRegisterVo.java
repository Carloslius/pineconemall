package plus.carlosliu.pineconemall.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserRegisterVo {

    @NotBlank(message = "用户名不能为空")
    @Size(min=6, max=17, message = "用户名长度在6—17字符")
    private String username;

    @NotBlank(message = "密码必须填写")
    @Size(min=6, max=17, message = "密码必须是6—17字符")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;

}