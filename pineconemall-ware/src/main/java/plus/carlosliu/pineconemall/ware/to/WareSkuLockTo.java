package plus.carlosliu.pineconemall.ware.to;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockTo {

    private String orderSn;
    /**
     * 需要锁住的所有库存信息
     */
    private List<OrderItemTo> locks;
}
