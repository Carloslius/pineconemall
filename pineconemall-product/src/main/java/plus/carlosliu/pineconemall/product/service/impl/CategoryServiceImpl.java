package plus.carlosliu.pineconemall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.Query;

import plus.carlosliu.pineconemall.product.dao.CategoryDao;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import plus.carlosliu.pineconemall.product.service.CategoryBrandRelationService;
import plus.carlosliu.pineconemall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void removeMenuByIds(List<Long> idList) {
        // TODO 检查当前要删除的菜单是否被别的地方引用
        baseMapper.deleteBatchIds(idList);
    }

    @Override
    public void updateCascadeById(CategoryEntity category) {
        baseMapper.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            categoryBrandRelationService.updateCategoryNameCascade(category.getCatId(), category.getName());
        }
    }



    @Override
    public List<CategoryEntity> listWithTree() {
        // 查询所有类别
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 1、找到所有的一级分类
        List<CategoryEntity> baseCategories = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(0L))
                .map(categories -> {
                    // 2、找到并拼装子菜单
                    categories.setChildren(this.getChildren(categories, categoryEntities));
                    return categories;
                })
                .sorted((categories1, categories2) -> {
                    // 3、菜单的排序
                    return (categories1.getSort()!=null?categories1.getSort():0) - (categories2.getSort()!=null?categories2.getSort():0);
                })
                .collect(Collectors.toList());
        return baseCategories;
    }
    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){
        List<CategoryEntity> childCategories = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categories -> {
                    // 1、找到并拼装子菜单
                    categories.setChildren(this.getChildren(categories, all));
                    return categories;
                })
                .sorted((categories1, categories2) -> {
                    // 2、菜单的排序
                    return (categories1.getSort()!=null?categories1.getSort():0) - (categories2.getSort()!=null?categories2.getSort():0);
                })
                .collect(Collectors.toList());
        return childCategories;
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = this.findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1、搜集当前节点id
        paths.add(catelogId);
        CategoryEntity entity = this.getById(catelogId);
        if (entity.getParentCid() != 0){
            // 2、递归查询父节点类别id
            this.findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }

}