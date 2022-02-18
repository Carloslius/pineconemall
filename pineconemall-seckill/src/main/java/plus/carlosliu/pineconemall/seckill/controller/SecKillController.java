package plus.carlosliu.pineconemall.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import plus.carlosliu.common.utils.R;
import plus.carlosliu.pineconemall.seckill.service.SecKillService;
import plus.carlosliu.pineconemall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

@Controller
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    @ResponseBody
    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKillSkus(){
        List<SeckillSkuRedisTo> tos = secKillService.getCurrentSecKillSkus();
        return R.ok().setData(tos);
    }

    @ResponseBody
    @GetMapping("/getSecKillSkuInfo/{skuId}")
    public R getSecKillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = secKillService.getSecKillSkuInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId,
                       @RequestParam("code") String code,
                       @RequestParam("num") Integer num,
                       Model model) {
        String orderSn = secKillService.kill(killId, code, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
