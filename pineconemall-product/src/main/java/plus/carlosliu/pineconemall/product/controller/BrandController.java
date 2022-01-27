package plus.carlosliu.pineconemall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import plus.carlosliu.common.valid.AddGroup;
import plus.carlosliu.common.valid.UpdateGroup;
import plus.carlosliu.common.valid.UpdateStatusGroup;
import plus.carlosliu.pineconemall.product.entity.BrandEntity;
import plus.carlosliu.pineconemall.product.service.BrandService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;



/**
 * 品牌
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 12:33:30
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 00、获取品牌数据列表
     * /product/brand/list
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 00、批量删除品牌，需要删除关联表中的关联关系
     * /product/brand/delete
     */
    @RequestMapping("/delete")
    public R deleteBatch(@RequestBody List<Long> brandIds){
        brandService.removeCascadeByIds(brandIds);
        return R.ok();
    }

    /**
     * 00、修改品牌显示状态
     * /product/brand/update/status
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated({UpdateStatusGroup.class})@RequestBody BrandEntity brand){
        brandService.updateById(brand);
        return R.ok();
    }


    /**
     * 00、显示品牌详细信息
     * /product/brand/info/{brandId}
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
        BrandEntity brand = brandService.getById(brandId);
        return R.ok().put("brand", brand);
    }

    /**
     * 00、修改品牌详细信息
     * /product/brand/update
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class})@RequestBody BrandEntity brand){
        brandService.updateCascadeById(brand);
        return R.ok();
    }

    /**
     * 00、增加品牌信息
     * /product/brand/save
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*,BindingResult result*/){
//        if(result.hasErrors()){
//            Map<String,String> map = new HashMap<>();
//            //1、获取校验的错误结果
//            result.getFieldErrors().forEach((item)->{
//                //FieldError 获取到错误提示
//                String message = item.getDefaultMessage();
//                //获取错误的属性的名字
//                String field = item.getField();
//                map.put(field,message);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        }else {
//        }
        brandService.save(brand);
        return R.ok();
    }
}
