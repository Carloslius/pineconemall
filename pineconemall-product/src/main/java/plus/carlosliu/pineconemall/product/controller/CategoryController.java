package plus.carlosliu.pineconemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.w3c.dom.stylesheets.LinkStyle;
import plus.carlosliu.pineconemall.product.entity.CategoryEntity;
import plus.carlosliu.pineconemall.product.service.CategoryService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;



/**
 * 商品三级分类
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 00、查询类别具体信息，编辑类别时，数据回显
     * /product/category/info/{catId}
     */
    @GetMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
        CategoryEntity category = categoryService.getById(catId);
        return R.ok().put("data", category);
    }

    /**
     * 00、增加三级分类
     * /product/category/save
     */
    @PostMapping("/save")
    public R save(@RequestBody CategoryEntity category){
        categoryService.save(category);
        return R.ok();
    }

    /**
     * 00、批量删除类别信息
     * /product/category/delete
     * @RequestBody: 获取请求体，必须发送post请求
     * SpringMVC自动将请求体的数据（json），转为对应的对象
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody List<Long> catIds){
        categoryService.removeCascadeByIds(catIds);
        return R.ok();
    }

    /**
     * 00、修改三级分类
     * /product/category/update
     */
    @PostMapping("/update")
    public R update(@RequestBody CategoryEntity category){
        categoryService.updateCascadeById(category);
        return R.ok();
    }

    /**
     * 01、查出所有分类以及子分类，以树形结构组装起来
     * /product/category/list/tree
     */
    @GetMapping("/list/tree")
    public R list(){
        List<CategoryEntity> categoryEntities = categoryService.listWithTree();
        return R.ok().put("data", categoryEntities);
    }

    /**
     * 02、批量修改修改分类父子关系以及顺序
     * /product/category/update/sort
     */
    @PostMapping("/update/sort")
    public R updateBatch(@RequestBody List<CategoryEntity> category){
        categoryService.updateBatchById(category);
        return R.ok();
    }

}
