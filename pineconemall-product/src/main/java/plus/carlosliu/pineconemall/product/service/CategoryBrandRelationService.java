package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:29
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取分类关联的品牌
     * @param catId 类别id
     * @return 类别对应的品牌信息集合
     */
    List<CategoryBrandRelationEntity> brandList(Long catId);

    /**
     * 获取品牌关联的分类
     * @param brandId 品牌id
     * @return 品牌对应的类别信息集合
     */
    List<CategoryBrandRelationEntity> catelogList(Long brandId);

    /**
     * 新增品牌与分类关联关系
     * @param categoryBrandRelation 品牌与分类关联关系信息
     */
    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    /**
     * 级联修改pms_category_brand_relation(类别和品牌关联表)中品牌名
     * @param brandId 需要修改的品牌id
     * @param name 要修改的名字结果
     */
    void updateBrandNameCascade(Long brandId, String name);

    /**
     * 级联修改pms_category_brand_relation(类别和品牌关联表)中类别名
     * @param catId 需要修改的类别id
     * @param name 要修改的名字结果
     */
    void updateCategoryNameCascade(Long catId, String name);
}

