package plus.carlosliu.pineconemall.member.exception;

public class LoginAcctException extends RuntimeException{
    public LoginAcctException() {
        super("用户名不存在");
    }
}
