package plus.carlosliu.pineconemall.ware.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import plus.carlosliu.common.to.SkuHasStockTo;
import plus.carlosliu.pineconemall.ware.entity.WareSkuEntity;
import plus.carlosliu.pineconemall.ware.service.WareSkuService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;



/**
 * 商品库存
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:11:00
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 02、查询商品库存
     * /ware/waresku/list
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }

    /**
     * 查询sku是否有库存，上架功能远程调用
     */
    @PostMapping("/hasStock")
    public Map<Long, Boolean> getSkuHasStock(@RequestBody List<Long> skuIds){
        Map<Long, Boolean> map = new HashMap<>();
        List<SkuHasStockTo> stockTos =  wareSkuService.getSkuHasStock(skuIds);
        stockTos.stream().filter(stockTo -> {
            map.put(stockTo.getSkuId(), stockTo.getHasStock());
            return true;
        }).collect(Collectors.toList());
        return map;
    }

}
