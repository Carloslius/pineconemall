package plus.carlosliu.pineconemall.product.dao;

import org.apache.ibatis.annotations.Param;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

import java.util.List;

/**
 * 属性分组
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
