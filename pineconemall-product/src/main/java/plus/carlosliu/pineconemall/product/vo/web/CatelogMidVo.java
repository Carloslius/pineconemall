package plus.carlosliu.pineconemall.product.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CatelogMidVo {

    private String catalog1Id; // 一级父分类id
    private List<CatelogBaseVo> catalog3List; // 三级子分类
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class CatelogBaseVo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
