package plus.carlosliu.pineconemall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import plus.carlosliu.pineconemall.ware.entity.WareInfoEntity;
import plus.carlosliu.pineconemall.ware.service.WareInfoService;
import plus.carlosliu.common.utils.PageUtils;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.ware.vo.FareVo;


/**
 * 仓库信息
 *
 * @author CarlosLiu
 * @email 1753459461@qq.com
 * @date 2022-01-20 13:11:00
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wareinfo:info")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 01、仓库列表
     * /ware/wareinfo/list
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }

    /**
     * 根据收货地址获取运费
     * @param addrId 地址
     * @return 运费，和对应的地址信息
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId){
        FareVo fare = wareInfoService.getFare(addrId);
        return R.ok().setData(fare);
    }

}
