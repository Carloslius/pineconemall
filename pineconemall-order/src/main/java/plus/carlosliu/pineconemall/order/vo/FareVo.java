package plus.carlosliu.pineconemall.order.vo;

import lombok.Data;
import plus.carlosliu.pineconemall.order.to.MemberAddressTo;

import java.math.BigDecimal;

@Data
public class FareVo {

    private MemberAddressTo address;

    private BigDecimal fare;
}
