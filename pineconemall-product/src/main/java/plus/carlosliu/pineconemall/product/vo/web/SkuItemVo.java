package plus.carlosliu.pineconemall.product.vo.web;

import lombok.Data;
import plus.carlosliu.pineconemall.product.entity.SkuImagesEntity;
import plus.carlosliu.pineconemall.product.entity.SkuInfoEntity;
import plus.carlosliu.pineconemall.product.entity.SpuInfoDescEntity;
import plus.carlosliu.pineconemall.product.to.SecKillInfoTo;

import java.util.List;

@Data
public class SkuItemVo {

    // 1、sku基本信息获取
    private SkuInfoEntity skuInfo;
    // 2、sku的图片信息
    private List<SkuImagesEntity> images;
    // 3、获取spu的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttrs;
    // 4、获取spu的介绍
    private SpuInfoDescEntity desc;
    // 5、获取spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;

    // 6、当前商品的秒杀优惠信息
    private SecKillInfoTo secKillInfo;

    private Boolean hasStock = false;

    @Data
    public static class SkuItemSaleAttrVo{
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }
    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }
    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }
    @Data
    public static class AttrValueWithSkuIdVo{
        private String attrValue;
        private String skuIds;
    }
}
