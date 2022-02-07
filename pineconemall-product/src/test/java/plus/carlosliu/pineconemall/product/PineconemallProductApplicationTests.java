package plus.carlosliu.pineconemall.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import plus.carlosliu.pineconemall.product.entity.BrandEntity;
import plus.carlosliu.pineconemall.product.service.AttrGroupService;
import plus.carlosliu.pineconemall.product.service.BrandService;
import plus.carlosliu.pineconemall.product.service.SkuInfoService;
import plus.carlosliu.pineconemall.product.service.SkuSaleAttrValueService;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class PineconemallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandService.save(brandEntity);
    }

    @Test
    void test01(){
        List<SkuItemVo.SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(7L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }
    @Test
    void test02(){
        List<SkuItemVo.SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueService.getSaleAttrsBySpuId(5L);
        System.out.println(saleAttrsBySpuId);
    }
}
