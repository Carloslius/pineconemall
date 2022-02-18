package plus.carlosliu.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {

    /**
     * 库存工作单id
     */
    private Long id;

    private StockDetailTo detailTo;
}
