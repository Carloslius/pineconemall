package plus.carlosliu.pineconemall.product.dao;

import org.apache.ibatis.annotations.Param;
import plus.carlosliu.pineconemall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 品牌分类关联
 * 
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:29
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    /**
     * 批量删除pms_category_brand_relation(类别和品牌关联表)中的对应关联关系
     * @param brandIds 批量删除的品牌id
     */
    void deleteBatchRelationByBrandIds(@Param("brandIds") List<Long> brandIds);

    /**
     * 批量删除pms_category_brand_relation(类别和品牌关联表)中的对应关联关系
     * @param catIds 批量删除的类别id
     */
    void deleteBatchRelationByCatIds(@Param("catIds") List<Long> catIds);
}
