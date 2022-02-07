package plus.carlosliu.pineconemall.product.app;

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
import plus.carlosliu.pineconemall.product.vo.AttrGroupRespVo;


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
     * 级联删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody List<Long> attrGroupIds){
		attrGroupService.removeCascadeByIds(attrGroupIds);
        return R.ok();
    }

    /**
     * 03、获取分类属性分组
     * /product/attrgroup/list/{catelogId}
     */
    @GetMapping("/list/{catelogId}")
    public R list(@PathVariable("catelogId") Long categoryId, @RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(categoryId, params);
        return R.ok().put("page", page);
    }

    /**
     * 04、获取属性分组详情
     * /product/attrgroup/info/{attrGroupId}
     */
    @GetMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 10、获取属性分组的关联的所有属性
     * /product/attrgroup/{attrgroupId}/attr/relation
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrEntities);
    }

    /**
     * 11、添加属性与分组关联关系
     * /product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrAttrgroupRelationEntity> entities){
        attrAttrgroupRelationService.saveBatch(entities);
        return R.ok();
    }

    /**
     * 12、删除属性与分组的关联关系
     * /product/attrgroup/attr/relation/delete
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrAttrgroupRelationEntity[] entities){
        attrGroupService.deleteRelation(entities);
        return R.ok();
    }

    /**
     * 13、获取属性分组没有关联的其他属性
     * /product/attrgroup/{attrgroupId}/noattr/relation
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId, @RequestParam Map<String, Object> params){
        PageUtils pageUtils = attrService.getNoRelationAttr(attrgroupId, params);
        return R.ok().put("page", pageUtils);
    }

    /**
     * 17、获取分类下所有分组&关联属性
     * /product/attrgroup/{catelogId}/withattr
     */
    @GetMapping("/{catelogId}/withattr")
    public R attrGroupWithTheirArrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupRespVo> data = attrAttrgroupRelationService.listAttrGroupWithTheirArrs(catelogId);
        return R.ok().put("data", data);
    }
}
