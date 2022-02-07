package plus.carlosliu.pineconemall.product.dao;

import org.apache.ibatis.annotations.Param;
import plus.carlosliu.pineconemall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:28
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
