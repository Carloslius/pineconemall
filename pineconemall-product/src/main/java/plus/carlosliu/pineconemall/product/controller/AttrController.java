package plus.carlosliu.pineconemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import plus.carlosliu.pineconemall.product.entity.AttrEntity;
import plus.carlosliu.pineconemall.product.service.AttrService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.product.vo.AttrRespVo;
import plus.carlosliu.pineconemall.product.vo.AttrVo;


/**
 * 商品属性
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    /**
     * 05、获取分类规格参数
     * 09、获取分类销售属性
     * /product/attr/base/list/{catelogId}
     * /product/attr/sale/list/{catelogId}
     */
    @GetMapping("/{type}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId, @PathVariable("type") String type){
        PageUtils page = attrService.queryTypeAttrPage(params, catelogId, type);
        return R.ok().put("page", page);
    }

    /**
     * 06、保存属性【规格参数，销售属性】
     * /product/attr/save
     */
    @PostMapping("/save")
    public R save(@RequestBody AttrVo attr){
        attrService.saveAttr(attr);
        return R.ok();
    }

    /**
     * 07、查询属性详情
     * /product/attr/info/{attrId}
     */
    @GetMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        AttrRespVo attrRespVo = attrService.getDetailById(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 08、修改属性
     * /product/attr/update
     */
    @PostMapping("/update")
    public R update(@RequestBody AttrVo attr){
        attrService.updateAttr(attr);
        return R.ok();
    }
}
