package plus.carlosliu.pineconemall.search.vo.web;

import lombok.Data;

import java.util.List;

@Data
public class SearchParams {

    /**
     * 全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：
     *      sort=saleCount_asc/desc
     *      sort=skuPrice_asc/desc
     *      sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 一堆过滤条件
     *      hasStock、skuPrice区间、brandId、catalog3Id、attrs
     *      hasStock=0/1
     *      skuPrice=1_500/_500/500_
     *      brandId=1
     *      attrs=2_5寸:6寸
     */
    private Integer hasStock; // 是否只显示有货
    private String skuPrice; // 价格区间查询
    private List<Long> brandId; // 按照品牌进行查询，可以多选
    private List<String> attrs; // 按照属性筛选

    private Integer pageNum;
    private String _queryString; // 原生的查询条件

}
