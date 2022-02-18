package plus.carlosliu.pineconemall.ware.vo;

import lombok.Data;
import plus.carlosliu.pineconemall.ware.to.MemberAddressTo;

import java.math.BigDecimal;

@Data
public class FareVo {

    private MemberAddressTo address;
    private BigDecimal fare;
}
