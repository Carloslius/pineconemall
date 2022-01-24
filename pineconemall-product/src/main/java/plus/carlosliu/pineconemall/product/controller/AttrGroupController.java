package plus.carlosliu.pineconemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import plus.carlosliu.pineconemall.product.entity.AttrAttrgroupRelationEntity;
import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.entity.AttrGroupEntity;
import plus.carlosliu.pineconemall.product.service.AttrAttrgroupRelationService;
import plus.carlosliu.pineconemall.product.service.AttrGroupService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.product.service.AttrService;
import plus.carlosliu.pineconemall.product.service.CategoryService;


/**
 * 属性分组
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * /product/attrgroup/{attrgroupId}/attr/relation
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    // /product/attrgroup/attr/relation/delete
    @RequestMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R deleteRelation(@RequestBody AttrAttrgroupRelationEntity[] entities){
        attrGroupService.deleteRelation(entities);

        return R.ok();
    }

    // /product/attrgroup/{attrgroupId}/noattr/relation
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId, @RequestParam Map<String, Object> params){
        PageUtils pageUtils = attrService.getNoRelationAttr(attrgroupId, params);
        return R.ok().put("page", pageUtils);
    }

    // /product/attrgroup/attr/relation
    @PostMapping
    public R addRelation(@RequestBody List<AttrAttrgroupRelationEntity> entities){
        attrAttrgroupRelationService.saveBatch(entities);
        return R.ok();
    }


    /**
     * 03、获取分类属性分组
     * /product/attrgroup/list/{catelogId}
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@PathVariable("catelogId") Long categoryId, @RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(categoryId, params);
        return R.ok().put("page", page);
    }

    /**
     * 04、获取属性分组详情
     * /product/attrgroup/info/{attrGroupId}
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }
}
