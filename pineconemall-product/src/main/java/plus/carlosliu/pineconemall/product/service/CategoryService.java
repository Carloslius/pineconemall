package plus.carlosliu.pineconemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;

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

    void removeMenuByIds(List<Long> idList);

    /**
     * 找到指定Id的类别的完整路径
     * [父/子/孙]
     * @param catelogId 类别Id
     * @return 三级分类完整路径
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascadeById(CategoryEntity category);
}

