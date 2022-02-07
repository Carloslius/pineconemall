package plus.carlosliu.pineconemall.search.vo.web;

import lombok.Data;
import plus.carlosliu.pineconemall.search.to.es.SkuEsModel;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {

    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前查询到的结果，所有涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询到的结果，所有涉及到的所有属性
     */
    private List<AttrVo> attrs;

    /**
     * 当前查询到的结果，所有涉及到的分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 面包屑导航
     */
    private List<NavVo> navs = new ArrayList<>();

    /**
     * 筛选过的属性id集合
     */
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
    @Data
    public static class NavVo{
        private Long navId;
        private String navName;
        private String navValue;
        private String link;
    }

}
