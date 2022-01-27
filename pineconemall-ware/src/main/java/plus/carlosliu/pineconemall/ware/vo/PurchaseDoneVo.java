package plus.carlosliu.pineconemall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneVo {

    private Long id;
    private List<PurchaseDoneItemVo> items;
}
