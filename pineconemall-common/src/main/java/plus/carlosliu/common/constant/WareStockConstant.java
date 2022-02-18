package plus.carlosliu.common.constant;

public class WareStockConstant {

    public enum WareStockStatusEnum{
        LOCKED(1,"已锁定"),
        UNLOCKER(2,"已解锁"),
        SUCCESS(3,"已扣减");
        private int code;
        private String msg;

        WareStockStatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

}
