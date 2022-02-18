package plus.carlosliu.pineconemall.ware.to;

import lombok.Data;

@Data
public class LockStockResultTo {

    private Long skuId;
    private Integer num;
    private Boolean locked;
}
