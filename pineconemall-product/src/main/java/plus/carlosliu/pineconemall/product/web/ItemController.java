package plus.carlosliu.pineconemall.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import plus.carlosliu.pineconemall.product.service.SkuInfoService;
import plus.carlosliu.pineconemall.product.vo.web.SkuItemVo;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo vo =  skuInfoService.item(skuId);
        model.addAttribute("item", vo);
        return "item";
    }
}
