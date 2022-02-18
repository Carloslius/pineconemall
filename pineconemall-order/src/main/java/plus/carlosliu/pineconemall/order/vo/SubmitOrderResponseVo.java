package plus.carlosliu.pineconemall.order.vo;

import lombok.Data;
import plus.carlosliu.pineconemall.order.entity.OrderEntity;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /** 错误状态码 **/
    private Integer code;
}
