package plus.carlosliu.pineconemall.product.dao;

import org.apache.ibatis.annotations.Param;
import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;

/**
 * 属性&属性分组关联
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelation(@Param("entities") AttrAttrgroupRelationEntity[] entities);
}
