package plus.carlosliu.pineconemall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import plus.carlosliu.common.utils.R;

@FeignClient("pineconemall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/getSpuBySkuId/{id}")
    R getSpuBySkuId(@PathVariable("id") Long skuId);

    @RequestMapping("/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);
}
