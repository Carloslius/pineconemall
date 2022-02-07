package plus.carlosliu.pineconemall.product.app;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import plus.carlosliu.pineconemall.product.entity.CategoryBrandRelationEntity;
import plus.carlosliu.pineconemall.product.service.CategoryBrandRelationService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:29
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }


    /**
     * 14、获取分类关联的品牌
     * /product/categorybrandrelation/brands/list
     */
    @GetMapping("/brands/list")
    public R brandListByCatelogId(@RequestParam("catId") Long catId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.brandList(catId);
        return R.ok().put("data", data);
    }

    /**
     * 15、获取品牌关联的分类
     * /product/categorybrandrelation/catelog/list
     */
    @GetMapping("/catelog/list")
    public R catelogListByBrandId(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.catelogList(brandId);
        return R.ok().put("data", data);
    }

    /**
     * 16、新增品牌与分类关联关系
     * product/categorybrandrelation/save
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
        categoryBrandRelationService.saveDetail(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 00、删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody List<Long> ids){
        categoryBrandRelationService.removeByIds(ids);
        return R.ok();
    }

}
