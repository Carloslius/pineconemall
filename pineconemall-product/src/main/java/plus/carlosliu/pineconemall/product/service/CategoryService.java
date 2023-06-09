package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import plus.carlosliu.pineconemall.product.vo.web.CatelogMidVo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取所有分类以及子分类，并返回json树形结构
     * @return 返回json树形结构
     */
    List<CategoryEntity> listWithTree();

    /**
     * 批量删除类别信息，级联删除分类与品牌关联表中的数据
     * @param catIds 需要批量删除的类别id集合
     */
    void removeCascadeByIds(List<Long> catIds);

    /**
     * 修改三级分类
     * @param category 三级分类信息
     */
    void updateCascadeById(CategoryEntity category);

    /**
     * 找到指定Id的类别的完整路径
     * [父/子/孙]
     * @param catelogId 类别Id
     * @return 三级分类完整路径
     */
    Long[] findCatelogPath(Long catelogId);



    /**
     * 首页显示：查询所有的一级分类
     * @return 所有的一级分类
     */
    List<CategoryEntity> getTopLevelCategories();

    /**
     * 查出所有分类，按照要求组织
     * @return 查出所有分类，按照要求组织
     */
    Map<String, List<CatelogMidVo>> getCatalogJson();
}

