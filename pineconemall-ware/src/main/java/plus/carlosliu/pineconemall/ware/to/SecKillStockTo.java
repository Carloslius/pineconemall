package plus.carlosliu.pineconemall.ware.to;

import lombok.Data;
import plus.carlosliu.common.to.mq.StockLockedTo;

import java.util.List;

@Data
public class SecKillStockTo {

    private Boolean isSecKillOrder;
    private List<StockLockedTo> tos;
}
