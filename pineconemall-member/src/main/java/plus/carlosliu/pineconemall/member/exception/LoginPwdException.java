package plus.carlosliu.pineconemall.member.exception;

public class LoginPwdException extends RuntimeException{
    public LoginPwdException() {
        super("密码错误");
    }
}
